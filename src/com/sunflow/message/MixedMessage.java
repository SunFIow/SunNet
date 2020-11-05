package com.sunflow.message;

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

public class MixedMessage<T extends Serializable> extends MessageBuffer<T> {
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

	public MixedMessage() { super(); }

	public MixedMessage(T id) { this(); this.id = id; }

	public MixedMessage(MixedMessage<T> origin) { super(origin); }

	public MixedMessage(PacketBuffer origin) { super(origin); }

	public MixedMessage(ByteBuf wrapped) { super(wrapped); }

	@Override
	public T getID() { return id; }

	@Override
	public MixedMessage<T> setID(T id) { this.id = id; return this; }

	@Override
	public int idSize() { throw new UnsupportedOperationException(); }

	@Override
	public int headerSize() {
		try {
			ByteArrayOutputStream raw = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(raw);
			out.writeObject(id);

			out.writeInt(0);
			out.close();
			return raw.size();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * @return byte size of the message data
	 */
	@Override
	int writeHeader(OutputStream out) throws IOException {
		ObjectOutputStream oout = new ObjectOutputStream(out);
		oout.writeObject(id);
		int dataSize = writerIndex();
		oout.writeInt(dataSize);
		oout.flush();
		return headerSize();
	}

	/**
	 * Reads in the message header from the Inputstream
	 * 
	 * @return the data size
	 * @throws IOException
	 *             if the specified stream threw an exception during I/O
	 */
	@Override
	@SuppressWarnings("unchecked")
	int readHeader(InputStream in) throws IOException {
		ObjectInputStream oin = new ObjectInputStream(in);
		try {
			id = (T) oin.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		Logger.debug("Header arrived");

		int dataSize = oin.readInt();

		readerIndex(0);
		writerIndex(0);
		return dataSize;
	}

	@Override
	protected PacketBuffer writeID(PacketBuffer idbuffer) throws IOException { throw new UnsupportedOperationException(); }

	@Override
	protected void _readID() throws IOException { throw new UnsupportedOperationException(); }
}
