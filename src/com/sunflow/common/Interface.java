package com.sunflow.common;

import java.io.Closeable;
import java.util.function.Supplier;

import com.sunflow.util.TSQueue;

public abstract class Interface<T> implements Closeable {

	/**
	 * Main ThreadGroup of this interface <br>
	 * All Threads used by this interface are in it
	 */
	protected ThreadGroup threadGroup;

	/**
	 * Context handels the data transfer...
	 */
	protected CommonContext m_context;

	/**
	 * Thread to execute all work on
	 */
	protected Thread m_threadContext;

	/**
	 * Thread Safe Queue for incoming messages
	 */
	protected TSQueue<MessageBuffer.Owned<T>> m_qMessagesIn;

	/**
	 * Factory Method giving back a blank messages
	 */
	protected Supplier<MessageBuffer<T>> messageFactory;

	public Interface(Supplier<MessageBuffer<T>> messageFactory) {
		this.messageFactory = messageFactory;

		this.m_qMessagesIn = new TSQueue<>();
	}

	public void update() { update(Integer.MAX_VALUE); }

	/**
	 * 
	 * @param maxMessages
	 *            Maximum number of messages to process
	 */
	public void update(int maxMessages) {
		int messageCount = 0;
		while (messageCount < maxMessages && !m_qMessagesIn.empty()) {
			// Grab the front message
			MessageBuffer.Owned<T> msg = m_qMessagesIn.pop_front();

			// Pass to message handler
			onMessage(msg);

			messageCount++;
		}
	}

	protected abstract void onMessage(MessageBuffer.Owned<T> msg);
}
