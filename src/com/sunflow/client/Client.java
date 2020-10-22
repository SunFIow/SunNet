package com.sunflow.client;

import java.io.Closeable;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.sunflow.common.CommonContext;
import com.sunflow.common.Connection;
import com.sunflow.common.Message;
import com.sunflow.error.ConnectingException;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;
import com.sunflow.util.TSQueue;

public class Client {

	/**
	 * @param <T>
	 *            The type of messages
	 */
	public static class Interface<T extends Serializable> implements Closeable {

		/**
		 * If the client is destroyed, always try and disconnect from server
		 */
		@Override
		public void close() {
			Logger.debug("CLIENT", "close()");
			disconnect();
		}

		/**
		 * Main ThreadGroup of the client <br>
		 * All Threads used by the client are in it
		 */
		protected ThreadGroup clientThreadGroup;

		/**
		 * Context handels the data transfer...
		 */
		protected CommonContext m_context;

		/**
		 * ...but needs a thread of its own to exectue its work commands
		 */
		protected Thread m_threadContext;

		/**
		 * The client has a single instance of a "connection" object,
		 * which handles data transfer
		 */
		protected Connection<T> m_connection;

		/**
		 * This is the thread safe queue of incoming messages from the server
		 */
		private TSQueue<Message.Owned<T>> m_qMessagesIn;

		public Interface() {
			m_qMessagesIn = new TSQueue<>();
		}

		/**
		 * Connect to server
		 * 
		 * @param host
		 *            The hostname/ip-address of the server
		 * @param port
		 *            The port number of the server, between 0 and 65535
		 */

		public void connect(String host, int port) {
			// Resolve hostname/ip-address into tangible physical address
			InetSocketAddress serverEndpoint = new InetSocketAddress(host, port);
			connect(serverEndpoint);
		}

		/**
		 * Connect to server
		 * 
		 * @param host
		 *            The ip-address of the server
		 * @param port
		 *            The port number of the server, between 0 and 65535
		 */

		public void connect(InetAddress host, int port) {
			// Resolve hostname/ip-address into tangible physical address
			InetSocketAddress serverEndpoint = new InetSocketAddress(host, port);
			connect(serverEndpoint);
		}

		/**
		 * Connect to server
		 * 
		 * @param endpoint
		 *            InetSocketAddress of the server, between 0 and 65535
		 */
		public void connect(InetSocketAddress endpoint) {
			Logger.info("CLIENT", "Connecting...");

			clientThreadGroup = new ThreadGroup(endpoint + "/Client-Thread-Group");

			// Create the context
			m_context = new ClientContext(clientThreadGroup);

			m_context.connect(endpoint, socket -> {
//				SocketAddress clientEndpoint = socket.getLocalSocketAddress();
				Logger.info("CLIENT", "Succesfully conntected to (" + endpoint + ")");
				m_connection = new Connection<>(Side.Client, m_context, socket, m_qMessagesIn);
				m_connection.connectToServer();
			}, error -> Logger.error("CLIENT", "couldn't connect", new ConnectingException("", error)));

			// Start Context Thread
			m_threadContext = new Thread(
					clientThreadGroup,
					m_context::run,
					"ClientContext");
			m_threadContext.start();
		}

		/**
		 * Disconnect from the server
		 */
		@SuppressWarnings("deprecation")
		public void disconnect() {
			Logger.debug("CLIENT", "disconnect()");
			// If connection exists, and it's connected then...
			if (isConnected()) {
				// ...disconnect from server gracefully
				m_connection.disconnect();
			}

			// Either way we're also done with the thread, we stop it...
			m_context.stop();

			try {
				Logger.debug("CLIENT", "Wait 3000 ms for " + m_threadContext + " to die");
				long start = System.currentTimeMillis();
				// ...and wait for it to die
				m_threadContext.join(3000);
				if (m_threadContext.isAlive()) {
					Logger.debug("CLIENT", m_threadContext + " is still alive after 3000 ms so we stop him now");
					m_threadContext.stop();
				} else {
					long now = System.currentTimeMillis();
					Logger.debug("CLIENT", m_threadContext + " died after: " + (now - start) + " ms");
				}
			} catch (InterruptedException e) {
				Logger.error("CLIENT", Thread.currentThread() + " got interrupted while waiting for " + m_threadContext + " to die", e);
			}
		}

		/**
		 * Check if client is actually connected to a server
		 */
		public boolean isConnected() {
			if (m_connection != null) return m_connection.isConnected();
			else return false;
		}

		/**
		 * Retrieve queue of messages from server
		 */
		public TSQueue<Message.Owned<T>> incoming() { return m_qMessagesIn; };

		public void send(Message<T> msg) {
			if (isConnected())
				m_connection.send(msg);
		}

		/**
		 * 
		 */
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
				Message.Owned<T> msg = m_qMessagesIn.pop_front();

				// Pass to message handler
				onMessage(msg.getMessage());

				messageCount++;
			}
		}

		/**
		 * Called when a message arrives
		 * 
		 * @param msg
		 *            The message
		 */
		protected void onMessage(Message<T> msg) {}
	}
}
