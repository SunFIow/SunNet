package com.sunflow.common;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.sunflow.message.PacketBuffer;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;
import com.sunflow.util.TSQueue;
import com.sunflow.util.Task;

public abstract class CommonContext implements Runnable, Closeable {
	private static int id = 0;

	protected final Side side;
	private final ThreadGroup threadGroup;

	private boolean running = false;

	private TSQueue<Thread> taskThread_queue;
	private TSQueueImpl<Thread> taskThreads;

	public CommonContext(Side side) { this(side, null); }

	public CommonContext(Side side, ThreadGroup threadGroup) {
		this.side = side;
		this.threadGroup = threadGroup;
		this.taskThread_queue = new TSQueue<>();
		this.taskThreads = new TSQueueImpl<>();
	}

	public void post(String description, Runnable post) {
		Logger.debug("CommonContext", "Run Task[" + description + "::" + id++ + "," + threadGroup.getName() + "]");
		post.run();
	}

	public void async_post(String description, Runnable post) {
		synchronized (this) {
			Thread postThread = new Thread(threadGroup, post, description + "::" + id++);
			taskThread_queue.push_back(postThread);
			notify();
		}
	}

	public <T extends Exception> void task(String description, Task<T> task, Consumer<T> errorConsumer) {
		post(description, () -> {
			try {
				task.execute();
			} catch (Exception e) {
				@SuppressWarnings("unchecked")
				T error = (T) e;
				errorConsumer.accept(error);
			}
		});
	}

	public <T extends Exception> void async_task(String description, Task<T> task, Consumer<T> errorConsumer) {
		async_post(description, () -> {
			try {
				task.execute();
			} catch (Exception e) {
				@SuppressWarnings("unchecked")
				T error = (T) e;
				errorConsumer.accept(error);
			}
		});
	}

	public abstract void accept(Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer);

	public abstract void async_accept(Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer);

	public abstract void connect(InetSocketAddress serverEndpoint, Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer);

	public abstract void async_connect(InetSocketAddress serverEndpoint, Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer);

	public void write(Socket socket, PacketBuffer buffer,
			Consumer<Integer> successConsumer, Consumer<Exception> errorConsumer) {
		task(side + "_context_write", () -> {
			OutputStream out = socket.getOutputStream();
			int wroteBytes = buffer.write(out);
			successConsumer.accept(wroteBytes);
		}, errorConsumer);
	}

	public void async_write(Socket socket, PacketBuffer buffer,
			Consumer<Integer> successConsumer, Consumer<Exception> errorConsumer) {
		async_task(side + "_context_async_write", () -> {
			OutputStream out = socket.getOutputStream();
			int wroteBytes = buffer.write(out);
			successConsumer.accept(wroteBytes);
		}, errorConsumer);
	}

	public void read(Socket socket, PacketBuffer buffer,
			Consumer<Integer> messageConsumer, Consumer<Exception> errorConsumer) {
		task(side + "_context_read", () -> {
			InputStream in = socket.getInputStream();
			int readBytes = buffer.read(in);
			messageConsumer.accept(readBytes);
		}, errorConsumer);
	}

	public void async_read(Socket socket, PacketBuffer buffer,
			Consumer<Integer> messageConsumer, Consumer<Exception> errorConsumer) {
		async_task(side + "_context_async_read", () -> {
			InputStream in = socket.getInputStream();
			int readBytes = buffer.read(in);
			messageConsumer.accept(readBytes);
		}, errorConsumer);
	}

	@Override
	public void run() {
		running = true;
		Thread taskThread = null;
		while (running) {
			try {
				synchronized (this) {
					while ((taskThread = taskThread_queue.pop_front()) == null && running)
						wait();
					if (taskThread != null) {
						taskThreads.tsremoveUnless(Thread::isAlive);
						Logger.debug("CommonContext", "Start " + taskThread);
						taskThread.start();
						taskThreads.push_back(taskThread);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		finishTasks();

		Logger.debug(Thread.currentThread() + "CommonContext", "EXIT");
	}

	private void finishTasks() {
		Logger.debug(Thread.currentThread() + "CommonContext", "Running TaskThreads: " + taskThreads.count());
		taskThreads.tsforeach(Logger::info);

		// The Context should close, so finish all running tasks
		Logger.debug(Thread.currentThread() + "CommonContext", "Stop all running TaskThreads");
		taskThreads.tsforeach(t -> {
			try {
				t.join(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		taskThreads.tsremoveUnless(Thread::isAlive);
		Logger.debug(Thread.currentThread() + "CommonContext", "Stopped all running TaskThreads");
		taskThreads.tsforeach(Logger::info);
	}

	public void stop() { close(); }

	@Override
	public void close() {
		Logger.debug("CommonContext", "close()");
		running = false;
		synchronized (this) {
			notify();
		}
	}

	public boolean isRunning() { return running; }

	private class TSQueueImpl<T> extends TSQueue<T> {
		protected void tsforeach(Consumer<T> action) { tsrunnable(() -> deqQueue.forEach(action)); }

		protected void tsremoveIf(Predicate<T> filter) { tsrunnable(() -> deqQueue.removeIf(filter)); }

		protected void tsremoveUnless(Predicate<T> filter) { tsremoveIf(filter.negate()); }
	}
}
