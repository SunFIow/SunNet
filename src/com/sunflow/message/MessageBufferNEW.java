package com.sunflow.message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.SocketException;
import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;

import com.sunflow.common.Connection;
import com.sunflow.error.ReadMessageException;
import com.sunflow.util.Logger;

import io.netty.buffer.ByteBuf;

public class MessageBufferNEW extends MessageBuffer<Object> {
	private static final byte T_BOOL = 100;
	private static final byte T_BYTE = 101;
	private static final byte T_SHORT = 102;
	private static final byte T_INT = 103;
	private static final byte T_LONG = 104;
	private static final byte T_FLOAT = 105;
	private static final byte T_DOUBLE = 106;
	private static final byte T_CHAR = 107;
	private static final byte T_STRING = 108;
	private static final byte T_TIME = 109;
	private static final byte T_UUID = 110;
	private static final byte T_ENUM = 111;
	private static final byte T_GEN = 112;

	public static class Owned {

		private Connection remote;
		private MessageBufferNEW msg;

		public Owned(Connection connection, MessageBufferNEW m_msgTemporaryIn) {
			this.remote = connection;
			this.msg = m_msgTemporaryIn;
		}

		@Override
		public String toString() {
			return getMessage().toString();
		}

		public Connection getRemote() { return remote; }

		public MessageBufferNEW getMessage() { return msg; }
	}

	@Override
	public String toString() {
		return "MessageBuffer<" + id + ">{" + super.toString() + "}";
	}

	protected Object id;

	public MessageBufferNEW() { super(); }

	public MessageBufferNEW(PacketBuffer origin) { super(origin); }

	public MessageBufferNEW(ByteBuf wrapped) { super(wrapped); }

	@Override
	public Object getID() { return id; }

	public <T> T getIDT() { return (T) id; }

	@Override
	public MessageBufferNEW setID(Object id) { this.id = id; return this; }

	/**
	 * Write this message out to the specified stream
	 * 
	 * @return the size of this message in bytes written to the specified stream
	 */
	@Override
	public int write(OutputStream out) throws IOException {
		BufferedOutputStream bout = new BufferedOutputStream(out);

		Logger.warn("write header to outputstream");
		int headerSize = writeHeader(bout);

		Logger.warn("write data to outputstream");
		int dataSize = writeData(bout);

		Logger.warn("headerSize: " + headerSize + ", dataSize: " + dataSize);

		return headerSize + dataSize;
	}

	/**
	 * @return byte size of the message header
	 */
	@Override
	protected int writeHeader(OutputStream out) throws IOException {
		PacketBuffer header = new PacketBuffer();
		writeID(header);
		header.writeInt(writerIndex());
		int headerSize = header.write(out);
		out.flush();
		return headerSize;
	}

//	/**
//	 * @return byte size of the message data
//	 */
//	@Override
//	int writeData(OutputStream out) throws IOException {
//		int dataSize = super.write(out);
//		out.flush();
//		return dataSize;
//	}

	/**
	 * Reads in the message from the InputStream
	 * 
	 * @return the data size
	 * 
	 * @throws IOException
	 *             if the specified stream threw an exception during I/O
	 */
	@Override
	public int read(InputStream in) throws IOException {
		BufferedInputStream bin = new BufferedInputStream(in);

		// Read in the message header
		int dataSize = readHeader(bin);

		// Read in the message data
		readData(bin, dataSize);

		Logger.debug("Read Data Successfully");

		// Return the message data size
		return dataSize;
	}

	/**
	 * Reads in the message header from the InputStream
	 * 
	 * @return the data size
	 * @throws IOException
	 *             if the specified stream threw an exception during I/O
	 */
	@Override
	protected int readHeader(InputStream in) throws IOException {
		try {
			readID(in);
		} catch (ClassNotFoundException e) {
			throw new ReadMessageException("", e);
		}

		int dataSize = readInt();
		discardReadBytes();
		return dataSize;
	}

