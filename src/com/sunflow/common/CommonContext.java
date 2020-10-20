package com.sunflow.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
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
import com.sunflow.util.TriConsumer;

public abstract class CommonContext implements Runnable, Closeable {

	private int id = 0;

	protected final Side side;
	private final ThreadGroup threadGroup;

	private boolean shouldClose = false;

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
		taskThread_queue.push_back(new Thread(threadGroup, task, description + "::" + id++));
	}

	public abstract void async_accept(BiConsumer<IOException, Socket> socketConsumer);

	public abstract void async_connect(InetSocketAddress serverEndpoint,
			BiConsumer<IOException, Socket> consumer);

	public <T extends Serializable> void async_write(Socket socket,
			T data, int length,
			BiConsumer<IOException, Integer> consumer) {
		async_post(side + "_context_async_write", () -> {
			IOException error = null;
			try {
				OutputStream os = socket.getOutputStream();
				BufferedOutputStream bos = new BufferedOutputStream(os);
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.flush();

				oos.writeInt(length);
				oos.writeObject(data);

				oos.flush();
			} catch (IOException e) {
				error = e;
			} finally {
				consumer.accept(error, length);
			}
		});
	}

	public void async_read(Socket socket,
			TriConsumer<Exception, Integer, Object> consumer) {
		Logger.debug(Thread.currentThread(), -1);
		async_post(side + "_context_async_read", () -> {
			Logger.debug(Thread.currentThread(), 1);
			int length = -1;
			Object data = null;
			Exception error = null;
			try {
				Logger.debug(Thread.currentThread(), 2);
				InputStream is = socket.getInputStream();
				Logger.debug(Thread.currentThread(), 3);
				BufferedInputStream bis = new BufferedInputStream(is);
				Logger.debug(Thread.currentThread(), 4);
				ObjectInputStream ois = new ObjectInputStream(bis);
				Logger.debug(Thread.currentThread(), 5);

				length = ois.readInt();
				Logger.debug(Thread.currentThread(), 6);
				data = ois.readObject();
				Logger.debug(Thread.currentThread(), 7);
			} catch (IOException | ClassNotFoundException e) {
				Logger.debug(Thread.currentThread(), 8);
				error = e;
			} finally {
				Logger.debug(Thread.currentThread(), 9);
				consumer.accept(error, length, data);
			}
			Logger.debug(Thread.currentThread(), 10);
		});
		Logger.debug(Thread.currentThread(), 0);
	}

	public void async_read_not_closable(Socket socket,
			TriConsumer<Exception, Integer, Object> consumer) {
		async_post(side + "_context_async_read", () -> {
			int length = -1;
			Object data = null;
			Exception error = null;
			try {
				InputStream is = socket.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ObjectInputStream ois = new ObjectInputStream(bis);

				length = ois.readInt();
				data = ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				error = e;
			} finally {
				consumer.accept(error, length, data);
			}
		});
	}

	public <T> void async_read_generic(Socket socket,
			TriConsumer<Exception, Integer, T> consumer) {
		async_post(side + "_context_async_read_generic", () -> {
			int length = -1;
			T data = null;
			Exception error = null;
			try {
				InputStream is = socket.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ObjectInputStream ois = new ObjectInputStream(bis);

				length = ois.readInt();
				data = (T) ois.readObject();
			} catch (ClassNotFoundException | IOException e) {
				error = e;
			} catch (ClassCastException e) {
				Logger.fatal("CommonContext", "async_readGeneric() : ClassCastException");
				error = e;
			} finally {
				consumer.accept(error, length, data);
			}
		});
	}

	@Override
	public void run() {
		while (!shouldClose) {
//			taskThreads.tsrunnable(() -> taskThreads.deqQueue.removeIf(taskThread -> !taskThread.isAlive()));
			taskThreads.tsremoveUnless(Thread::isAlive);

			Thread taskThread;
			if ((taskThread = taskThread_queue.pop_front()) != null) {
				Logger.debug("CommonContext", "Start " + taskThread + " Task");

				taskThread.start();

				taskThreads.push_back(taskThread);
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Logger.info("CommonContext", "Running TaskThreads: " + taskThreads.count());
		taskThreads.tsforeach(Logger::info);

		// The Context should close, so finish all running tasks
		Logger.info("CommonContext", "Stop all running TaskThreads");
//		taskThreads.tsrunnable(() -> taskThreads.deqQueue.forEach(Thread::stop));
		taskThreads.tsforeach(Thread::stop); // TODO Make TaskThreads check an boolean from time to time to see if they should stop
		taskThreads.tsforeach(t -> {
			try {
				t.join(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}); // TODO Make TaskThreads check an boolean from time to time to see if they should stop
		Logger.info("CommonContext", "Stopped all running TaskThreads");
		taskThreads.tsforeach(Logger::info);

		Logger.info("CommonContext", "EXIT");
	}

	public void stop() throws IOException { close(); }

	@Override
	public void close() throws IOException { Logger.debug("CommonContext", "close()"); shouldClose = true; }

	private class TSQueueImpl<T> extends TSQueue<T> {
		protected void tsforeach(Consumer<T> action) { tsrunnable(() -> deqQueue.forEach(action)); }

		protected void tsremoveIf(Predicate<T> filter) { tsrunnable(() -> deqQueue.removeIf(filter)); }

		protected void tsremoveUnless(Predicate<T> filter) { tsremoveIf(filter.negate()); }
	}
}
