package com.sunflow.common;

import java.io.IOException;
import java.net.Socket;

import com.sunflow.common.Message.Header;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;
import com.sunflow.util.TSQueue;

public class Connection<T> {

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
			readHeader();
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
			readHeader();
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
			if (!bWritingMessage) writeHeader();
		});
	}

	/**
	 * @ASYNC Prime context ready to read a message header
	 */
	private void readHeader() {
		m_context.async_read(m_socket,
//				m_msgTemporaryIn,
				(Exception error, Integer length, Header<T> header) -> {
					if (error == null) {
						Logger.debug(Thread.currentThread(), "Read Header (" + length + ") / " + (header != null ? header.getClass() + " - " + header : "NULL"));
						Message<T> m_msgTemporaryIn = new Message<>();
						// A complete message header has been read
						m_msgTemporaryIn.header = header;
						// Check if this message has a body to follow...
						if (m_msgTemporaryIn.header.bodySize > 0) {
							// ...it does, so instruct the task to read the body.
							readBody(m_msgTemporaryIn);
						} else {
							addToIncomingMessageQueue(m_msgTemporaryIn);
							readHeader();
						}
					} else {
						// Check if we got an Error because the Socket isn't connected anymore
						Logger.error("(" + id + ") Read Header Exception: " + error);
//						disconnect();
						try {
							m_socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * @ASYNC Prime context ready to read a message body
	 */
	private void readBody(Message<T> msgWithHeader) {
		m_context.async_read(m_socket,
//							m_msgTemporaryIn,
				(Exception error, Integer length, byte[] body) -> {
					if (error == null) {
						Logger.debug(Thread.currentThread(), "Read Body (" + length + ") / " + (body != null ? body.getClass() + " - " + body : "NULL"));
						// A complete message body has been read
						msgWithHeader.body = body;
						addToIncomingMessageQueue(msgWithHeader);
						readHeader();
					} else {
						Logger.error("(" + id + ") Read Body Exception:", error);
//						disconnect();
						try {
							m_socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * @ASYNC Prime context ready to write a message header
	 */
	private void writeHeader() {
		m_context.async_write(m_socket,
				m_qMessagesOut.front().header, m_qMessagesOut.front().sizeHeader(),
				(error, length) -> {
					Logger.debug(Thread.currentThread(), "Wrote Header: " + length);
					if (error == null) {
						// A complete message header has been written, check if this message
						// has a body to follow...
						if (m_qMessagesOut.front().sizeBody() > 0) {
							// ...it does, so instruct the task to write the body.
							writeBody();
						} else {
							m_qMessagesOut.pop_front();
							if (!m_qMessagesOut.empty()) {
								writeHeader();
							}
						}
					} else {
						Logger.error("(" + id + ") Write Header Exception:", error);
						try {
							m_socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * @ASYNC Prime context ready to write a message body
	 */
	private void writeBody() {
		m_context.async_write(m_socket,
				m_qMessagesOut.front().body, m_qMessagesOut.front().sizeBody(),
				(error, length) -> {
					Logger.debug(Thread.currentThread(), "Wrote Body: " + length);
					if (error == null) {
						m_qMessagesOut.pop_front();
						if (!m_qMessagesOut.empty()) {
							writeHeader();
						}
					} else {
						Logger.error("(" + id + ") Write Body Exception:", error);
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
