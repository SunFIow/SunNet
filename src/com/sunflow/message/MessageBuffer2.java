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

public abstract class MessageBuffer2<T> extends PacketBuffer {
	public static class Owned<T> {

		private Connection<T> remote;
		private MessageBuffer2<T> msg;

		public Owned(Connection<T> connection, MessageBuffer2<T> m_msgTemporaryIn) {
			this.remote = connection;
			this.msg = m_msgTemporaryIn;
		}

		@Override
		public String toString() {
			return getMessage().toString();
		}

		public Connection<T> getRemote() { return remote; }

		public MessageBuffer2<T> getMessage() { return msg; }
	}

	@Override
	public String toString() {
		return "MessageBuffer<" + id + ">{" + super.toString() + "}";
	}

	protected T id;

	MessageBuffer2() { super(); }

	MessageBuffer2(MessageBuffer2<T> origin) { super(origin); }

	MessageBuffer2(PacketBuffer origin) { super(origin); }

	MessageBuffer2(ByteBuf wrapped) { super(wrapped); }

	public T getID() { return id; }

	public MessageBuffer2<T> setID(T id) { this.id = id; return this; }

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

	public static class EnumMessage<E extends Enum<E>> extends MessageBuffer2<E> {
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

	public static class BooleanMessage extends MessageBuffer2<Boolean> {

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

	public static class ByteMessage extends MessageBuffer2<Byte> {

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

	public static class ShortMessage extends MessageBuffer2<Short> {

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

	public static class IntegerMessage extends MessageBuffer2<Integer> {

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

	public static class LongMessage extends MessageBuffer2<Long> {

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

	public static class FloatMessage extends MessageBuffer2<Float> {

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

	public static class DoubleMessage extends MessageBuffer2<Double> {

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

	public static class CharacterMessage extends MessageBuffer2<Character> {

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

	public static class TimeMessage extends MessageBuffer2<Date> {

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

	public static class UUIDMessage extends MessageBuffer2<UUID> {

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

	public static class GenericMessage<G extends IIdentifier> extends MessageBuffer2<G> {

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
	public static <E extends Enum<E>> MessageBuffer2<E> create(E id) { return createEnum(id.getDeclaringClass()).setID(id); }

	public static <E extends Enum<E>> MessageBuffer2<E> create(E id, PacketBuffer origin) { return createEnum(id.getDeclaringClass(), origin).setID(id); }

	public static <E extends Enum<E>> MessageBuffer2<E> create(E id, ByteBuf wrapper) { return createEnum(id.getDeclaringClass(), wrapper).setID(id); }

	public static <E extends Enum<E>> MessageBuffer2<E> createEnum(Class<E> idClazz) { return new MessageBuffer2.EnumMessage<E>(idClazz); }

	public static <E extends Enum<E>> MessageBuffer2<E> createEnum(Class<E> idClazz, PacketBuffer origin) { return new MessageBuffer2.EnumMessage<E>(idClazz, origin); }

	public static <E extends Enum<E>> MessageBuffer2<E> createEnum(Class<E> idClazz, ByteBuf wrapper) { return new MessageBuffer2.EnumMessage<E>(idClazz, wrapper); }

	/* Boolean */
	public static MessageBuffer2<Boolean> create(boolean id) { return createBoolean().setID(id); }

	public static MessageBuffer2<Boolean> create(boolean id, PacketBuffer origin) { return createBoolean(origin).setID(id); }

	public static MessageBuffer2<Boolean> create(boolean id, ByteBuf wrapper) { return createBoolean(wrapper).setID(id); }

	public static MessageBuffer2<Boolean> createBoolean() { return new MessageBuffer2.BooleanMessage(); }

	public static MessageBuffer2<Boolean> createBoolean(PacketBuffer origin) { return new MessageBuffer2.BooleanMessage(origin); }

	public static MessageBuffer2<Boolean> createBoolean(ByteBuf wrapper) { return new MessageBuffer2.BooleanMessage(wrapper); }

