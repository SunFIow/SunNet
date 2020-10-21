package com.sunflow.common;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

import com.sunflow.util.Logger;
import com.sunflow.util.Side;
import com.sunflow.util.TSQueue;

public class Connection<T extends Serializable> {

	/**
	 * Each connection has a unique socket to a remote
	 */
	protected Socket m_socket;

	/**
	 * This context is shared with the whole instance
	 */
	protected CommonContext m_context;

	/**
	 * This queue holds all mesages to be sent to the remote side
	 * of this connection
	 */
	protected TSQueue<Message<T>> m_qMessagesOut;

	/**
	 * This queue holds all messages that have been recieved from
	 * the remote side of this connection. Note it is a reference
	 * as the "owner" of this connection is expected to provide a queue
	 */
	protected TSQueue<Message.Owned<T>> m_qMessagesIn;

	/**
	 * A connection is "owned" by either a server or a client, and its
	 * behaviour is slightly different bewteen the two.
	 */
	protected Side m_nOwnerType;

	/**
	 * 
	 */
	protected int id;

	/**
	 * Specify Owner, connect to context, transfer the socket
	 * Provide reference to incoming message queue
	 * 
	 * @param parent
	 *            Owner of this connection
	 * @param threadContext
	 * @param socket
	 * @param qIn
	 */
	public Connection(Side parent, CommonContext m_context, Socket socket, TSQueue<Message.Owned<T>> qIn) {
		this.m_context = m_context;
		this.m_socket = socket;
		this.m_qMessagesIn = qIn;

		this.m_nOwnerType = parent;

//		this.m_msgTemporaryIn = new Message<>();
		this.m_qMessagesOut = new TSQueue<>();
		this.id = -1;
	}

	/**
	 * @return the connections unique id
	 */
	public int getID() { return id; }

	/**
	 * Connect to a client
	 * 
	 * @param uid
	 *            the unique id for this connection
	 */
	public void connectToClient(int uid) {
		if (m_nOwnerType == Side.server && isConnected()) {
			id = uid;
			readMessage();
		}
	}

	/**
	 * Connect to a server
	 * 
	 * @param uid
	 *            the unique id for this connection
	 */
	public void connectToServer() {
		if (m_nOwnerType == Side.client && isConnected()) {
			readMessage();
		}
	}

	public void disconnect() {
		m_context.async_post(m_nOwnerType + "_connection_disconnect", () -> {
			try {
				m_socket.close();
//				m_context.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public boolean isConnected() {
		return !m_socket.isClosed() && m_socket.isConnected();
	}

	/**
	 * @ASYNC Send a message, connections are one-to-one so no need to specifiy
	 *        the target, for a client, the target is the server and vice versa
	 */
	public void send(final Message<T> msg) {
		m_context.async_post(m_nOwnerType + "_connection_send", () -> {
			/*
			 * If the queue has a message in it, then we must
			 * assume that it is in the process of asynchronously being written.
			 * Either way add the message to the queue to be output. If no messages
			 * were available to be written, then start the process of writing the
			 * message at the front of the queue.
			 */
			boolean bWritingMessage = !m_qMessagesOut.empty();
			m_qMessagesOut.push_back(msg);
			if (!bWritingMessage) writeMessage();
		});
	}

	/**
	 * @ASYNC Prime context ready to write a message
	 */
	private void writeMessage() {
		m_context.async_write(m_socket, m_qMessagesOut.front(),
				(error) -> {
					Logger.debug(Thread.currentThread(), "Wrote Message: " + m_qMessagesOut.front());
					if (error == null) {
						// A complete message has been written
						m_qMessagesOut.pop_front();
						if (!m_qMessagesOut.empty()) {
							writeMessage();
						}
					} else {
						Logger.error("(" + id + ") Write Message Exception:", error);
						try {
							m_socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * @ASYNC Prime context ready to read a message
	 */
	private void readMessage() {
		m_context.async_read(m_socket,
				(Exception error, Message<T> msg) -> {
					if (error == null) {
						// A complete message has been read
						Logger.debug(Thread.currentThread(), "Read Message: " + msg);
						addToIncomingMessageQueue(msg);
						readMessage();
					} else {
						// Check if we got an Error because the Socket isn't connected anymore
						Logger.error("(" + id + ") Read Message Exception: " + error);
//						disconnect();
						try {
							m_socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
	}

	private void addToIncomingMessageQueue(Message<T> msg) {
		if (m_nOwnerType == Side.server) {
			m_qMessagesIn.push_back(new Message.Owned<T>(this, msg));
		} else {
			m_qMessagesIn.push_back(new Message.Owned<T>(null, msg));
		}
	}
}