	/**
	 * Reads in the message data from the InputStream
	 * 
	 * @throws IOException
	 *             if the specified stream threw an exception during I/O
	 */
	@Override
	void readData(InputStream in, int dataSize) throws IOException {
		Logger.help("DS: " + dataSize);
		addData(in, dataSize);
		Logger.help("Data arrived");
	}

	@Override
	void addData(InputStream in, int dataSize) throws IOException {
		if (dataSize == 0) return;
		int readBytes = readableBytes();
		while (readBytes < dataSize) {
			int _readBytes = writeBytes(in, dataSize - readBytes);
			if (_readBytes == -1) throw new EOFException();
			readBytes += _readBytes;
//			Logger.debug("MessageBuffer", readBytes + "/" + dataSize + " - (" + _readBytes + ")");
		}
	}

	@Override
	protected void writeID(PacketBuffer idbuffer) {
		if (id instanceof Boolean) {
			idbuffer.writeByte(T_BOOL);
			idbuffer.writeBoolean((boolean) id);
		} else if (id instanceof Byte) {
			idbuffer.writeByte(T_BYTE);
			idbuffer.writeByte((byte) id);
		} else if (id instanceof Short) {
			idbuffer.writeByte(T_SHORT);
			idbuffer.writeShort((short) id);
		} else if (id instanceof Integer) {
			idbuffer.writeByte(T_INT);
			idbuffer.writeInt((int) id);
		} else if (id instanceof Long) {
			idbuffer.writeByte(T_LONG);
			idbuffer.writeLong((long) id);
		} else if (id instanceof Float) {
			idbuffer.writeByte(T_FLOAT);
			idbuffer.writeFloat((float) id);
		} else if (id instanceof Double) {
			idbuffer.writeByte(T_DOUBLE);
			idbuffer.writeDouble((double) id);
		} else if (id instanceof Character) {
			idbuffer.writeByte(T_CHAR);
			idbuffer.writeChar((int) id);
		} else if (id instanceof String) {
			idbuffer.writeByte(T_STRING);
			idbuffer.writeString((String) id);
		} else if (id instanceof Date) {
			idbuffer.writeByte(T_TIME);
			idbuffer.writeTime((Date) id);
		} else if (id instanceof UUID) {
			idbuffer.writeByte(T_UUID);
			idbuffer.writeUniqueId((UUID) id);
		} else if (id instanceof Enum<?>) {
			idbuffer.writeByte(T_ENUM);
			String idClazzName = id.getClass().getName();
			byte[] idClazzNameBytes = idClazzName.getBytes();
			idbuffer.writeVarInt(idClazzNameBytes.length);
			idbuffer.writeBytes(idClazzName.getBytes());

			idbuffer.writeEnumValue((Enum<?>) id);
		} else if (id instanceof IIdentifier) {
			idbuffer.writeByte(T_GEN);
			idbuffer.writeVarInt(((IIdentifier) id).size());

			String idClazzName = id.getClass().getName();
			byte[] idClazzNameBytes = idClazzName.getBytes();
			idbuffer.writeVarInt(idClazzNameBytes.length);
			idbuffer.writeBytes(idClazzName.getBytes());

			((IIdentifier) id).write(idbuffer);
		} else {
			throw new IllegalStateException("The Identifier " + id

					+ " is of an unsupported type "
					+ (id != null ? id.getClass() : "NULL"));
		}
	}