	/* Byte */
	public static MessageBuffer2<Byte> create(byte id) { return createByte().setID(id); }

	public static MessageBuffer2<Byte> create(byte id, PacketBuffer origin) { return createByte(origin).setID(id); }

	public static MessageBuffer2<Byte> create(byte id, ByteBuf wrapper) { return createByte(wrapper).setID(id); }

	public static MessageBuffer2<Byte> createByte() { return new MessageBuffer2.ByteMessage(); }

	public static MessageBuffer2<Byte> createByte(PacketBuffer origin) { return new MessageBuffer2.ByteMessage(origin); }

	public static MessageBuffer2<Byte> createByte(ByteBuf wrapper) { return new MessageBuffer2.ByteMessage(wrapper); }

	/* Short */
	public static MessageBuffer2<Short> create(short id) { return createShort().setID(id); }

	public static MessageBuffer2<Short> create(short id, PacketBuffer origin) { return createShort(origin).setID(id); }

	public static MessageBuffer2<Short> create(short id, ByteBuf wrapper) { return createShort(wrapper).setID(id); }

	public static MessageBuffer2<Short> createShort() { return new MessageBuffer2.ShortMessage(); }

	public static MessageBuffer2<Short> createShort(PacketBuffer origin) { return new MessageBuffer2.ShortMessage(origin); }

	public static MessageBuffer2<Short> createShort(ByteBuf wrapper) { return new MessageBuffer2.ShortMessage(wrapper); }

	/* Integer */
	public static MessageBuffer2<Integer> create(int id) { return createInteger().setID(id); }

	public static MessageBuffer2<Integer> create(int id, PacketBuffer origin) { return createInteger(origin).setID(id); }

	public static MessageBuffer2<Integer> create(int id, ByteBuf wrapper) { return createInteger(wrapper).setID(id); }

	public static MessageBuffer2<Integer> createInteger() { return new MessageBuffer2.IntegerMessage(); }

	public static MessageBuffer2<Integer> createInteger(PacketBuffer origin) { return new MessageBuffer2.IntegerMessage(origin); }

	public static MessageBuffer2<Integer> createInteger(ByteBuf wrapper) { return new MessageBuffer2.IntegerMessage(wrapper); }

	/* Long */
	public static MessageBuffer2<Long> create(long id) { return createLong().setID(id); }

	public static MessageBuffer2<Long> create(long id, PacketBuffer origin) { return createLong(origin).setID(id); }

	public static MessageBuffer2<Long> create(long id, ByteBuf wrapper) { return createLong(wrapper).setID(id); }

	public static MessageBuffer2<Long> createLong() { return new MessageBuffer2.LongMessage(); }

	public static MessageBuffer2<Long> createLong(PacketBuffer origin) { return new MessageBuffer2.LongMessage(origin); }

	public static MessageBuffer2<Long> createLong(ByteBuf wrapper) { return new MessageBuffer2.LongMessage(wrapper); }

	/* Float */
	public static MessageBuffer2<Float> create(float id) { return createFloat().setID(id); }

	public static MessageBuffer2<Float> create(float id, PacketBuffer origin) { return createFloat(origin).setID(id); }

	public static MessageBuffer2<Float> create(float id, ByteBuf wrapper) { return createFloat(wrapper).setID(id); }

	public static MessageBuffer2<Float> createFloat() { return new MessageBuffer2.FloatMessage(); }

	public static MessageBuffer2<Float> createFloat(PacketBuffer origin) { return new MessageBuffer2.FloatMessage(origin); }

	public static MessageBuffer2<Float> createFloat(ByteBuf wrapper) { return new MessageBuffer2.FloatMessage(wrapper); }

	/* Double */
	public static MessageBuffer2<Double> create(double id) { return createDouble().setID(id); }

	public static MessageBuffer2<Double> create(double id, PacketBuffer origin) { return createDouble(origin).setID(id); }

	public static MessageBuffer2<Double> create(double id, ByteBuf wrapper) { return createDouble(wrapper).setID(id); }

