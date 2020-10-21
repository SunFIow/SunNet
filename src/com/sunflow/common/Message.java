package com.sunflow.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Iterator;

public class Message<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 5130385395145010707L;

	/**
	 * Message Header is sent at start of all messages. The template allows us
	 * to use "enum class" to ensure that the messages are valid a compile time
	 */

	private T id;
	private ArrayDeque<Serializable> body;

	public Message() { body = new ArrayDeque<>(); }

	public Message(T id) {
		this();
		this.id = id;
	}

	/**
	 * @return the id of this message
	 */
	public T id() { return id; }

	/**
	 * @return the number of elements in the body
	 */
	public int size() { return body.size(); }

	/**
	 * @return the data of this message
	 */
	public ArrayDeque<Serializable> data() { return body; }

	/**
	 * Set the id of this message
	 * 
	 * @param id
	 */
	public void setID(T id) { this.id = id; }

	/**
	 * Set the data of this message
	 * 
	 * @param data
	 */
	public void setData(ArrayDeque<Serializable> data) { this.body = data; }

	@Override
	public String toString() {
		StringBuilder content = new StringBuilder();
		if (body.size() > 0) {
			for (Iterator<Serializable> iterator = body.iterator(); iterator.hasNext();) {
				content.append(iterator.next());
				if (iterator.hasNext()) content.append(", ");
			}
			return "ID{" + id + "}, Body{ Size[" + body.size() + "], Content[" + content + "] }";
		}
		return "ID{" + id + "}, Body{ Size[0], Content[] }";
	}

	/**
	 * 
	 * @return the size of this message in bytes
	 */
	public int byteSize() {
		int size = -1;
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			size = baos.size();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null) try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return size;
	}

	/**
	 * Add any Serializable data onto the end of the message buffer
	 */
	public <DataType extends Serializable> Message<T> add(DataType data) { return push(data); }

	/**
	 * Add any number of Serializable data onto the end of the message buffer
	 */
	public <DataType extends Serializable> Message<T> add(@SuppressWarnings("unchecked") DataType... data) { return push(data); }

	/**
	 * Pushes any number of Serializable data onto the end of the message buffer
	 */
	public <DataType extends Serializable> Message<T> push(@SuppressWarnings("unchecked") DataType... data) { for (DataType d : data) push(d); return this; }

	/**
	 * Pushes any Serializable data onto the end of the message buffer
	 */
	public <DataType extends Serializable> Message<T> push(DataType data) {
		// Add the data to the body
		body.add(data);
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
