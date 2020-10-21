package com.sunflow.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.sunflow.util.Logger;
import com.sunflow.util.Side;
import com.sunflow.util.TSQueue;

public abstract class CommonContext implements Runnable, Closeable {

	private int id = 0;

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

	public void async_post(String description, Runnable task) {
		synchronized (this) {
			taskThread_queue.push_back(new Thread(threadGroup, task, description + "::" + id++));
			notify();
		}
	}

	public abstract void async_accept(BiConsumer<IOException, Socket> socketConsumer);

	public abstract void async_connect(InetSocketAddress serverEndpoint,
			BiConsumer<IOException, Socket> consumer);

	public <T extends Serializable> void async_write(ObjectOutputStream out, T data,
			Consumer<IOException> consumer) {
		async_post(side + "_context_async_write", () -> {
			IOException error = null;
			try {
				out.writeObject(data);
				out.flush();
			} catch (IOException e) {
				error = e;
			} finally {
				consumer.accept(error);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public <T> void async_read(ObjectInputStream in,
			BiConsumer<Exception, T> consumer) {
		async_post(side + "_context_async_read", () -> {
			T data = null;
			Exception error = null;
			try {
				data = (T) in.readObject();
			} catch (ClassCastException e) {
				e.addSuppressed(new InvalidObjectException(
						"Read object is not the expected type / " + (data != null
								? data.getClass() + " - " + data
								: "NULL")));
				error = e;
			} catch (ClassNotFoundException | IOException e) {
				error = e;
			} finally {
				consumer.accept(error, data);
			}
		});
	}

	public <T extends Serializable> void async_write(Socket socket, T data,
			Consumer<IOException> consumer) {
		async_post(side + "_context_async_write", () -> {
			IOException error = null;
			try {
				OutputStream os = socket.getOutputStream();
				BufferedOutputStream bos = new BufferedOutputStream(os);
				ObjectOutputStream oos = new ObjectOutputStream(bos);

				oos.writeObject(data);

				oos.flush();
			} catch (IOException e) {
				error = e;
			} finally {
				consumer.accept(error);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public <T> void async_read(Socket socket,
			BiConsumer<Exception, T> consumer) {
		async_post(side + "_context_async_read", () -> {
			T data = null;
			Exception error = null;
			try {
				InputStream is = socket.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ObjectInputStream ois = new ObjectInputStream(bis);

				data = (T) ois.readObject();
			} catch (ClassCastException e) {
				e.addSuppressed(new InvalidObjectException(
						"Read object is not the expected type / " + (data != null
								? data.getClass() + " - " + data
								: "NULL")));
				error = e;
			} catch (ClassNotFoundException | IOException e) {
				error = e;
			} finally {
				consumer.accept(error, data);
			}
		});
	}

	@Override
	public void run() {
		running = true;
		Thread taskThread = null;
		while (running) {
			try {
				synchronized (this) {
					while ((taskThread = taskThread_queue.pop_front()) == null)
						wait();
					taskThreads.tsremoveUnless(Thread::isAlive);
					Logger.debug("CommonContext", "Start " + taskThread + " Task");
					taskThread.start();
					taskThreads.push_back(taskThread);
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
	public void close() { Logger.debug("CommonContext", "close()"); running = false; }

	public boolean isRunning() { return running; }

	private class TSQueueImpl<T> extends TSQueue<T> {
		protected void tsforeach(Consumer<T> action) { tsrunnable(() -> deqQueue.forEach(action)); }

		protected void tsremoveIf(Predicate<T> filter) { tsrunnable(() -> deqQueue.removeIf(filter)); }

		protected void tsremoveUnless(Predicate<T> filter) { tsremoveIf(filter.negate()); }
	}
}
