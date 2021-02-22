package com.sunflow.message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;

import com.sunflow.common.Connection;
import com.sunflow.error.UnkownClassNameException;
import com.sunflow.error.UnkownIdentifierException;
import com.sunflow.util.Logger;

public class MessageBuffer<T> extends PacketBuffer {
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

	public static class Owned<T> {

		private Connection<T> remote;
		private MessageBuffer<T> msg;

		public Owned(Connection<T> connection, MessageBuffer<T> m_msgTemporaryIn) {
			this.remote = connection;
			this.msg = m_msgTemporaryIn;
		}

		@Override
		public String toString() { return getMessage().toString(); }

		public Connection<T> getRemote() { return remote; }

		public MessageBuffer<T> getMessage() { return msg; }
	}

	@Override
	public String toString() {
		return "MessageBuffer<" + id + ">{" + super.toString() + "}";
	}

	protected T id;

	public MessageBuffer() { super(); }

	public MessageBuffer(PacketBuffer origin) { super(origin); }

//	public MessageBuffer(ByteBuf wrapped) { super(wrapped); }

	public T getID() { return id; }

	public MessageBuffer<T> setID(T id) { this.id = id; return this; }

	/**
	 * Write this message out to the specified stream
	 * 
	 * @return the size of this message in bytes written to the specified stream
	 */
	@Override
	public int write(OutputStream out) throws IOException {
		BufferedOutputStream bout = new BufferedOutputStream(out);

		Logger.net("write header to outputstream");
		int headerSize = writeHeader(bout);

		Logger.net("write data to outputstream");
		int dataSize = writeData(bout);

		Logger.net("headerSize: " + headerSize + ", dataSize: " + dataSize);

		return headerSize + dataSize;
	}

	/**
	 * @return byte size of the message header
	 */
	protected int writeHeader(OutputStream out) throws IOException {
		PacketBuffer header = new PacketBuffer();
		writeID(header);
		int dataSize = writerIndex();
		header.writeInt(dataSize);
		int headerSize = header.write(out);
		out.flush();
		return headerSize;
	}

	/**
	 * @return byte size of the message data
	 */
	int writeData(OutputStream out) throws IOException {
		int dataSize = super.write(out);
		out.flush();
		return dataSize;
	}

