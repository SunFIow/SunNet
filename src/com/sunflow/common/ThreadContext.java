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

public abstract class ThreadContext implements Runnable, Closeable {

	protected final Side side;
	private final ThreadGroup threadGroup;

	private boolean shouldClose = false;

	private TSQueue<Thread> taskThread_queue;
	private TSQueue<Thread> taskThreads;

	public ThreadContext(Side side) { this(side, null); }

	public ThreadContext(Side side, ThreadGroup threadGroup) {
		this.side = side;
		this.threadGroup = threadGroup;
		this.taskThread_queue = new TSQueue<>();
		this.taskThreads = new TSQueue<>();
	}

	public void post(String description, Runnable task) {
		taskThread_queue.push_back(new Thread(threadGroup, task, description));
	}

	void async_post1251251262(String description, Runnable task) {
		taskThread_queue.push_back(new Thread(threadGroup, task, description)); // TODO Make these task run async
	}

	public abstract void async_accept(BiConsumer<IOException, Socket> socketConsumer);

	public abstract void async_connect(InetSocketAddress serverEndpoint,
			BiConsumer<IOException, Socket> consumer);

	public <T extends Serializable> void async_write(Socket socket,
			T data, int length,
			BiConsumer<IOException, Integer> consumer) {
		post(side + "_context_async_write", () -> {
			IOException error = null;
			try {
				OutputStream os = socket.getOutputStream();
				BufferedOutputStream bos = new BufferedOutputStream(os);
				ObjectOutputStream oos = new ObjectOutputStream(bos);

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
		post(side + "_context_async_read", () -> {
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
		post(side + "_context_async_read_generic", () -> {
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
				Logger.fatal("async_readGeneric() : ClassCastException");
				error = e;
			} finally {
				consumer.accept(error, length, data);
			}
		});
	}

	@Override
	public void run() {
		while (!shouldClose) {
			taskThreads.tsrunnable(() -> taskThreads.deqQueue.removeIf(taskThread -> !taskThread.isAlive()));

			Thread taskThread;
			if ((taskThread = taskThread_queue.pop_front()) != null) {
				Logger.debug("Start " + taskThread + " Task");

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
		// The Context should close, so finish all running tasks
		Logger.info("ThreadContext", "Stop all running TaskThreads");
		taskThreads.tsrunnable(() -> taskThreads.deqQueue.forEach(Thread::stop));
		Logger.info("ThreadContext", "Stopped all running TaskThreads");

		Logger.info("ThreadContext", "EXIT");
	}

	public void stop() throws IOException { close(); }

	@Override
	public void close() throws IOException { Logger.debug("ThreadContext", "close()"); shouldClose = true; }

}
