package com.sunflow.common;

import java.io.Serializable;
import java.util.ArrayDeque;

public class Message<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 5130385395145010707L;

	/**
	 * Message Header is sent at start of all messages. The template allows us
	 * to use "enum class" to ensure that the messages are valid a compile time
	 */
	public static class Header<T> implements Serializable {
		private static final long serialVersionUID = -2532418061029867801L;

		public T id = null;
		public int bodySize = 0;

		@Override
		public String toString() {
			return "ID: " + id + ", Size:" + bodySize;
		}
	}

	private Header<T> header;
	private ArrayDeque<Serializable> body;

	public Message() {
		header = new Header<>();
		body = new ArrayDeque<>();
	}

	public Message(T id) {
		this();
		this.header.id = id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Object o : body) builder.append(o);
		String s = builder.toString();
		return "Header{" + header + "}" + "\n" +
				"Body{" + s + "}";
	}

	/**
	 * @return the id of this message
	 */
	public T id() {
		return header.id;
	}

	/**
	 * @return the number of elements in the body
	 */
	public int size() {
		return body.size();
	}

	/**
	 * @return the header of this message
	 */
	public Header<T> header() {
		return header;
	}

	/**
	 * @return the data of this message
	 */
	public ArrayDeque<Serializable> data() {
		return body;
	}

	/**
	 * Set the header of this message
	 * 
	 * @param header
	 */
	public void setHeader(Header<T> header) {
		this.header = header;
	}

	/**
	 * Set the data of this message
	 * 
	 * @param data
	 */
	public void setData(ArrayDeque<Serializable> data) {
		body = data;
	}

	/**
	 * Add any Serializable data onto the end of the message buffer
	 */
	public <DataType extends Serializable> Message<T> add(DataType data) { return push(data); }

	/**
	 * Add any number of Serializable data onto the end of the message buffer
	 */
	public <DataType extends Serializable> Message<T> add(@SuppressWarnings("unchecked") DataType... data) { return push(data); }

//	/**
//	 * Adds any array of Serializable data onto the end of the message buffer
//	 */
//	public <DataType extends Serializable> Message<T> addArray(Serializable data) { return pushArray(data); }

	/**
	 * Pushes any number of Serializable data onto the end of the message buffer
	 */
	public <DataType extends Serializable> Message<T> push(@SuppressWarnings("unchecked") DataType... data) { for (DataType d : data) push(d); return this; }

//	/**
//	 * Pushes any array of Serializable data onto the end of the message buffer
//	 */
//	public <DataType extends Serializable> Message<T> pushArray(DataType data) { return push(data); }

	/**
	 * Pushes any Serializable data onto the end of the message buffer
	 */
	public <DataType extends Serializable> Message<T> push(DataType data) {
		// Add the data to the body
		body.add(data);

		// Recalculate the message size
		header.bodySize = size();

		// Return the target message so it can be "chained"
		return this;
	}

	/**
	 * @return the data from at the front of the message
	 */
	public <DataType extends Serializable> DataType remove() { return pop(); }

	/**
	 * @return the data from at the front of the message
	 */
	public <DataType extends Serializable> DataType pop() {
		@SuppressWarnings("unchecked")
		// Retrieves and remove the first element of the body
		DataType data = (DataType) body.pollFirst();

		// Recalculate the message size
		header.bodySize = size();

		// Return the data
		return data;
	}

	/**
	 * @return a boolean from the front of the message
	 */
	public boolean popBoolean() { return pop(); }

	/**
	 * @return a byte from the front of the message
	 * @throws ClassCastException
	 *             if the popped object is not a byte
	 */
	public byte popByte() { return pop(); }

	/**
	 * @return a char from the front of the message
	 * @throws ClassCastException
	 *             if the popped object is not a char
	 */
	public char popCharacter() { return pop(); }

	/**
	 * @return a short from the front of the message
	 * @throws ClassCastException
	 *             if the popped object is not a short
	 */
	public short popShort() { return pop(); }

	/**
	 * @return a int from the front of the message
	 * @throws ClassCastException
	 *             if the popped object is not a int
	 */
	public int popInteger() { return pop(); }

	/**
	 * @return a long from the front of the message
	 * @throws ClassCastException
	 *             if the popped object is not a long
	 */
	public long popLong() { return pop(); }

	/**
	 * @return a float from the front of the message
	 * @throws ClassCastException
	 *             if the popped object is not a float
	 */
	public float popFloat() { return pop(); }

	/**
	 * @return a double from the front of the message
	 * @throws ClassCastException
	 *             if the popped object is not a double
	 */
	public double popDouble() { return pop(); }

	/**
	 * @return a string from the front of the message
	 * @throws ClassCastException
	 *             if the popped object is not a string
	 */
	public String popString() { return pop(); }

	public static class Owned<T extends Serializable> {

		private Connection<T> remote;
		private Message<T> msg;

		public Owned(Connection<T> connection, Message<T> m_msgTemporaryIn) {
			this.remote = connection;
			this.msg = m_msgTemporaryIn;
		}

		@Override
		public String toString() {
			return getMessage().toString();
		}

		public Connection<T> getRemote() { return remote; }

		public Message<T> getMessage() { return msg; }
	}
}
