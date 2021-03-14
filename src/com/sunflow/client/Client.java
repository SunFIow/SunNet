package com.sunflow.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.sunflow.common.Connection;
import com.sunflow.common.Interface;
import com.sunflow.error.ConnectingException;
import com.sunflow.message.MessageBuffer;
import com.sunflow.message.PacketBuffer;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;
import com.sunflow.util.TSQueue;

/**
 * @param <T>
 *            The type of messages
 */
public class Client<T> extends Interface<T> {

	/**
	 * If the client is destroyed, always try and disconnect from server
	 */
	@Override
	public void close() {
		Logger.debug("CLIENT", "close()");
		disconnect();
	}

	/**
	 * The client has a single instance of a "connection" object,
	 * which handles data transfer
	 */
	protected Connection<T> m_connection;

	public Client() { this(MessageBuffer::new); }

	public Client(Supplier<MessageBuffer<T>> messageFactory) {
		super(messageFactory);

		this.m_qMessagesIn = new TSQueue<>();
	}

	/**
	 * Connect to server
	 * 
	 * @param host
	 *            The hostname/ip-address of the server
	 * @param port
	 *            The port number of the server, between 0 and 65535
	 */

	public boolean connect(String host, int port) {
		// Resolve hostname/ip-address into tangible physical address
		InetSocketAddress serverEndpoint = new InetSocketAddress(host, port);
		return connect(serverEndpoint);
	}

	/**
	 * Connect to server
	 * 
	 * @param host
	 *            The ip-address of the server
	 * @param port
	 *            The port number of the server, between 0 and 65535
	 */

	public boolean connect(InetAddress host, int port) {
		// Resolve hostname/ip-address into tangible physical address
		InetSocketAddress serverEndpoint = new InetSocketAddress(host, port);
		return connect(serverEndpoint);
	}

	/**
	 * Connect to server
	 * 
	 * @param endpoint
	 *            InetSocketAddress of the server, between 0 and 65535
	 */
	public boolean connect(InetSocketAddress endpoint) {
		Logger.info("CLIENT", "Connecting...");

		threadGroup = new ThreadGroup(endpoint + "/Client-Thread-Group");

		// Create the context
		m_context = new ClientContext(threadGroup);

		AtomicBoolean connectionSucess = new AtomicBoolean();
		m_context.connect(endpoint, socket -> {
			connectionSucess.set(true);
//			SocketAddress clientEndpoint = socket.getLocalSocketAddress();
			Logger.info("CLIENT", "Succesfully conntected to (" + endpoint + ")");
			m_connection = new Connection<>(Side.Client, m_context, socket, m_qMessagesIn, messageFactory);
			m_connection.connectToServer();
		}, error -> Logger.error("CLIENT", new ConnectingException("", error)));

		// Start Context Thread
		m_threadContext = new Thread(
				threadGroup,
				m_context::run,
				"ClientContext");
		m_threadContext.start();

		return connectionSucess.get();
	}

	/**
	 * Disconnect from the server
	 */
	@SuppressWarnings("deprecation")
	public boolean disconnect() {
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
			return true;
		} catch (InterruptedException e) {
			Logger.error("CLIENT", Thread.currentThread() + " got interrupted while waiting for " + m_threadContext + " to die", e);
			return false;
		}
	}

	/**
	 * Check if client is actually connected to a server
	 */
	public boolean isConnected() {
		return m_connection != null && m_connection.isConnected();
	}

	public void send(PacketBuffer msg) {
		if (isConnected()) m_connection.send(msg);
	}

	@Override
	protected void onMessage(MessageBuffer.Owned<T> msg) {
		onMessage(msg.getMessage());
	}

	/**
	 * Called when a message arrives
	 * 
	 * @param msg
	 *            The message
	 */
	protected void onMessage(MessageBuffer<T> msg) {}

}
