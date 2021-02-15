package com.sunflow.message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.SocketException;
import java.util.Date;
import java.util.UUID;

import com.sunflow.common.Connection;
import com.sunflow.util.Logger;

import io.netty.buffer.ByteBuf;

public abstract class MessageBufferOLD<T> extends PacketBuffer {
	public static class Owned<T> {

		private Connection<T> remote;
		private MessageBufferOLD<T> msg;

		public Owned(Connection<T> connection, MessageBufferOLD<T> m_msgTemporaryIn) {
			this.remote = connection;
			this.msg = m_msgTemporaryIn;
		}

		@Override
		public String toString() {
			return getMessage().toString();
		}

		public Connection<T> getRemote() { return remote; }

		public MessageBufferOLD<T> getMessage() { return msg; }
	}

	@Override
	public String toString() {
		return "MessageBuffer<" + id + ">{" + super.toString() + "}";
	}

	protected T id;

	MessageBufferOLD() { super(); }

	MessageBufferOLD(MessageBufferOLD<T> origin) { super(origin); }

	MessageBufferOLD(PacketBuffer origin) { super(origin); }

	MessageBufferOLD(ByteBuf wrapped) { super(wrapped); }

	public T getID() { return id; }

	public MessageBufferOLD<T> setID(T id) { this.id = id; return this; }

	/**
	 * @return byte size of the header (idSize + dataSize[4])
	 */
//	public int headerSize() { return idSize() + 4; }

	/**
	 * @return byte size of the identifier
	 */
	public abstract int idSize();

	public int headerSize() { return idSize() + Integer.BYTES; }

	/**
	 * Write this message out to the specified stream
	 * 
	 * @return the size of this message in bytes written to the specified stream
	 */
	@Override
	public int write(OutputStream out) throws IOException {
		OutputStream bout = new BufferedOutputStream(out);

		Logger.debug("write header to outputstream");
		int headerSize = writeHeader(bout);

		Logger.debug("write data to outputstream");
		int dataSize = writeData(bout);

		Logger.debug("headerSize: " + headerSize + ", dataSize: " + dataSize);

//		Logger.warn("flush outputstream");
//		bout.flush();
//		Logger.warn("flushed outputstream");

		return headerSize + dataSize;
	}

	/**
	 * @return byte size of the message header
	 */
	int writeHeader(OutputStream out) throws IOException {
		PacketBuffer header = new PacketBuffer();
		writeID(header);
		header.writeInt(writerIndex());
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
	 */
	@Override
	public int read(InputStream in) throws IOException {
		InputStream bin = new BufferedInputStream(in);

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
	int readHeader(InputStream in) throws IOException {
		Logger.debug("HS: " + headerSize());
		while (in.available() < headerSize()) {
			in.mark(1);
			int b = in.read();
			if (b == -1) throw new SocketException("Connection closed");
			in.reset();
		}
		Logger.debug("Header arrived");
		readerIndex(0);
		writerIndex(0);
		writeBytes(in, headerSize());
		_readID();

		int dataSize = readInt();

		readerIndex(0);
		writerIndex(0);
		return dataSize;
	}

	/**
	 * Reads in the message data from the InputStream
	 * 
	 * @throws IOException
	 *             if the specified stream threw an exception during I/O
	 */
	void readData(InputStream in, int dataSize) throws IOException {
		if (dataSize == 0) return;
		Logger.help("DS: " + dataSize);
		int readBytes = 0;
		while (readBytes < dataSize) {
			int _readBytes = writeBytes(in, dataSize - readBytes);
			readBytes += _readBytes;
//			System.out.println(readBytes + "/" + dataSize + " - (" + _readBytes + ")");
		}
		Logger.help("Data arrived");
	}

	protected abstract PacketBuffer writeID(PacketBuffer idbuffer) throws IOException;

	protected abstract void _readID() throws IOException;

	public static class EnumMessage<E extends Enum<E>> extends MessageBufferOLD<E> {
		private final Class<E> idClazz;

		public EnumMessage(Class<E> idClazz) { super(); this.idClazz = idClazz; }

		public EnumMessage(Class<E> idClazz, PacketBuffer origin) { super(origin); this.idClazz = idClazz; }

		public EnumMessage(Class<E> idClazz, ByteBuf wrapped) { super(wrapped); this.idClazz = idClazz; }

		@Override
		public int idSize() { return Integer.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			E id = super.id != null ? super.id : idClazz.getEnumConstants()[0];
			idbuffer.writeEnumValue(id);
			return idbuffer;
		}

		@Override
		protected void _readID() { Logger.help("MessageBuffer", idClazz); setID(readEnumValue(idClazz)); }
	}

	public static class BooleanMessage extends MessageBufferOLD<Boolean> {

		public BooleanMessage() { super(); }

		public BooleanMessage(PacketBuffer origin) { super(origin); }

		public BooleanMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Byte.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeBoolean(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readBoolean()); }
	}

	public static class ByteMessage extends MessageBufferOLD<Byte> {

		public ByteMessage() { super(); }

		public ByteMessage(PacketBuffer origin) { super(origin); }

		public ByteMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Byte.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeByte(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readByte()); }
	}

