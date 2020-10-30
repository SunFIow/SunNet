package com.sunflow.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.sunflow.error.DisconnectException;
import com.sunflow.error.ReadMessageException;
import com.sunflow.error.WriteMessageException;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;
import com.sunflow.util.TSQueue;
import com.sunflow.util.Utils;
import com.ªtest.net.MessageBuffer;

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
//	protected TSQueue<Message<T>> m_qMessagesOut;
//	protected TSQueue<MixedMessage<T>> m_qMessagesOut;
	protected TSQueue<MessageBuffer<T>> m_qMessagesOut;

	/**
	 * This queue holds all messages that have been recieved from
	 * the remote side of this connection. Note it is a reference
	 * as the "owner" of this connection is expected to provide a queue
	 */
//	protected TSQueue<Message.Owned<T>> m_qMessagesIn;
//	protected TSQueue<MixedMessage.Owned<T>> m_qMessagesIn;
	protected TSQueue<MessageBuffer.Owned<T>> m_qMessagesIn;

	private Supplier<MessageBuffer<T>> messageFactory;

	/**
	 * A connection is "owned" by either a server or a client, and its
	 * behaviour is slightly different bewteen the two.
	 */
	protected Side m_nOwnerType;

	/**
	 * The id of this connection
	 */
	protected int id;

	/**
	 * The InputStream of this connection
	 */
	protected Callable<ObjectInputStream> inputStream;
	/**
	 * The OutputStream of this connection
	 */
	protected Callable<ObjectOutputStream> outputStream;

	protected ObjectInputStream getObjectInputStream(Socket socket) throws IOException {
		return new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
	}

	protected ObjectOutputStream getObjectOutputStream(Socket socket) throws IOException {
		return new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}

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
//	public Connection(Side parent, CommonContext m_context, Socket socket, TSQueue<Message.Owned<T>> qIn) {
//	public Connection(Side parent, CommonContext m_context, Socket socket, TSQueue<MixedMessage.Owned<T>> qIn) {
	public Connection(Side parent, CommonContext m_context, Socket socket, TSQueue<MessageBuffer.Owned<T>> qIn, Supplier<MessageBuffer<T>> messageFactory) {
		this.m_context = m_context;
		this.m_socket = socket;
		this.m_qMessagesIn = qIn;

		this.m_nOwnerType = parent;

		this.m_qMessagesOut = new TSQueue<>();
		this.messageFactory = messageFactory;
		this.id = -1;
		this.outputStream = Utils.getCachedCallable(() -> getObjectOutputStream(socket));
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
		if (m_nOwnerType == Side.Server && isConnected()) {
			id = uid;
			this.inputStream = Utils.getCachedCallable(() -> getObjectInputStream(m_socket));
			readMessage();
		}
	}

	/**
	 * Connect to a server
	 * 
	 */
	public void connectToServer() {
		if (m_nOwnerType == Side.Client && isConnected()) {
			this.inputStream = Utils.getCachedCallable(() -> getObjectInputStream(m_socket));
			readMessage();
		}
	}

	public void disconnect() {
		m_context.async_task(m_nOwnerType + "_connection_disconnect", () -> {
			m_socket.close();
//			m_context.stop();
		}, error -> {
//			error.printStackTrace();
			Logger.error(m_nOwnerType + "-Connection", "(" + id + ") couldn't disconnect:", new DisconnectException("", error));
		});

	}

	public boolean isConnected() {
		return !m_socket.isClosed() && m_socket.isConnected();
	}

	/**
	 * @ASYNC Send a message, connections are one-to-one so no need to specifiy
	 *        the target, for a client, the target is the server and vice versa
	 */
//	public void send(Message<T> msg) {
//	public void send(MixedMessage<T> msg) {
	public void send(MessageBuffer<T> msg) {
		m_context.post(m_nOwnerType + "_connection_send", () -> {
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
		m_context.async_write(m_socket, m_qMessagesOut.front(), (wroteBytes) -> {
			Logger.help(Thread.currentThread(), "Wrote Message of length " + wroteBytes + ": " + m_qMessagesOut.front());
			// A complete message has been written
			m_qMessagesOut.pop_front();
			if (!m_qMessagesOut.empty()) {
				writeMessage();
			}
		}, (error) -> {
			// Something is wrong with this connection...
			Logger.error(m_nOwnerType + "-Connection", "(" + id + ") couldn't write message:", new WriteMessageException("", error));
			// ... so disconnect it
			disconnect();
		});
	}

	/**
	 * @ASYNC Prime context ready to read a message
	 */
	private void readMessage() {
//		m_context.async_read(inputStream, (Message<T> msg) -> {
//		MixedMessage<T> msg = new MixedMessage<>();
		MessageBuffer<T> msg = messageFactory.get();
		m_context.async_read(m_socket, msg, (readBytes) -> {
			// A complete message has been read
			Logger.help(Thread.currentThread(), "Read Message of length " + readBytes + ": " + msg);
			addToIncomingMessageQueue(msg);
			readMessage();
		}, (error) -> {
			// Something is wrong with this connection...
			Logger.error(m_nOwnerType + "-Connection", "(" + id + ") couldn't read message:" + new ReadMessageException(error));
			// ... so disconnect it
			disconnect();
		});
	}

//	/**
//	 * @ASYNC Prime context ready to read a message
//	 */
//	private void readMessage() {
//		m_context.async_read(inputStream, (Message<T> msg) -> {
//			// A complete message has been read
//			Logger.debug(Thread.currentThread(), "Read Message: " + msg);
//			addToIncomingMessageQueue(msg);
//			readMessage();
//		}, (error) -> {
//			// Something is wrong with this connection...
//			Logger.error(m_nOwnerType + "-Connection", "(" + id + ") couldn't read message:" + new ReadMessageException(error));
//			// ... so disconnect it
//			disconnect();
//
//		});
//	}

//	private void addToIncomingMessageQueue(Message<T> msg) {
//	private void addToIncomingMessageQueue(MixedMessage<T> msg) {
	private void addToIncomingMessageQueue(MessageBuffer<T> msg) {
//		m_qMessagesIn.push_back(new Message.Owned<T>(m_nOwnerType == Side.Server ? this : null, msg)); 
//		m_qMessagesIn.push_back(new MixedMessage.Owned<T>(m_nOwnerType == Side.Server ? this : null, msg));
		m_qMessagesIn.push_back(new MessageBuffer.Owned<T>(m_nOwnerType == Side.Server ? this : null, msg));
	}
}
