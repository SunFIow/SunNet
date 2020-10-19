package com.sunflow.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.ConnectIOException;
import java.util.ArrayDeque;
import java.util.Deque;

import com.sunflow.common.Connection;
import com.sunflow.common.Logger;
import com.sunflow.common.Message;
import com.sunflow.common.Side;
import com.sunflow.common.TSQueue;
import com.sunflow.common.ThreadContext;

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
		protected ThreadContext m_context;

		/**
		 * Thread to execute all async work on
		 */
		protected Thread m_threadContext;

		/**
		 * Clients will be identified in the "wider system" via an ID
		 */
		protected int nIDCounter = 10000;

		/**
		 * Creates a server, bound to the specified port.
		 * A port numberof 0 means that the port number is automaticallyallocated,
		 * typically from an ephemeral port range.
		 * 
		 * @param port
		 *            The port number, or 0 to use a portnumber that is automatically allocated.
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		public Interface(int port) throws IOException {
			this(new InetSocketAddress(port));
		}

		public Interface(String host, int port) throws IOException {
			this(new InetSocketAddress(host, port));
		}

		public Interface(InetAddress host, int port) throws IOException {
			this(new InetSocketAddress(host, port));
		}

		public Interface(InetSocketAddress endpoint) throws IOException {
			m_qMessagesIn = new TSQueue<>();
			m_deqConnections = new ArrayDeque<>();

			serverThreadGroup = new ThreadGroup(endpoint.getPort() + "/Server-Thread-Group");

			// Create the acceptor whose purpose is to provide a unique socket
			// for each incoming connection attempt
//			m_acceptor = new Acceptor(m_context, endpoint);

			// Create the context
			m_context = new ServerContext(serverThreadGroup, endpoint);
		}

		/**
		 * Starts the server
		 * 
		 * @return If the server started without errors
		 */
		public boolean start() {
			Logger.info("SERVER", "Starting...");

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
			try {
				// Request the context to close
				m_context.close();
				// ...and wait for it to die
				m_threadContext.join();
			} catch (InterruptedException | IOException e) {
				// Some thread has interrupted the m_threadContext
				Logger.error("SERVER", "Stop Error:", e);
			}
			Logger.info("SERVER", "Stopped!");
		}

		/**
		 * ASYNC - Instrict asio to wait for connection
		 */
		public void waitForClientConnection() {
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
					if (onClientConnect(newconn)) {
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
				onClientDisconnect(client);
				m_deqConnections.remove(client); // TODO delete client
				client.disconnect();
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
					onClientDisconnect(client);
					m_deqConnections.remove(client); // TODO delete client
					client.disconnect();
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
		 * Called when a client connects
		 * 
		 * 
		 * @param client
		 *            The connecting client
		 * @return true to allow the connection, false to deny the connection
		 */
		protected boolean onClientConnect(Connection<T> client) { return false; }

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