	public static class ShortMessage extends MessageBufferOLD<Short> {

		public ShortMessage() { super(); }

		public ShortMessage(PacketBuffer origin) { super(origin); }

		public ShortMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Short.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			System.out.println(idbuffer + " " + super.id);
			idbuffer.writeShort(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readShort()); }
	}

	public static class IntegerMessage extends MessageBufferOLD<Integer> {

		public IntegerMessage() { super(); }

		public IntegerMessage(PacketBuffer origin) { super(origin); }

		public IntegerMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Integer.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeInt(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readInt()); }
	}

	public static class LongMessage extends MessageBufferOLD<Long> {

		public LongMessage() { super(); }

		public LongMessage(PacketBuffer origin) { super(origin); }

		public LongMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Long.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeLong(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readLong()); }
	}

	public static class FloatMessage extends MessageBufferOLD<Float> {

		public FloatMessage() { super(); }

		public FloatMessage(PacketBuffer origin) { super(origin); }

		public FloatMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Float.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeFloat(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readFloat()); }
	}

	public static class DoubleMessage extends MessageBufferOLD<Double> {

		public DoubleMessage() { super(); }

		public DoubleMessage(PacketBuffer origin) { super(origin); }

		public DoubleMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Double.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeDouble(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readDouble()); }
	}

	public static class CharacterMessage extends MessageBufferOLD<Character> {

		public CharacterMessage() { super(); }

		public CharacterMessage(PacketBuffer origin) { super(origin); }

		public CharacterMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Character.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeChar(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readChar()); }
	}

	public static class TimeMessage extends MessageBufferOLD<Date> {

		public TimeMessage() { super(); }

		public TimeMessage(PacketBuffer origin) { super(origin); }

		public TimeMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Long.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeTime(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readTime()); }
	}

	public static class UUIDMessage extends MessageBufferOLD<UUID> {

		public UUIDMessage() { super(); }

		public UUIDMessage(PacketBuffer origin) { super(origin); }

		public UUIDMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		public int idSize() { return Long.BYTES + Long.BYTES; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeUniqueId(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readUniqueId()); }
	}

	public static class GenericMessage<G extends IIdentifier> extends MessageBufferOLD<G> {

		private final Class<G> idClazz;

		public GenericMessage(Class<G> idClazz) { super(); this.idClazz = idClazz; }

		public GenericMessage(Class<G> idClazz, PacketBuffer origin) { super(origin); this.idClazz = idClazz; }

		public GenericMessage(Class<G> idClazz, ByteBuf wrapped) { super(wrapped); this.idClazz = idClazz; }

		@Override
		public int idSize() { return _getID().size(); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			_getID().write(idbuffer);
			return idbuffer;
		}

		@Override
		protected void _readID() {
			super.id = _getID();
			super.id.read(this);
		}

		@SuppressWarnings("unchecked")
		private G _getID() {
			if (super.id != null) return super.id;
			try {
				Constructor<?>[] ctors = idClazz.getDeclaredConstructors();
				Constructor<?> ctor = null;
				int minLen = 1000;
				for (int i = 0; i < ctors.length; i++) {
					Constructor<?> cctor = ctors[i];
					int len = ctors[i].getGenericParameterTypes().length;
					if (len < minLen) {
						minLen = ctors[i].getGenericParameterTypes().length;
						ctor = cctor;
					}
				}
				return (G) ctor.newInstance(new Object[minLen]);
			} catch (ReflectiveOperationException | IllegalArgumentException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/* Enum */
	public static <E extends Enum<E>> MessageBufferOLD<E> create(E id) { return createEnum(id.getDeclaringClass()).setID(id); }

	public static <E extends Enum<E>> MessageBufferOLD<E> create(E id, PacketBuffer origin) { return createEnum(id.getDeclaringClass(), origin).setID(id); }

	public static <E extends Enum<E>> MessageBufferOLD<E> create(E id, ByteBuf wrapper) { return createEnum(id.getDeclaringClass(), wrapper).setID(id); }

	public static <E extends Enum<E>> MessageBufferOLD<E> createEnum(Class<E> idClazz) { return new MessageBufferOLD.EnumMessage<E>(idClazz); }

	public static <E extends Enum<E>> MessageBufferOLD<E> createEnum(Class<E> idClazz, PacketBuffer origin) { return new MessageBufferOLD.EnumMessage<E>(idClazz, origin); }

	public static <E extends Enum<E>> MessageBufferOLD<E> createEnum(Class<E> idClazz, ByteBuf wrapper) { return new MessageBufferOLD.EnumMessage<E>(idClazz, wrapper); }

	/* Boolean */
	public static MessageBufferOLD<Boolean> create(boolean id) { return createBoolean().setID(id); }

	public static MessageBufferOLD<Boolean> create(boolean id, PacketBuffer origin) { return createBoolean(origin).setID(id); }

	public static MessageBufferOLD<Boolean> create(boolean id, ByteBuf wrapper) { return createBoolean(wrapper).setID(id); }

	public static MessageBufferOLD<Boolean> createBoolean() { return new MessageBufferOLD.BooleanMessage(); }

	public static MessageBufferOLD<Boolean> createBoolean(PacketBuffer origin) { return new MessageBufferOLD.BooleanMessage(origin); }

	public static MessageBufferOLD<Boolean> createBoolean(ByteBuf wrapper) { return new MessageBufferOLD.BooleanMessage(wrapper); }

	/* Byte */
	public static MessageBufferOLD<Byte> create(byte id) { return createByte().setID(id); }

	public static MessageBufferOLD<Byte> create(byte id, PacketBuffer origin) { return createByte(origin).setID(id); }

	public static MessageBufferOLD<Byte> create(byte id, ByteBuf wrapper) { return createByte(wrapper).setID(id); }

	public static MessageBufferOLD<Byte> createByte() { return new MessageBufferOLD.ByteMessage(); }

	public static MessageBufferOLD<Byte> createByte(PacketBuffer origin) { return new MessageBufferOLD.ByteMessage(origin); }

	public static MessageBufferOLD<Byte> createByte(ByteBuf wrapper) { return new MessageBufferOLD.ByteMessage(wrapper); }

	/* Short */
	public static MessageBufferOLD<Short> create(short id) { return createShort().setID(id); }

	public static MessageBufferOLD<Short> create(short id, PacketBuffer origin) { return createShort(origin).setID(id); }

	public static MessageBufferOLD<Short> create(short id, ByteBuf wrapper) { return createShort(wrapper).setID(id); }

	public static MessageBufferOLD<Short> createShort() { return new MessageBufferOLD.ShortMessage(); }

	public static MessageBufferOLD<Short> createShort(PacketBuffer origin) { return new MessageBufferOLD.ShortMessage(origin); }

	public static MessageBufferOLD<Short> createShort(ByteBuf wrapper) { return new MessageBufferOLD.ShortMessage(wrapper); }

	/* Integer */
	public static MessageBufferOLD<Integer> create(int id) { return createInteger().setID(id); }

	public static MessageBufferOLD<Integer> create(int id, PacketBuffer origin) { return createInteger(origin).setID(id); }

	public static MessageBufferOLD<Integer> create(int id, ByteBuf wrapper) { return createInteger(wrapper).setID(id); }

	public static MessageBufferOLD<Integer> createInteger() { return new MessageBufferOLD.IntegerMessage(); }

	public static MessageBufferOLD<Integer> createInteger(PacketBuffer origin) { return new MessageBufferOLD.IntegerMessage(origin); }

	public static MessageBufferOLD<Integer> createInteger(ByteBuf wrapper) { return new MessageBufferOLD.IntegerMessage(wrapper); }

	/* Long */
	public static MessageBufferOLD<Long> create(long id) { return createLong().setID(id); }

	public static MessageBufferOLD<Long> create(long id, PacketBuffer origin) { return createLong(origin).setID(id); }

	public static MessageBufferOLD<Long> create(long id, ByteBuf wrapper) { return createLong(wrapper).setID(id); }

	public static MessageBufferOLD<Long> createLong() { return new MessageBufferOLD.LongMessage(); }

	public static MessageBufferOLD<Long> createLong(PacketBuffer origin) { return new MessageBufferOLD.LongMessage(origin); }

	public static MessageBufferOLD<Long> createLong(ByteBuf wrapper) { return new MessageBufferOLD.LongMessage(wrapper); }

	/* Float */
	public static MessageBufferOLD<Float> create(float id) { return createFloat().setID(id); }

	public static MessageBufferOLD<Float> create(float id, PacketBuffer origin) { return createFloat(origin).setID(id); }

	public static MessageBufferOLD<Float> create(float id, ByteBuf wrapper) { return createFloat(wrapper).setID(id); }

	public static MessageBufferOLD<Float> createFloat() { return new MessageBufferOLD.FloatMessage(); }

	public static MessageBufferOLD<Float> createFloat(PacketBuffer origin) { return new MessageBufferOLD.FloatMessage(origin); }

	public static MessageBufferOLD<Float> createFloat(ByteBuf wrapper) { return new MessageBufferOLD.FloatMessage(wrapper); }

	/* Double */
	public static MessageBufferOLD<Double> create(double id) { return createDouble().setID(id); }

	public static MessageBufferOLD<Double> create(double id, PacketBuffer origin) { return createDouble(origin).setID(id); }

	public static MessageBufferOLD<Double> create(double id, ByteBuf wrapper) { return createDouble(wrapper).setID(id); }

	public static MessageBufferOLD<Double> createDouble() { return new MessageBufferOLD.DoubleMessage(); }

	public static MessageBufferOLD<Double> createDouble(PacketBuffer origin) { return new MessageBufferOLD.DoubleMessage(origin); }

	public static MessageBufferOLD<Double> createDouble(ByteBuf wrapper) { return new MessageBufferOLD.DoubleMessage(wrapper); }

	/* Character */
	public static MessageBufferOLD<Character> create(char id) { return createCharacter().setID(id); }

	public static MessageBufferOLD<Character> create(char id, PacketBuffer origin) { return createCharacter(origin).setID(id); }

	public static MessageBufferOLD<Character> create(char id, ByteBuf wrapper) { return createCharacter(wrapper).setID(id); }

	public static MessageBufferOLD<Character> createCharacter() { return new MessageBufferOLD.CharacterMessage(); }

	public static MessageBufferOLD<Character> createCharacter(PacketBuffer origin) { return new MessageBufferOLD.CharacterMessage(origin); }

	public static MessageBufferOLD<Character> createCharacter(ByteBuf wrapper) { return new MessageBufferOLD.CharacterMessage(wrapper); }

	/* Time */
	public static MessageBufferOLD<Date> create(Date id) { return createTime().setID(id); }

	public static MessageBufferOLD<Date> create(Date id, PacketBuffer origin) { return createTime(origin).setID(id); }

	public static MessageBufferOLD<Date> create(Date id, ByteBuf wrapper) { return createTime(wrapper).setID(id); }

	public static MessageBufferOLD<Date> createTime() { return new MessageBufferOLD.TimeMessage(); }

	public static MessageBufferOLD<Date> createTime(PacketBuffer origin) { return new MessageBufferOLD.TimeMessage(origin); }

	public static MessageBufferOLD<Date> createTime(ByteBuf wrapper) { return new MessageBufferOLD.TimeMessage(wrapper); }

	/* UUID */
	public static MessageBufferOLD<UUID> create(UUID id) { return createUUID().setID(id); }

	public static MessageBufferOLD<UUID> create(UUID id, PacketBuffer origin) { return createUUID(origin).setID(id); }

	public static MessageBufferOLD<UUID> create(UUID id, ByteBuf wrapper) { return createUUID(wrapper).setID(id); }

	public static MessageBufferOLD<UUID> createUUID() { return new MessageBufferOLD.UUIDMessage(); }

	public static MessageBufferOLD<UUID> createUUID(PacketBuffer origin) { return new MessageBufferOLD.UUIDMessage(origin); }

	public static MessageBufferOLD<UUID> createUUID(ByteBuf wrapper) { return new MessageBufferOLD.UUIDMessage(wrapper); }

	/* Generic */
	@SuppressWarnings("unchecked")
	public static <D extends IIdentifier> MessageBufferOLD<D> create(D id) { return createGeneric((Class<D>) id.getClass()).setID(id); }

	@SuppressWarnings("unchecked")
	public static <D extends IIdentifier> MessageBufferOLD<D> create(D id, PacketBuffer origin) { return createGeneric((Class<D>) id.getClass(), origin).setID(id); }

	@SuppressWarnings("unchecked")
	public static <D extends IIdentifier> MessageBufferOLD<D> create(D id, ByteBuf wrapper) { return createGeneric((Class<D>) id.getClass(), wrapper).setID(id); }

	public static <D extends IIdentifier> MessageBufferOLD<D> createGeneric(Class<D> idClazz) { return new MessageBufferOLD.GenericMessage<>(idClazz); }

	public static <D extends IIdentifier> MessageBufferOLD<D> createGeneric(Class<D> idClazz, PacketBuffer origin) { return new MessageBufferOLD.GenericMessage<>(idClazz, origin); }

	public static <D extends IIdentifier> MessageBufferOLD<D> createGeneric(Class<D> idClazz, ByteBuf wrapper) { return new MessageBufferOLD.GenericMessage<>(idClazz, wrapper); }

}