	/**
	 * Reads in the message from the InputStream
	 * 
	 * @return the data size
	 * 
	 * @throws IOException
	 *             if the specified stream threw an exception during I/O
	 * @throws ClassNotFoundException
	 */
	@Override
	public int read(InputStream in) throws IOException {
		BufferedInputStream bin = new BufferedInputStream(in);

		// Read in the message header
		int dataSize = readHeader(bin);

		// Read in the message data
		readData(bin, dataSize);

		Logger.net("Read Data Successfully");

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
	protected int readHeader(InputStream in) throws IOException {
		readID(in);

		int dataSize = readInt();
		discardReadBytes();
//		discardSomeReadBytes();
//		clear();
		return dataSize;
	}

	/**
	 * Reads in the message data from the InputStream
	 * 
	 * @throws IOException
	 *             if the specified stream threw an exception during I/O
	 */
	void readData(InputStream in, int dataSize) throws IOException {
		Logger.net("DS: " + dataSize);
		addData(in, dataSize);
		Logger.net("Data arrived");
	}

	void addData(InputStream in, int dataSize) throws IOException {
		if (dataSize == 0) return;
//		int readBytes = readableBytes();
		int totalReadBytes = 0;
		while (totalReadBytes < dataSize) {
			int currentReadBytes = writeBytes(in, dataSize - totalReadBytes);
			if (currentReadBytes == -1) throw new EOFException();
			totalReadBytes += currentReadBytes;
			Logger.net("MessageBuffer", totalReadBytes + "/" + dataSize + " - (" + currentReadBytes + ")");
		}
	}

	protected void writeID(PacketBuffer idbuffer) {
		// @formatter:off
		if (id instanceof Boolean)  	  idbuffer.writeByte(T_BOOL)  .writeBoolean( (boolean) id);
		else if (id instanceof Byte) 	  idbuffer.writeByte(T_BYTE)  .writeByte(	 (byte)	   id);
		else if (id instanceof Short) 	  idbuffer.writeByte(T_SHORT) .writeShort(   (short)   id);
		else if (id instanceof Integer)   idbuffer.writeByte(T_INT)   .writeInt(     (int) 	   id);
		else if (id instanceof Long) 	  idbuffer.writeByte(T_LONG)  .writeLong(    (long)	   id);
		else if (id instanceof Float) 	  idbuffer.writeByte(T_FLOAT) .writeFloat(   (float)   id);
		else if (id instanceof Double)	  idbuffer.writeByte(T_DOUBLE).writeDouble(  (double)  id);
		else if (id instanceof Character) idbuffer.writeByte(T_CHAR)  .writeChar(    (int) 	   id);
		else if (id instanceof String) 	  idbuffer.writeByte(T_STRING).writeString(  (String)  id);
		else if (id instanceof Date) 	  idbuffer.writeByte(T_TIME)  .writeTime(    (Date)	   id);
		else if (id instanceof UUID) 	  idbuffer.writeByte(T_UUID)  .writeUniqueId((UUID)    id);
		// @formatter:on
		else if (id instanceof Enum<?>) {
			idbuffer.writeByte(T_ENUM);

			idbuffer.writeString(id.getClass().getName());

			idbuffer.writeEnumValue((Enum<?>) id);
		} else if (id instanceof IIdentifier) {
			idbuffer.writeByte(T_GEN);
			idbuffer.writeVarInt(((IIdentifier) id).size());

			idbuffer.writeString(id.getClass().getName());

			((IIdentifier) id).write(idbuffer);
		} else throw new IllegalStateException("The Identifier " + id
				+ " is of an unsupported type "
				+ (id != null ? id.getClass() : "NULL"));
	}

	private <E extends Enum<E>> void readID(InputStream in) throws IOException {
		int idSize;
		Supplier<Object> idSup;
		int type = in.read();
		switch (type) {
			case -1:
				throw new SocketException("Connection closed");
			default:
				throw new UnkownIdentifierException("" + type);
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

				try {
					@SuppressWarnings("unchecked")
					Class<E> enumClazz = (Class<E>) Class.forName(enumStr);
					idSup = () -> readEnumValue(enumClazz);
				} catch (ClassNotFoundException e) {
					throw new UnkownClassNameException("ClassName: '" + enumStr + "'", e);
				}
				break;
			case T_GEN:
				idSize = readVarInt(in);
				String genStr = readString(in);

				System.out.println("NAME: " + genStr);
				try {
					@SuppressWarnings("unchecked")
					Class<IIdentifier> genClazz = (Class<IIdentifier>) Class.forName(genStr);
					idSup = () -> {
						IIdentifier id = _getID(genClazz);
						id.read(this);
						return id;
					};
				} catch (ClassNotFoundException e) {
					throw new UnkownClassNameException(e);
				}
				break;
		}
		int headerSize = idSize + Integer.BYTES;
		addData(in, headerSize);

		@SuppressWarnings("unchecked")
		T cid = (T) idSup.get();
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
		} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
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

	public static <T> MessageBuffer<T> create(InputStream in) throws IOException {
		MessageBuffer<T> message = new MessageBuffer<>();
		message.read(in);
		return message;
	}

	/* _ */
	public static <E> MessageBuffer<E> create_(E id) { MessageBuffer<E> msg = create_(); msg.setID(id); return msg; }

	public static <E> MessageBuffer<E> create_(E id, PacketBuffer origin) { MessageBuffer<E> msg = create_(origin); msg.setID(id); return msg; }

//	public static <E> MessageBuffer<E> create_(E id, ByteBuf wrapper) { MessageBuffer<E> msg = create_(wrapper); msg.setID(id); return msg; }

	public static <E> MessageBuffer<E> create_() { return new MessageBuffer<>(); }

	public static <E> MessageBuffer<E> create_(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static <E> MessageBuffer<E> create_(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Enum */
	public static <E extends Enum<E>> MessageBuffer<E> create(E id) { MessageBuffer<E> msg = createEnum(); msg.setID(id); return msg; }

	public static <E extends Enum<E>> MessageBuffer<E> create(E id, PacketBuffer origin) { MessageBuffer<E> msg = createEnum(origin); msg.setID(id); return msg; }

//	public static <E extends Enum<E>> MessageBuffer<E> create(E id, ByteBuf wrapper) { MessageBuffer<E> msg = createEnum(wrapper); msg.setID(id); return msg; }

	public static <E extends Enum<E>> MessageBuffer<E> createEnum() { return new MessageBuffer<>(); }

	public static <E extends Enum<E>> MessageBuffer<E> createEnum(PacketBuffer origin) { return new MessageBuffer<E>(origin); }

//	public static <E extends Enum<E>> MessageBuffer<E> createEnum(ByteBuf wrapper) { return new MessageBuffer<E>(wrapper); }

	/* Boolean */
	public static MessageBuffer<Boolean> create(boolean id) { return createBoolean().setID(id); }

	public static MessageBuffer<Boolean> create(boolean id, PacketBuffer origin) { return createBoolean(origin).setID(id); }

//	public static MessageBuffer<Boolean> create(boolean id, ByteBuf wrapper) { return createBoolean(wrapper).setID(id); }

	public static MessageBuffer<Boolean> createBoolean() { return new MessageBuffer<>(); }

	public static MessageBuffer<Boolean> createBoolean(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<Boolean> createBoolean(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Byte */
	public static MessageBuffer<Byte> create(byte id) { return createByte().setID(id); }

	public static MessageBuffer<Byte> create(byte id, PacketBuffer origin) { return createByte(origin).setID(id); }

//	public static MessageBuffer<Byte> create(byte id, ByteBuf wrapper) { return createByte(wrapper).setID(id); }

	public static MessageBuffer<Byte> createByte() { return new MessageBuffer<>(); }

	public static MessageBuffer<Byte> createByte(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<Byte> createByte(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Short */
	public static MessageBuffer<Short> create(short id) { return createShort().setID(id); }

	public static MessageBuffer<Short> create(short id, PacketBuffer origin) { return createShort(origin).setID(id); }

//	public static MessageBuffer<Short> create(short id, ByteBuf wrapper) { return createShort(wrapper).setID(id); }

	public static MessageBuffer<Short> createShort() { return new MessageBuffer<>(); }

	public static MessageBuffer<Short> createShort(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<Short> createShort(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Integer */
	public static MessageBuffer<Integer> create(int id) { return createInteger().setID(id); }

	public static MessageBuffer<Integer> create(int id, PacketBuffer origin) { return createInteger(origin).setID(id); }

//	public static MessageBuffer<Integer> create(int id, ByteBuf wrapper) { return createInteger(wrapper).setID(id); }

	public static MessageBuffer<Integer> createInteger() { return new MessageBuffer<>(); }

	public static MessageBuffer<Integer> createInteger(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<Integer> createInteger(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Long */
	public static MessageBuffer<Long> create(long id) { return createLong().setID(id); }

	public static MessageBuffer<Long> create(long id, PacketBuffer origin) { return createLong(origin).setID(id); }

//	public static MessageBuffer<Long> create(long id, ByteBuf wrapper) { return createLong(wrapper).setID(id); }

	public static MessageBuffer<Long> createLong() { return new MessageBuffer<>(); }

	public static MessageBuffer<Long> createLong(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<Long> createLong(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Float */
	public static MessageBuffer<Float> create(float id) { return createFloat().setID(id); }

	public static MessageBuffer<Float> create(float id, PacketBuffer origin) { return createFloat(origin).setID(id); }

//	public static MessageBuffer<Float> create(float id, ByteBuf wrapper) { return createFloat(wrapper).setID(id); }

	public static MessageBuffer<Float> createFloat() { return new MessageBuffer<>(); }

	public static MessageBuffer<Float> createFloat(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<Float> createFloat(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Double */
	public static MessageBuffer<Double> create(double id) { return createDouble().setID(id); }

	public static MessageBuffer<Double> create(double id, PacketBuffer origin) { return createDouble(origin).setID(id); }

//	public static MessageBuffer<Double> create(double id, ByteBuf wrapper) { return createDouble(wrapper).setID(id); }

	public static MessageBuffer<Double> createDouble() { return new MessageBuffer<>(); }

	public static MessageBuffer<Double> createDouble(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<Double> createDouble(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Character */
	public static MessageBuffer<Character> create(char id) { return createCharacter().setID(id); }

	public static MessageBuffer<Character> create(char id, PacketBuffer origin) { return createCharacter(origin).setID(id); }

//	public static MessageBuffer<Character> create(char id, ByteBuf wrapper) { return createCharacter(wrapper).setID(id); }

	public static MessageBuffer<Character> createCharacter() { return new MessageBuffer<>(); }

	public static MessageBuffer<Character> createCharacter(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<Character> createCharacter(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Time */
	public static MessageBuffer<Date> create(Date id) { return createTime().setID(id); }

	public static MessageBuffer<Date> create(Date id, PacketBuffer origin) { return createTime(origin).setID(id); }

//	public static MessageBuffer<Date> create(Date id, ByteBuf wrapper) { return createTime(wrapper).setID(id); }

	public static MessageBuffer<Date> createTime() { return new MessageBuffer<>(); }

	public static MessageBuffer<Date> createTime(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<Date> createTime(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* UUID */
	public static MessageBuffer<UUID> create(UUID id) { return createUUID().setID(id); }

	public static MessageBuffer<UUID> create(UUID id, PacketBuffer origin) { return createUUID(origin).setID(id); }

//	public static MessageBuffer<UUID> create(UUID id, ByteBuf wrapper) { return createUUID(wrapper).setID(id); }

	public static MessageBuffer<UUID> createUUID() { return new MessageBuffer<>(); }

	public static MessageBuffer<UUID> createUUID(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static MessageBuffer<UUID> createUUID(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

	/* Generic */
	public static <D extends IIdentifier> MessageBuffer<D> create(D id) { MessageBuffer<D> msg = createGeneric(); msg.setID(id); return msg; }

	public static <D extends IIdentifier> MessageBuffer<D> create(D id, PacketBuffer origin) { MessageBuffer<D> msg = createGeneric(origin); msg.setID(id); return msg; }

//	public static <D extends IIdentifier> MessageBuffer<D> create(D id, ByteBuf wrapper) { MessageBuffer<D> msg = createGeneric(wrapper); msg.setID(id); return msg; }

	public static <D extends IIdentifier> MessageBuffer<D> createGeneric() { return new MessageBuffer<>(); }

	public static <D extends IIdentifier> MessageBuffer<D> createGeneric(PacketBuffer origin) { return new MessageBuffer<>(origin); }

//	public static <D extends IIdentifier> MessageBuffer<D> createGeneric(ByteBuf wrapper) { return new MessageBuffer<>(wrapper); }

}
