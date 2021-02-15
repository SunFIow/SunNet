package com.sunflow.common;

import java.net.Socket;
import java.util.function.Supplier;

import com.sunflow.error.DisconnectException;
import com.sunflow.error.ReadMessageException;
import com.sunflow.error.ValidationException;
import com.sunflow.error.WriteMessageException;
import com.sunflow.message.MessageBuffer;
import com.sunflow.message.PacketBuffer;
import com.sunflow.server.Server;
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
	protected TSQueue<PacketBuffer> m_qMessagesOut;

	/**
	 * This queue holds all messages that have been recieved from
	 * the remote side of this connection. Note it is a reference
	 * as the "owner" of this connection is expected to provide a queue
	 */
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
	protected int id = -1;

	private long m_nHandshakeOut = 0;
	private long m_nHandshakeIn = 0;
	private long m_nHandshakeCheck = 0;

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
	public Connection(Side parent, CommonContext m_context, Socket socket, TSQueue<MessageBuffer.Owned<T>> qIn, Supplier<MessageBuffer<T>> messageFactory) {
		this.m_context = m_context;
		this.m_socket = socket;
		this.m_qMessagesIn = qIn;

		this.m_nOwnerType = parent;

		this.m_qMessagesOut = new TSQueue<>();
		this.messageFactory = messageFactory;

		// Construct validation check data
		if (m_nOwnerType == Side.Server) {
			// Connection is Server -> Client, construct random data for the client
			// to transform and send back for validation
			m_nHandshakeOut = System.currentTimeMillis();

			// Pre-calculate the result for checking when the client responds
			m_nHandshakeCheck = scramble(m_nHandshakeOut);
		} else {
			// Connection is Client -> Server, so we have nothing to define

		}
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
	public void connectToClient(Server<T> server, int uid) {
		if (m_nOwnerType == Side.Server && isConnected()) {
			id = uid;
//			Was: readMessage();

			// A client has attempted to connect to the server, but we wish
			// the client to first validate itself, so first write out the
			// handshake data to be validated
			writeValidation();

			// Next, issue a task to sit and wait asynchronously for precisely
			// the validation data sent back from the client
			readValidation(server);
		}
	}

	/**
	 * Connect to a server
	 * 
	 */
	public void connectToServer() {
		if (m_nOwnerType == Side.Client && isConnected()) {
//			Was: readMessage();

			// First thing server will do is send packet to be validated
			// so wait for that and respond
			readValidation();
		}
	}

	public void disconnect() {
		m_context.async_task(m_nOwnerType + "_connection_disconnect", () -> {
			m_socket.close();
//			m_context.stop();
		}, error -> Logger.error(m_nOwnerType + "-Connection", "(" + id + "): ", new DisconnectException("", error)));
	}

	public boolean isConnected() { return !m_socket.isClosed() && m_socket.isConnected(); }

	/**
	 * @ASYNC Send a message, connections are one-to-one so no need to specifiy
	 *        the target, for a client, the target is the server and vice versa
	 */
	public void send(PacketBuffer msg) {
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
			Logger.error(m_nOwnerType + "-Connection", "(" + id + "): ", new WriteMessageException("", error));
			// ... so disconnect it
			disconnect();
		});
	}

	/**
	 * @ASYNC Prime context ready to read a message
	 */
	private void readMessage() {
		MessageBuffer<T> msg = messageFactory.get();
//		MessageBuffer<T> msg = new MessageBuffer<>();
		m_context.async_read(m_socket, msg, (readBytes) -> {
			// A complete message has been read
			Logger.help(Thread.currentThread(), "Read Message of length " + readBytes + ": " + msg);
			addToIncomingMessageQueue(msg);
			readMessage();
		}, (error) -> {
			// Something is wrong with this connection...
			Logger.error(m_nOwnerType + "-Connection", "(" + id + "): " + new ReadMessageException(error));
			// ... so disconnect it
			disconnect();
		});
	}

	private void addToIncomingMessageQueue(MessageBuffer<T> msg) {
		m_qMessagesIn.push_back(new MessageBuffer.Owned<T>(m_nOwnerType == Side.Server ? this : null, msg));
	}

	// "Encrypt" data (8 bytes)
	private long scramble(long nInput) {
		long out = nInput ^ 0xDEADBEEFC0DECAFEL;
		out = (out & 0xF0F0F0F0F0F0F0F0L) >> 4 | (out & 0x0F0F0F0F0F0F0F0FL) << 4;
		return out ^ 0xC0DEFACE12345678L;
	}

	// ASYNC - Used by both client and server to write validation packet
	private void writeValidation() {
		PacketBuffer val_msg = new PacketBuffer();
		val_msg.writeLong(m_nHandshakeOut);
		m_context.async_write(m_socket, val_msg, (wroteBytes) -> {
			// Validation data sent, clients should sit and wait
			// for a response (or a closure)
			if (m_nOwnerType == Side.Client) readMessage();
		}, (error) -> {
			// Something went wrong while validating...
			Logger.error(m_nOwnerType + "-Connection", "(" + id + "): " + new ValidationException(error));
			// ... so disconnect it
			disconnect();
		});
	}

	private void readValidation() { readValidation(null); }

	// ASYNC - Used by both client and server to read validation packet
	private void readValidation(Server<T> server) {
		m_context.async_read(m_socket, Long.BYTES + 20, (val_msg, readBytes) -> {
			m_nHandshakeIn = val_msg.readLong();
			if (m_nOwnerType == Side.Server) {
				if (m_nHandshakeIn == m_nHandshakeCheck) {
					// Client has provided valid solution, so allow it to connect properly
					Logger.help("Client Validated");
					server.onClientValidated(this);

					// Sit waiting to receive data now
					readMessage();
				} else {
					// Client gave incorrect data, so disconnect
					Logger.help("Client Disconnected (Fail Validation)");
					// ... so disconnect it
					disconnect();
				}
			} else {
				// Connection is a client, so solve puzzle
				m_nHandshakeOut = scramble(m_nHandshakeIn);

				// Write the result
				writeValidation();
			}
		}, (error) -> {
			// Something went wrong while validating...
			Logger.error(m_nOwnerType + "-Connection", "(" + id + "): " + new ValidationException(error));
			// ... so disconnect it
			disconnect();
		});
	}
}