	private <E extends Enum<E>> void readID(InputStream in) throws IOException, ClassNotFoundException {
		int idSize;
		Supplier<Object> idSup;
		byte type = readByteSafe(in);
		switch (type) {
			case T_BOOL:
				idSize = Byte.BYTES;
				idSup = this::readBoolean;
				break;
			case T_BYTE:
				idSize = Byte.BYTES;
				idSup = this::readByte;
				break;
			case T_SHORT:
				idSize = Short.BYTES;
				idSup = this::readShort;
				break;
			case T_INT:
				idSize = Integer.BYTES;
				idSup = this::readInt;
				break;
			case T_LONG:
				idSize = Long.BYTES;
				idSup = this::readLong;
				break;
			case T_FLOAT:
				idSize = Float.BYTES;
				idSup = this::readFloat;
				break;
			case T_DOUBLE:
				idSize = Double.BYTES;
				idSup = this::readDouble;
				break;
			case T_CHAR:
				idSize = Character.BYTES;
				idSup = this::readChar;
			case T_STRING:
				idSize = -2;
				@SuppressWarnings("deprecation")
				Supplier<Object> s = this::readString;
				idSup = s;
				break;
			case T_TIME:
				idSize = Long.BYTES;
				idSup = this::readTime;
				break;
			case T_UUID:
				idSize = Long.BYTES + Long.BYTES;
				idSup = this::readUniqueId;
				break;
			case T_ENUM:
				idSize = Integer.BYTES;
				String enumStr = readString(in);

				@SuppressWarnings("unchecked")
				Class<E> enumClazz = (Class<E>) Class.forName(enumStr);
				idSup = () -> readEnumValue(enumClazz);
				break;
			case T_GEN:
				idSize = readVarInt(in);
				String genStr = readString(in);

				System.out.println("NAME: " + genStr);
				@SuppressWarnings("unchecked")
				Class<IIdentifier> genClazz = (Class<IIdentifier>) Class.forName(genStr);
				idSup = () -> {
					IIdentifier id = _getID(genClazz);
					id.read(this);
					return id;
				};
				break;
			default:
				System.out.print(type);
				throw new IllegalStateException();
		}
		int headerSize = idSize + Integer.BYTES;
		addData(in, headerSize);

		@SuppressWarnings("unchecked")
		Object cid = idSup.get();
		id = cid;
	}

	private IIdentifier _getID(Class<IIdentifier> idClazz) {
		if (id != null) return (IIdentifier) id;
		try {
			@SuppressWarnings("unchecked")
			Constructor<IIdentifier>[] ctors = (Constructor<IIdentifier>[]) idClazz.getDeclaredConstructors();
			Constructor<IIdentifier> ctor = null;
			int minLen = 1000;
			for (int i = 0; i < ctors.length; i++) {
				Constructor<IIdentifier> cctor = ctors[i];
				int len = ctors[i].getGenericParameterTypes().length;
				if (len < minLen) {
					minLen = ctors[i].getGenericParameterTypes().length;
					ctor = cctor;
				}
			}
			return ctor.newInstance(new Object[minLen]);
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String readString(InputStream in) throws IOException {
		int length = readVarInt(in);
		byte[] bytes = new byte[length];
		int alength = in.read(bytes);
		assert length == alength;
		return new String(bytes);
	}

	private static int readVarInt(InputStream in) throws IOException {
		int i = 0;
		int j = 0;

		while (true) {
			byte b0 = (byte) in.read();
			i |= (b0 & 127) << j++ * 7;
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}

			if ((b0 & 128) != 128) {
				break;
			}
		}

		return i;
	}

	private static byte readByteSafe(InputStream in) throws IOException {
		byte b = (byte) in.read();
		if (b == -1) throw new SocketException("Connection closed");
		return b;
	}

	public static MessageBufferNEW create(InputStream in) throws IOException {
		MessageBufferNEW message = new MessageBufferNEW();
		message.read(in);
		return message;
	}

	public static <T> MessageBufferNEW createT(T id) { return createT().setID(id); }

	public static <T> MessageBufferNEW createT(T id, PacketBuffer origin) { return createT(origin).setID(id); }

	public static <T> MessageBufferNEW createT(T id, ByteBuf wrapper) { return createT(wrapper).setID(id); }

	public static MessageBufferNEW createT() { return new MessageBufferNEW(); }

	public static MessageBufferNEW createT(PacketBuffer origin) { return new MessageBufferNEW(origin); }

	public static MessageBufferNEW createT(ByteBuf wrapper) { return new MessageBufferNEW(wrapper); }
}