	public static MessageBuffer2<Double> createDouble() { return new MessageBuffer2.DoubleMessage(); }

	public static MessageBuffer2<Double> createDouble(PacketBuffer origin) { return new MessageBuffer2.DoubleMessage(origin); }

	public static MessageBuffer2<Double> createDouble(ByteBuf wrapper) { return new MessageBuffer2.DoubleMessage(wrapper); }

	/* Character */
	public static MessageBuffer2<Character> create(char id) { return createCharacter().setID(id); }

	public static MessageBuffer2<Character> create(char id, PacketBuffer origin) { return createCharacter(origin).setID(id); }

	public static MessageBuffer2<Character> create(char id, ByteBuf wrapper) { return createCharacter(wrapper).setID(id); }

	public static MessageBuffer2<Character> createCharacter() { return new MessageBuffer2.CharacterMessage(); }

	public static MessageBuffer2<Character> createCharacter(PacketBuffer origin) { return new MessageBuffer2.CharacterMessage(origin); }

	public static MessageBuffer2<Character> createCharacter(ByteBuf wrapper) { return new MessageBuffer2.CharacterMessage(wrapper); }

	/* Time */
	public static MessageBuffer2<Date> create(Date id) { return createTime().setID(id); }

	public static MessageBuffer2<Date> create(Date id, PacketBuffer origin) { return createTime(origin).setID(id); }

	public static MessageBuffer2<Date> create(Date id, ByteBuf wrapper) { return createTime(wrapper).setID(id); }

	public static MessageBuffer2<Date> createTime() { return new MessageBuffer2.TimeMessage(); }

	public static MessageBuffer2<Date> createTime(PacketBuffer origin) { return new MessageBuffer2.TimeMessage(origin); }

	public static MessageBuffer2<Date> createTime(ByteBuf wrapper) { return new MessageBuffer2.TimeMessage(wrapper); }

	/* UUID */
	public static MessageBuffer2<UUID> create(UUID id) { return createUUID().setID(id); }

	public static MessageBuffer2<UUID> create(UUID id, PacketBuffer origin) { return createUUID(origin).setID(id); }

	public static MessageBuffer2<UUID> create(UUID id, ByteBuf wrapper) { return createUUID(wrapper).setID(id); }

	public static MessageBuffer2<UUID> createUUID() { return new MessageBuffer2.UUIDMessage(); }

	public static MessageBuffer2<UUID> createUUID(PacketBuffer origin) { return new MessageBuffer2.UUIDMessage(origin); }

	public static MessageBuffer2<UUID> createUUID(ByteBuf wrapper) { return new MessageBuffer2.UUIDMessage(wrapper); }

	/* Generic */
	@SuppressWarnings("unchecked")
	public static <D extends IIdentifier> MessageBuffer2<D> create(D id) { return createGeneric((Class<D>) id.getClass()).setID(id); }

	@SuppressWarnings("unchecked")
	public static <D extends IIdentifier> MessageBuffer2<D> create(D id, PacketBuffer origin) { return createGeneric((Class<D>) id.getClass(), origin).setID(id); }

	@SuppressWarnings("unchecked")
	public static <D extends IIdentifier> MessageBuffer2<D> create(D id, ByteBuf wrapper) { return createGeneric((Class<D>) id.getClass(), wrapper).setID(id); }

	public static <D extends IIdentifier> MessageBuffer2<D> createGeneric(Class<D> idClazz) { return new MessageBuffer2.GenericMessage<>(idClazz); }

	public static <D extends IIdentifier> MessageBuffer2<D> createGeneric(Class<D> idClazz, PacketBuffer origin) { return new MessageBuffer2.GenericMessage<>(idClazz, origin); }

	public static <D extends IIdentifier> MessageBuffer2<D> createGeneric(Class<D> idClazz, ByteBuf wrapper) { return new MessageBuffer2.GenericMessage<>(idClazz, wrapper); }

}
