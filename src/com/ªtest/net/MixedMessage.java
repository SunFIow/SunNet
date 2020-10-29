package com.ªtest.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import com.sunflow.common.Connection;
import com.sunflow.util.Logger;

import io.netty.buffer.ByteBuf;

public class MixedMessage<T extends Serializable> extends PacketBuffer {
	public static class Owned<T extends Serializable> {

		private Connection<T> remote;
		private MixedMessage<T> msg;

		public Owned(Connection<T> connection, MixedMessage<T> m_msgTemporaryIn) {
			this.remote = connection;
			this.msg = m_msgTemporaryIn;
		}

		@Override
		public String toString() {
			return getMessage().toString();
		}

		public Connection<T> getRemote() { return remote; }

		public MixedMessage<T> getMessage() { return msg; }
	}

	@Override
	public String toString() {
		return "MixedMessage<" + id + ">{" + super.toString() + "}";
	}

	private T id;

	public MixedMessage() { super(); }

	public MixedMessage(T id) { this(); this.id = id; }

	public MixedMessage(MixedMessage<T> origin) { super(origin); }

	public MixedMessage(PacketBuffer origin) { super(origin); }

	public MixedMessage(ByteBuf wrapped) { super(wrapped); }

	public T getID() { return id; }

	public MixedMessage<T> setID(T id) { this.id = id; return this; }

	public int headerSize() throws IOException {
		ByteArrayOutputStream raw = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(raw);
		out.writeObject(id);
		out.writeInt(0);
		out.close();
		return raw.size();
	}

	// * @return data size in bytes written out to the specified stream
	/**
	 * Write this message out to the specified stream
	 * 
	 * 
	 * @return the size of this message in bytes written to the specified stream
	 */
	@Override
	public int write(OutputStream out) throws IOException {
		BufferedOutputStream bout = new BufferedOutputStream(out);
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(id);
		int dataSize = writerIndex();
		oout.writeInt(dataSize);
		oout.flush();

		if (dataSize > 0) {
			getBytes(0, bout, dataSize);
			bout.flush();
		}

		return headerSize() + dataSize;
	}

	/**
	 * Reads in the message from the InputStream
	 * 
	 * @return the data size
	 * 
	 * @throws IOException
	 *             if the specified stream threw an exception during I/O
	 * @throws ClassNotFoundException
	 *             Class of a serialized object cannot befound.
	 * @throws ClassCastException
	 *             Class of a serialized object is not this message's type.
	 */
	@Override
	public int read(InputStream in) throws IOException, ClassNotFoundException, ClassCastException {
		System.out.println("Read Avaiable: " + in.available());
		BufferedInputStream bin = new BufferedInputStream(in);
		// Read in the message header
		int dataSize = readHeader(bin);

		// Read int the message data if there
		if (dataSize > 0) writeBytes(bin, dataSize);

		Logger.debug("Read Data Successfully");
		// Return the data size
		return dataSize;
	}

	/**
	 * Reads in the message header from the Inputstream
	 * 
	 * @return the data size
	 * @throws IOException
	 *             if the specified stream threw an exception during I/O
	 * @throws ClassNotFoundException
	 *             Class of a serialized object cannot befound.
	 * @throws ClassCastException
	 *             Class of a serialized object is not this message's type.
	 */
	@SuppressWarnings("unchecked")
	private int readHeader(InputStream in) throws IOException, ClassNotFoundException, ClassCastException {
		System.out.println("Read Avaiable: " + in.available());
		ObjectInputStream oin = new ObjectInputStream(in);
		System.out.println("Read Avaiable: " + in.available());
		id = (T) oin.readObject();
		System.out.println("Read Avaiable: " + in.available());
		int dataSize = oin.readInt();
		System.out.println("Read Avaiable: " + in.available());

		readerIndex(0);
		writerIndex(0);
		return dataSize;
	}
}
