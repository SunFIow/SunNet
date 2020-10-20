package com.sunflow.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.rmi.ConnectIOException;
import java.util.ArrayDeque;
import java.util.Deque;

import com.sunflow.common.CommonContext;
import com.sunflow.common.Connection;
import com.sunflow.common.Message;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;
import com.sunflow.util.TSQueue;

public class Server {

	/**
	 * Implementations of this class need to override onClientConnect,
	 * due to this class denying all connections by default
	 * 
	 * @param <T>
	 *            The type of messages
	 */
	public static class Interface<T> implements Closeable {

		@Override
		public void close() {
			Logger.debug("SERVER", "close()");
			stop();
		}

		/**
		 * Thread Safe Queue for incoming message packets
		 */
		protected TSQueue<Message.Owned<T>> m_qMessagesIn;

		/**
		 * Container of active validated connections
		 */
		protected Deque<Connection<T>> m_deqConnections;

		/**
		 * Main ThreadGroup of the server <br>
		 * All Threads used by the server are in it
		 */
		protected ThreadGroup serverThreadGroup;

		/**
		 * Order of declaration is important - it is also the order of initialisation
		 */
		protected CommonContext m_context;

		/**
		 * Thread to execute all async work on
		 */
		protected Thread m_threadContext;

		/**
		 * Clients will be identified in the "wider system" via an ID
		 */
		protected int nIDCounter = 10000;

		public Interface() {
			m_qMessagesIn = new TSQueue<>();
			m_deqConnections = new ArrayDeque<>();
		}

		/**
		 * Creates a server without starting, bound to the specified port. A port number
		 * of {@code 0} means that the port number is automatically
		 * allocated, typically from an ephemeral port range.
		 * 
		 * @param port
		 *            the port number, or {@code 0} to use a port
		 *            number that is automatically allocated.
		 */
		public Interface(int port) {
			this(new InetSocketAddress(port));
		}

		/**
		 * Creates a server without starting, bound to a specific address
		 * (IP address and port number).
		 * <p>
		 * If the address is {@code null}, then the system will pick up
		 * an ephemeral port and a valid local address to bind the socket.
		 * 
		 * @param host
		 *            The hostname/ip-address to bind to
		 * @param port
		 *            the port number, or {@code 0} to use a port
		 *            number that is automatically allocated.
		 */
		public Interface(String host, int port) {
			this(new InetSocketAddress(host, port));
		}

		/**
		 * Creates a server without starting, bound to a specific address
		 * (IP address and port number).
		 * <p>
		 * If the address is {@code null}, then the system will pick up
		 * an ephemeral port and a valid local address to bind the socket.
		 * 
		 * @param host
		 *            The ip-address to bind to
		 * @param port
		 *            the port number, or {@code 0} to use a port
		 *            number that is automatically allocated.
		 */
		public Interface(InetAddress host, int port) {
			this(new InetSocketAddress(host, port));
		}

		/**
		 * Creates a server without starting, bound to a specific address
		 * (IP address and port number).
		 * <p>
		 * If the address is {@code null}, then the system will pick up
		 * an ephemeral port and a valid local address to bind the socket.
		 * 
		 * @param endpoint
		 *            The ip-address and port number to bind to.
		 */
		public Interface(InetSocketAddress endpoint) {
			this();
			create(endpoint);
		}

		/**
		 * Creates a server, bound to a specific address
		 * (IP address and port number).
		 * <p>
		 * If the address is {@code null}, then the system will pick up
		 * an ephemeral port and a valid local address to bind the socket.
		 * 
		 * @param endpoint
		 *            The ip-address and port number to bind to.
		 * 
		 * @return If the server started without errors
		 */
		private boolean create(InetSocketAddress endpoint) {
			serverThreadGroup = new ThreadGroup(endpoint.getPort() + "/Server-Thread-Group");// Create the context

			try {
				m_context = new ServerContext(serverThreadGroup, endpoint);
			} catch (IOException e) {
				Logger.fatal("SERVER", "Starting Error:", e);
				return false;
			}
			return true;
		}

		/**
		 * Creates and starts a server, bound to the specified port. A port number
		 * of {@code 0} means that the port number is automatically
		 * allocated, typically from an ephemeral port range.
		 * 
		 * @param port
		 *            the port number, or {@code 0} to use a port
		 *            number that is automatically allocated.
		 * 
		 * @return If the server started without errors
		 */
		public boolean start(int port) {
			return start(new InetSocketAddress(port));
		}

		/**
		 * Creates and starts a server, bound to a specific address
		 * (IP address and port number).
		 * <p>
		 * A port number of {@code 0} means that the port number is automatically
		 * allocated, typically from an ephemeral port range.
		 * 
		 * @param host
		 *            The hostname/ip-address to bind to
		 * @param port
		 *            the port number, or {@code 0} to use a port
		 *            number that is automatically allocated.
		 * 
		 * @return If the server started without errors
		 */
		public boolean start(String host, int port) {
			return start(new InetSocketAddress(host, port));
		}

		/**
		 * Creates and starts a server, bound to a specific address
		 * (IP address and port number).
		 * <p>
		 * If the address is {@code null}, then the system will pick up
		 * an ephemeral port and a valid local address to bind the socket.
		 * 
		 * @param host
		 *            The ip-address to bind to
		 * @param port
		 *            the port number, or {@code 0} to use a port
		 *            number that is automatically allocated.
		 * 
		 * @return If the server started without errors
		 */
		public boolean start(InetAddress host, int port) {
			return start(new InetSocketAddress(host, port));
		}

