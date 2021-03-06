package com.sunflow.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Supplier;

import com.sunflow.common.Connection;
import com.sunflow.common.Interface;
import com.sunflow.error.AcceptingException;
import com.sunflow.message.MessageBuffer;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;
import com.sunflow.util.TSQueue;

/**
 * Implementations of this class need to override onClientConnect,
 * due to this class denying all connections by default
 * 
 * @param <T>
 *            The type of messages
 */
public class Server<T> extends Interface<T> {

	@Override
	public void close() {
		Logger.debug("SERVER", "close()");
		stop();
	}

	/**
	 * Container of active validated connections
	 */
	protected Deque<Connection<T>> m_deqConnections;

	/**
	 * Clients will be identified in the "wider system" via an ID
	 */
	protected int nIDCounter = 10000;

	public Server() { this(MessageBuffer::new); }

	public Server(Supplier<MessageBuffer<T>> messageFactory) {
		super(messageFactory);

		this.m_qMessagesIn = new TSQueue<>();
		this.m_deqConnections = new ArrayDeque<>();
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
	public void create(int port) { create(new InetSocketAddress(port)); }

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
	public void create(String host, int port) { create(new InetSocketAddress(host, port)); }

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
	public void create(InetAddress host, int port) { create(new InetSocketAddress(host, port)); }

	/**
	 * Creates a server without starting, bound to a specific address
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
		threadGroup = new ThreadGroup(endpoint.getPort() + "/Server-Thread-Group");// Create the context

		try {
			m_context = new ServerContext(threadGroup, endpoint);
		} catch (IOException e) {
			Logger.fatal("SERVER", "Starting Exception:", e);
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
	public boolean start(int port) { return start(new InetSocketAddress(port)); }

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
	public boolean start(String host, int port) { return start(new InetSocketAddress(host, port)); }

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
	public boolean start(InetAddress host, int port) { return start(new InetSocketAddress(host, port)); }

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
		error = start();
		return !error;
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
				threadGroup,
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

	public boolean isCreated() { return m_context != null; }

	public boolean isRunning() { return isCreated() && m_context.isRunning(); }

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

		m_context.async_accept(socket -> {
			// Triggered by incoming connection request
			Logger.info("SERVER", "New Connection: (" + socket.getRemoteSocketAddress() + ")");

			Connection<T> newconn = new Connection<>(Side.Server, m_context, socket, m_qMessagesIn, messageFactory);

			// Give the server impl a chance to deny connection
			if (onClientConnect(newconn, nIDCounter)) {
				// Connection allowed, so add to container of new connections
				m_deqConnections.offerLast(newconn);

				newconn.connectToClient(this, nIDCounter++);

				Logger.info("SERVER", "(" + newconn.getID() + ") Connection Approved");
			} else {
				Logger.debug("SERVER", "Connection Denied");
			}

			// Prime the context with more work - again simply wait for another connection...
			waitForClientConnection();
		}, error ->
		// Error has occurred during acceptance
		Logger.error("SERVER", "New Connection Exception:", new AcceptingException("", error)));
	}

	/**
	 * Send a message to a specific client
	 * 
	 * @param client
	 *            The specific client
	 * @param msg
	 *            The message
	 */
	public void messageClient(Connection<T> client, MessageBuffer<T> msg) {
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
	public void messageAllClients(MessageBuffer<T> msg) { messageAllClients(msg, (Connection<T>) null); }

	/**
	 * Send a message to all clients except the ignored one
	 * 
	 * @param msg
	 *            The message
	 * @param ignoreClient
	 *            The client to ignore, null to send to everybody
	 */
	public void messageAllClients(MessageBuffer<T> msg, Connection<T> ignoreClient) {
//		System.out.println(m_deqConnections.size());
		for (Connection<T> client : m_deqConnections) {
//		for (Iterator<Connection<T>> iterator = m_deqConnections.descendingIterator(); iterator.hasNext();) {
//			Connection<T> client = iterator.next();
			// Check client is connected...
			if (client != null && client.isConnected()) {
				// Check that it's not the client we want to ignore
				if (client != ignoreClient) client.send(msg);
			} else {
				// The client couldn't be contacted, so assume it has disconnected.
				clientNotConnected(client);
			}
		}
	}

	/**
	 * Send a message to all clients except the ignored ones
	 * 
	 * @param msg
	 *            The message
	 * @param ignoreClient
	 *            The client to ignore, null to send to everybody
	 */
	public void messageAllClients(MessageBuffer<T> msg, Connection<T>... ignoreClients) {
//		System.out.println(m_deqConnections.size());
//		for (Connection<T> client : m_deqConnections) {
		for (Iterator<Connection<T>> iterator = m_deqConnections.descendingIterator(); iterator.hasNext();) {
			Connection<T> client = iterator.next();
			// Check client is connected...
			if (client != null && client.isConnected()) {
				// Check that it's not the client we want to ignore
				boolean ignore = false;
				for (Connection<T> ignoreClient : ignoreClients) if (client == ignoreClient) {
					ignore = true;
					break;
				}
				if (!ignore) client.send(msg);
			} else {
				// The client couldn't be contacted, so assume it has disconnected.
				clientNotConnected(client);
			}
		}
	}

	public void messageOnlyClients(MessageBuffer<T> msg, Connection<T>... clients) {
//		System.out.println(m_deqConnections.size());
//		for (Connection<T> client : m_deqConnections) {
		for (Iterator<Connection<T>> iterator = m_deqConnections.descendingIterator(); iterator.hasNext();) {
			Connection<T> client = iterator.next();
			// Check client is connected...
			if (client != null && client.isConnected()) {
				// Check that it's not the client we want to ignore
				boolean send = false;
				for (Connection<T> ignoreClient : clients) if (client == ignoreClient) {
					send = true;
					break;
				}
				if (send) client.send(msg);
			} else {
				// The client couldn't be contacted, so assume it has disconnected.
				clientNotConnected(client);
			}
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

	@Override
	protected void onMessage(MessageBuffer.Owned<T> msg) {
		onMessage(msg.getRemote(), msg.getMessage());
	}

	/**
	 * Called when a message arrives
	 * 
	 * @param client
	 *            The client that sent the message
	 * @param msg
	 *            The message
	 */
	protected void onMessage(Connection<T> client, MessageBuffer<T> msg) {}

	public void onClientValidated(Connection<T> client) {}

}