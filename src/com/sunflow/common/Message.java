package com.sunflow.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class Message<T> {
	private static byte BYTE_SIZE_INT = 6;

	public enum MessageTypes {
		PING,
		FireBullet,
		MovePlayer;

		private void writeObject(ObjectOutputStream out) throws IOException {
			System.out.println("TEST");
			out.defaultWriteObject();
		}
	}

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

	public Header<T> header;
	public byte[] body;

	public Message() {
		header = new Header<>();
		body = new byte[0];
	}

	public Message(T id) {
		this();
		header.id = id;
	}

	/**
	 * @return size of header and body combined
	 */
	public int sizeFully() {
		return sizeHeader() + sizeBody();
	}

	/**
	 * @return size of body
	 */
	public int size() {
		return sizeBody();
	}

	/**
	 * @return size of header
	 */
	private Header<T> cachedHeader;
	private int cachedHeaderSize = -1;

	public int sizeHeader() {
		if (header != cachedHeader || cachedHeaderSize < 0)
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(header);
				oos.close();
				cachedHeaderSize = bos.size();
				cachedHeader = header;
			} catch (IOException e) {
				e.printStackTrace();
			}
		return cachedHeaderSize;
	}

	/**
	 * @return size of body
	 */
	public int sizeBody() { return body.length; }

	public void resize(int newBodySize) {
		body = Arrays.copyOf(body, newBodySize);
	}

	public void resize(int from, int to) {
		body = Arrays.copyOfRange(body, from, to);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (byte b : body) builder.append(b);
		String s = builder.toString();
		return "Header{" + header.toString() + "}" + "\n" +
				"Body{" + s + "}";
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
		byte[] bytes;

		// Serialize the data
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);

			// Write the data into the stream
			oos.writeObject(data);

			// Write the sizeof the data into the stream
			oos.writeInt(bos.size());
			oos.flush();

			// Get the bytes of the data + sizeof the data
			bytes = bos.toByteArray();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return this;
		}

		// Cache current size of vector, as this will be the point we insert the data
		int i = body.length;

		// Resize the vector by the size of the data being pushed
		resize(i + bytes.length);

		// Copy the data into the newly allocated body space
//		for (int j = 0; j < bytes.length; j++) body[i + j] = bytes[j];
		System.arraycopy(bytes, 0, body, i, bytes.length);

		// Recalculate the message size
		header.bodySize = sizeBody();

		// Return the target message so it can be "chained"
		return this;
	}

	/**
	 * @return data from the front of the message
	 */
	public <DataType> DataType remove() { return pop(); }

	/**
	 * @return data from the front of the message
	 */
	public <DataType> DataType pop() {
		int dataSize = -1;

		// Deserialize the data
		Object o;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(body);
			ObjectInputStream ois = new ObjectInputStream(bis);

			// Read the data out of the stream
			o = ois.readObject();

			// Read the size of the data out of the stream
			dataSize = ois.readInt();

			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		// Cache the location towards the end of the vector where the pulled data starts
		int i = body.length;

		// Shrink the array to remove read bytes
		resize(dataSize + BYTE_SIZE_INT, i);

		// Recalculate the message size
		header.bodySize = sizeBody();

		// Return the data
		@SuppressWarnings("unchecked")
		DataType data = (DataType) o;
		return data;
	}

	/**
	 * Remove data from the front of the message and store it in the wrapper
	 */
	public <DataType> Message<T> remove(Wrapper<DataType> wrapper) { return pop(wrapper); }

	/**
	 * Pop data from the front of the message and store it in the wrapper
	 */
	public <DataType> Message<T> pop(Wrapper<DataType> wrapper) {
		// Store popped data in the wrapper
		wrapper.set(pop());

		// Return the target message so it can be "chained"
		return this;
	}

	/**
	 * @return a boolean from the front of the message
	 * @throws ClassCastException
	 *             if the popped object is not a boolean
	 */
	public boolean popBoolean() throws ClassCastException { return pop(); }

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

	public static class Owned<T> {

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

	public static class Wrapper<DataType> {
		public DataType val;

		public void set(DataType val) { this.val = val; }

		public DataType get() { return val; }
	}

}