		/**
		 * Creates and starts a server, bound to a specific address
		 * (IP address and port number).
		 * <p>
		 * If the address is {@code null}, then the system will pick up
		 * an ephemeral port and a valid local address to bind the socket.
		 * 
		 * @param endpoint
		 *            The ip-address and port number to bind to.
		 * 
		 * @return If the server started without errors
		 */
		public boolean start(InetSocketAddress endpoint) {
			boolean error = create(endpoint);

			if (error) return false;

			start();
			return true;
		}

		public boolean start() {
			Logger.info("SERVER", "Starting...");

			if (m_context == null) {
				Logger.error("SERVER", "Can't start a server that isn't created yet\ncall start(int), start(String,int), start(InetAddress,int) or start(InetSocketAddress) instead!");
				return false;
			}

			/*
			 * Issue a task to the context - This is important
			 * as it will prime the context with "work", and stop it
			 * from exiting immediately. Since this is a server, we
			 * want it primed ready to handle clients trying to
			 * connect.
			 */
			waitForClientConnection();

			// Launch the context in its own thread
			m_threadContext = new Thread(
					serverThreadGroup,
					m_context::run,
					"ServerContext");
			m_threadContext.start();

			Logger.info("SERVER", "Started!");
			return true;
		}

		/**
		 * Stops the server but does not close it
		 */
		public void stop() {
			if (!isRunning()) return;
			// Request the context to close
			m_context.close();

			try {
				Logger.debug("SERVER", "Wait 3000 ms for " + m_threadContext + " to die");
				long start = System.currentTimeMillis();
				// ...and wait for it to die
				m_threadContext.join(3000);
				if (m_threadContext.isAlive()) {
					Logger.debug("SERVER", m_threadContext + " is still alive after 3000 ms so we stop him now");
					m_threadContext.stop();
				} else {
					long now = System.currentTimeMillis();
					Logger.debug("SERVER", m_threadContext + " died after: " + (now - start) + " ms");
				}
			} catch (InterruptedException e) {
				Logger.error("SERVER", Thread.currentThread() + " got interrupted while waiting for " + m_threadContext + " to die", e);
			}

			Logger.info("SERVER", "Stopped!");
		}

		public boolean isCreated() {
			return m_context != null;
		}

		public boolean isRunning() {
			return isCreated() && m_context.isRunning();
		}

		/**
		 * ASYNC - Instrict asio to wait for connection
		 */
		public void waitForClientConnection() {
			if (!isCreated()) return;
			/*
			 * Prime context with an instruction to wait until a socket connects. This
			 * is the purpose of an "acceptor" object. It will provide a unique socket
			 * for each incoming connection attempt
			 */

			m_context.async_accept((error, socket) -> {
				// Triggered by incoming connection request
				if (error == null) {
					Logger.info("SERVER", "New Connection: (" + socket.getRemoteSocketAddress() + ")");

					Connection<T> newconn = new Connection<>(Side.server, m_context, socket, m_qMessagesIn);

					// Give the server impl a chance to deny connection
					if (onClientConnect(newconn, nIDCounter)) {
						// Connection allowed, so add to container of new connections
						m_deqConnections.offerLast(newconn);

						newconn.connectToClient(nIDCounter++);

						Logger.info("SERVER", "(" + newconn.getID() + ") Connection Approved");
					} else {
						Logger.debug("SERVER", "Connection Denied");
					}
				} else {
					// Error has occurred during acceptance
					Logger.error("SERVER", "New Connection Error:", new ConnectIOException("", error));
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				// Prime the context with more work - again simply wait for another connection...
				waitForClientConnection();
			});
		}

		/**
		 * Send a message to a specific client
		 * 
		 * @param client
		 *            The specific client
		 * @param msg
		 *            The message
		 */
		public void messageClient(Connection<T> client, final Message<T> msg) {
			// Check client is connected...
			if (client != null && client.isConnected())
				client.send(msg);
			else {
				// The client couldn't be contacted, so assume it has disconnected.
				clientNotConnected(client);
			}
		}

		/**
		 * Send a message to all clients
		 * 
		 * @param msg
		 *            The message
		 */
		public void messageAllClients(final Message<T> msg) { messageAllClients(msg, null); }

		/**
		 * Send a message to all clients except the ignored one
		 * 
		 * @param msg
		 *            The message
		 * @param ignoreClient
		 *            The client to ignore, null to send to everybody
		 */
		public void messageAllClients(final Message<T> msg, Connection<T> ignoreClient) {
			for (Connection<T> client : m_deqConnections) {
				// Check client is connected...
				if (client != null && client.isConnected()) {
					// Check that it's not the client we want to ignore
					if (client != ignoreClient)
						client.send(msg);
				} else {
					// The client couldn't be contacted, so assume it has disconnected.
					clientNotConnected(client);
				}
			}
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
				onMessage(msg.getRemote(), msg.getMessage());

				messageCount++;
			}
		}

		/**
		 * Called when a client couldn't be contacted, so we assume it has disconnected.
		 * 
		 * @param client
		 *            that couldn't be contacted
		 */
		private void clientNotConnected(Connection<T> client) {
			onClientDisconnect(client);
			m_deqConnections.remove(client);
			client.disconnect();
		}

		/**
		 * Called when a client connects
		 * 
		 * 
		 * @param client
		 *            The connecting client
		 * @return true to allow the connection, false to deny the connection
		 */
		protected boolean onClientConnect(Connection<T> client, int clientID) { return false; }

		/**
		 * Called when a client appears to have disconnected
		 * 
		 * @param client
		 *            The disconnected client
		 */
		protected void onClientDisconnect(Connection<T> client) {}

		/**
		 * Called when a message arrives
		 * 
		 * @param client
		 *            The client that sent the message
		 * @param msg
		 *            The message
		 */
		protected void onMessage(Connection<T> client, Message<T> msg) {}
	}
}