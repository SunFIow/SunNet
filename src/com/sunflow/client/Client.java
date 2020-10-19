package com.sunflow.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.ConnectIOException;

import com.sunflow.common.Connection;
import com.sunflow.common.Logger;
import com.sunflow.common.Message;
import com.sunflow.common.Side;
import com.sunflow.common.TSQueue;
import com.sunflow.common.ThreadContext;

public class Client {

	/**
	 * @param <T>
	 *            The type of messages
	 */
	public static class Interface<T> implements Closeable {

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
		protected ThreadContext m_context;

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

			// Create the acceptor whose purpose is to provide a unique socket
//			 for each incoming connection attempt
//			m_connector = new Connector(m_context);

		}

		/**
		 * Connect to server with hostname/ip-address and port between 0 and 65535
		 * 
		 * @param host
		 *            The hostname/ip-address
		 * @param port
		 *            The port number
		 */

		public boolean connect(final String host, final int port) {
			Logger.info("CLIENT", "Connecting...");

			// Resolve hostname/ip-address into tangible physical address
			InetSocketAddress serverEndpoint = new InetSocketAddress(host, port);

			// Throw Exception if it hostname/ip-address could't get resolved
			if (serverEndpoint.isUnresolved()) {
				Logger.fatal("CLIENT", "Connecting Exception:");
				new ConnectIOException("The hostname/ip-address[" + host + "] with port[" + port + "] couldn't get resolved!").printStackTrace();
				return false;
			}

			// Create the context
			m_context = new ClientContext(clientThreadGroup);

			clientThreadGroup = new ThreadGroup(host + ":" + port + "/Client-Thread-Group");

			m_context.async_connect(serverEndpoint, (error, socket) -> {
//				SocketAddress clientEndpoint = socket.getLocalSocketAddress();
				if (error == null) {
					Logger.info("CLIENT", "Succesfully conntected to (" + serverEndpoint + ")");

					m_connection = new Connection<>(Side.client, m_context, socket, m_qMessagesIn);

					m_connection.connectToServer(4711); // TODO the uid
				} else {
					Logger.fatal("CLIENT", "Connecting Error: ");
					new ConnectIOException("Couldn't connect to " + serverEndpoint, error).printStackTrace();
				}
			});

			// Start Context Thread
			m_threadContext = new Thread(
					clientThreadGroup,
					m_context::run,
					"ClientContext");
			m_threadContext.start();

			return true;
		}

		/**
		 * Disconnect from the server
		 */
		public void disconnect() {
			Logger.debug("CLIENT", "disconnect()");
			// If connection exists, and it's connected then...
			if (isConnected()) {
				// ...disconnect from server gracefully
				m_connection.disconnect();
			}

			try {
				// Either way we're also done with the thread, we stop it...
				m_context.stop();
//				m_threadContext.stop(); // TODO we want to stop the thread by a boolean variable instead

				// ...and wait for it to die
				Logger.info("CLIENT", "Wait 1000 ms for " + m_threadContext + " to die");
				long start = System.currentTimeMillis();
				m_threadContext.join(1000);
				if (m_threadContext.isAlive()) {
					Logger.info("CLIENT", m_threadContext + " is still alive after 1000 ms so we stop him now");
					m_threadContext.stop();
				} else {
					long now = System.currentTimeMillis();
					Logger.info("CLIENT", m_threadContext + " died after: " + (now - start) + " ms");
				}

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
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
