package com.ªtest.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.UUID;

import com.sunflow.util.Logger;

import io.netty.buffer.ByteBuf;

public abstract class MessageBuffer<T> extends PacketBuffer {

	private T id;

	private MessageBuffer() { super(); }

	private MessageBuffer(PacketBuffer origin) { super(origin); }

	private MessageBuffer(ByteBuf wrapped) { super(wrapped); }

	public T getID() { checkID(); return id; }

	public MessageBuffer<T> setID(T id) { this.id = id; return this; }

	@Override
	public int write(OutputStream out) throws IOException {
		boolean hasID = id != null;
		PacketBuffer idbuffer = new PacketBuffer();

		idbuffer.writeBoolean(hasID);
		if (hasID) writeID(idbuffer);
		idbuffer.write(out);

		return super.write(out);
	}

	@Override
	public int read(InputStream in) throws IOException {
		int readBytes = super.read(in);
		return readBytes;
	}

	/* Call this function once to read the ID */
	private void checkID() {
		if (id != null) return;
		boolean hasID = readBoolean();
		if (hasID) _readID();
	}

	protected abstract PacketBuffer writeID(PacketBuffer idbuffer);

	protected abstract void _readID();

	public static class EnumMessage<E extends Enum<E>> extends MessageBuffer<E> {
		private final Class<E> idClazz;

		public EnumMessage(Class<E> idClazz) { super(); this.idClazz = idClazz; }

		public EnumMessage(Class<E> idClazz, PacketBuffer origin) { super(origin); this.idClazz = idClazz; }

		public EnumMessage(Class<E> idClazz, ByteBuf wrapped) { super(wrapped); this.idClazz = idClazz; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeEnumValue(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { Logger.warn("MessageBuffer", idClazz); setID(readEnumValue(idClazz)); }
	}

	public static class BooleanMessage extends MessageBuffer<Boolean> {

		public BooleanMessage() { super(); }

		public BooleanMessage(PacketBuffer origin) { super(origin); }

		public BooleanMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeBoolean(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readBoolean()); }
	}

	public static class ByteMessage extends MessageBuffer<Byte> {

		public ByteMessage() { super(); }

		public ByteMessage(PacketBuffer origin) { super(origin); }

		public ByteMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeByte(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readByte()); }
	}

	public static class ShortMessage extends MessageBuffer<Short> {

		public ShortMessage() { super(); }

		public ShortMessage(PacketBuffer origin) { super(origin); }

		public ShortMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeShort(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readShort()); }
	}

	public static class IntegerMessage extends MessageBuffer<Integer> {

		public IntegerMessage() { super(); }

		public IntegerMessage(PacketBuffer origin) { super(origin); }

		public IntegerMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeVarInt(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readVarInt()); }
	}

	public static class LongMessage extends MessageBuffer<Long> {

		public LongMessage() { super(); }

		public LongMessage(PacketBuffer origin) { super(origin); }

		public LongMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeVarLong(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readVarLong()); }
	}

	public static class FloatMessage extends MessageBuffer<Float> {

		public FloatMessage() { super(); }

		public FloatMessage(PacketBuffer origin) { super(origin); }

		public FloatMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeFloat(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readFloat()); }
	}

	public static class DoubleMessage extends MessageBuffer<Double> {

		public DoubleMessage() { super(); }

		public DoubleMessage(PacketBuffer origin) { super(origin); }

		public DoubleMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeDouble(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readDouble()); }
	}

	public static class CharacterMessage extends MessageBuffer<Character> {

		public CharacterMessage() { super(); }

		public CharacterMessage(PacketBuffer origin) { super(origin); }

		public CharacterMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeChar(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readChar()); }
	}

	public static class StringMessage extends MessageBuffer<String> {

		public StringMessage() { super(); }

		public StringMessage(PacketBuffer origin) { super(origin); }

		public StringMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeString(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readString()); }
	}

	public static class TimeMessage extends MessageBuffer<Date> {

		public TimeMessage() { super(); }

		public TimeMessage(PacketBuffer origin) { super(origin); }

		public TimeMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeTime(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readTime()); }
	}

	public static class UUIDMessage extends MessageBuffer<UUID> {

		public UUIDMessage() { super(); }

		public UUIDMessage(PacketBuffer origin) { super(origin); }

		public UUIDMessage(ByteBuf wrapped) { super(wrapped); }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeUniqueId(super.id);
			return idbuffer;
		}

		@Override
		protected void _readID() { setID(readUniqueId()); }
	}

	public static class GenericMessage<G extends Data> extends MessageBuffer<G> {

		private final Class<G> idClazz;

		public GenericMessage(Class<G> idClazz) { super(); this.idClazz = idClazz; }

		public GenericMessage(Class<G> idClazz, PacketBuffer origin) { super(origin); this.idClazz = idClazz; }

		public GenericMessage(Class<G> idClazz, ByteBuf wrapped) { super(wrapped); this.idClazz = idClazz; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
//			idbuffer.write(super.id);
			super.id.write(idbuffer);
			return idbuffer;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void _readID() {
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
				super.id = (G) ctor.newInstance(new Object[minLen]);
				super.id.read(this);
			} catch (ReflectiveOperationException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	/* Enum */
	public static <E extends Enum<E>> MessageBuffer<E> create(E id) { return createEnum(id.getDeclaringClass()).setID(id); }

	public static <E extends Enum<E>> MessageBuffer<E> create(E id, PacketBuffer origin) { return createEnum(id.getDeclaringClass(), origin).setID(id); }

	public static <E extends Enum<E>> MessageBuffer<E> create(E id, ByteBuf wrapper) { return createEnum(id.getDeclaringClass(), wrapper).setID(id); }

	public static <E extends Enum<E>> MessageBuffer<E> createEnum(Class<E> idClazz) { return new MessageBuffer.EnumMessage<E>(idClazz); }

	public static <E extends Enum<E>> MessageBuffer<E> createEnum(Class<E> idClazz, PacketBuffer origin) { return new MessageBuffer.EnumMessage<E>(idClazz, origin); }

	public static <E extends Enum<E>> MessageBuffer<E> createEnum(Class<E> idClazz, ByteBuf wrapper) { return new MessageBuffer.EnumMessage<E>(idClazz, wrapper); }

	/* Boolean */
	public static MessageBuffer<Boolean> create(boolean id) { return createBoolean().setID(id); }

	public static MessageBuffer<Boolean> create(boolean id, PacketBuffer origin) { return createBoolean(origin).setID(id); }

	public static MessageBuffer<Boolean> create(boolean id, ByteBuf wrapper) { return createBoolean(wrapper).setID(id); }

	public static MessageBuffer<Boolean> createBoolean() { return new MessageBuffer.BooleanMessage(); }

	public static MessageBuffer<Boolean> createBoolean(PacketBuffer origin) { return new MessageBuffer.BooleanMessage(origin); }

	public static MessageBuffer<Boolean> createBoolean(ByteBuf wrapper) { return new MessageBuffer.BooleanMessage(wrapper); }

	/* Byte */
	public static MessageBuffer<Byte> create(byte id) { return createByte().setID(id); }

	public static MessageBuffer<Byte> create(byte id, PacketBuffer origin) { return createByte(origin).setID(id); }

	public static MessageBuffer<Byte> create(byte id, ByteBuf wrapper) { return createByte(wrapper).setID(id); }

	public static MessageBuffer<Byte> createByte() { return new MessageBuffer.ByteMessage(); }

	public static MessageBuffer<Byte> createByte(PacketBuffer origin) { return new MessageBuffer.ByteMessage(origin); }

	public static MessageBuffer<Byte> createByte(ByteBuf wrapper) { return new MessageBuffer.ByteMessage(wrapper); }

	/* Short */
	public static MessageBuffer<Short> create(short id) { return createShort().setID(id); }

	public static MessageBuffer<Short> create(short id, PacketBuffer origin) { return createShort(origin).setID(id); }

	public static MessageBuffer<Short> create(short id, ByteBuf wrapper) { return createShort(wrapper).setID(id); }

	public static MessageBuffer<Short> createShort() { return new MessageBuffer.ShortMessage(); }

	public static MessageBuffer<Short> createShort(PacketBuffer origin) { return new MessageBuffer.ShortMessage(origin); }

	public static MessageBuffer<Short> createShort(ByteBuf wrapper) { return new MessageBuffer.ShortMessage(wrapper); }

	/* Integer */
	public static MessageBuffer<Integer> create(int id) { return createInteger().setID(id); }

	public static MessageBuffer<Integer> create(int id, PacketBuffer origin) { return createInteger(origin).setID(id); }

	public static MessageBuffer<Integer> create(int id, ByteBuf wrapper) { return createInteger(wrapper).setID(id); }

	public static MessageBuffer<Integer> createInteger() { return new MessageBuffer.IntegerMessage(); }

	public static MessageBuffer<Integer> createInteger(PacketBuffer origin) { return new MessageBuffer.IntegerMessage(origin); }

	public static MessageBuffer<Integer> createInteger(ByteBuf wrapper) { return new MessageBuffer.IntegerMessage(wrapper); }

	/* Long */
	public static MessageBuffer<Long> create(long id) { return createLong().setID(id); }

	public static MessageBuffer<Long> create(long id, PacketBuffer origin) { return createLong(origin).setID(id); }

	public static MessageBuffer<Long> create(long id, ByteBuf wrapper) { return createLong(wrapper).setID(id); }

	public static MessageBuffer<Long> createLong() { return new MessageBuffer.LongMessage(); }

	public static MessageBuffer<Long> createLong(PacketBuffer origin) { return new MessageBuffer.LongMessage(origin); }

	public static MessageBuffer<Long> createLong(ByteBuf wrapper) { return new MessageBuffer.LongMessage(wrapper); }

	/* Float */
	public static MessageBuffer<Float> create(float id) { return createFloat().setID(id); }

	public static MessageBuffer<Float> create(float id, PacketBuffer origin) { return createFloat(origin).setID(id); }

	public static MessageBuffer<Float> create(float id, ByteBuf wrapper) { return createFloat(wrapper).setID(id); }

	public static MessageBuffer<Float> createFloat() { return new MessageBuffer.FloatMessage(); }

	public static MessageBuffer<Float> createFloat(PacketBuffer origin) { return new MessageBuffer.FloatMessage(origin); }

	public static MessageBuffer<Float> createFloat(ByteBuf wrapper) { return new MessageBuffer.FloatMessage(wrapper); }

	/* Double */
	public static MessageBuffer<Double> create(double id) { return createDouble().setID(id); }

	public static MessageBuffer<Double> create(double id, PacketBuffer origin) { return createDouble(origin).setID(id); }

	public static MessageBuffer<Double> create(double id, ByteBuf wrapper) { return createDouble(wrapper).setID(id); }

	public static MessageBuffer<Double> createDouble() { return new MessageBuffer.DoubleMessage(); }

	public static MessageBuffer<Double> createDouble(PacketBuffer origin) { return new MessageBuffer.DoubleMessage(origin); }

	public static MessageBuffer<Double> createDouble(ByteBuf wrapper) { return new MessageBuffer.DoubleMessage(wrapper); }

	/* Character */
	public static MessageBuffer<Character> create(char id) { return createCharacter().setID(id); }

	public static MessageBuffer<Character> create(char id, PacketBuffer origin) { return createCharacter(origin).setID(id); }

	public static MessageBuffer<Character> create(char id, ByteBuf wrapper) { return createCharacter(wrapper).setID(id); }

	public static MessageBuffer<Character> createCharacter() { return new MessageBuffer.CharacterMessage(); }

	public static MessageBuffer<Character> createCharacter(PacketBuffer origin) { return new MessageBuffer.CharacterMessage(origin); }

	public static MessageBuffer<Character> createCharacter(ByteBuf wrapper) { return new MessageBuffer.CharacterMessage(wrapper); }

	/* String */
	public static MessageBuffer<String> create(String id) { return createString().setID(id); }

	public static MessageBuffer<String> create(String id, PacketBuffer origin) { return createString(origin).setID(id); }

	public static MessageBuffer<String> create(String id, ByteBuf wrapper) { return createString(wrapper).setID(id); }

	public static MessageBuffer<String> createString() { return new MessageBuffer.StringMessage(); }

	public static MessageBuffer<String> createString(PacketBuffer origin) { return new MessageBuffer.StringMessage(origin); }

	public static MessageBuffer<String> createString(ByteBuf wrapper) { return new MessageBuffer.StringMessage(wrapper); }

	/* Time */
	public static MessageBuffer<Date> create(Date id) { return createTime().setID(id); }

	public static MessageBuffer<Date> create(Date id, PacketBuffer origin) { return createTime(origin).setID(id); }

	public static MessageBuffer<Date> create(Date id, ByteBuf wrapper) { return createTime(wrapper).setID(id); }

	public static MessageBuffer<Date> createTime() { return new MessageBuffer.TimeMessage(); }

	public static MessageBuffer<Date> createTime(PacketBuffer origin) { return new MessageBuffer.TimeMessage(origin); }

	public static MessageBuffer<Date> createTime(ByteBuf wrapper) { return new MessageBuffer.TimeMessage(wrapper); }

	/* UUID */
	public static MessageBuffer<UUID> create(UUID id) { return createUUID().setID(id); }

	public static MessageBuffer<UUID> create(UUID id, PacketBuffer origin) { return createUUID(origin).setID(id); }

	public static MessageBuffer<UUID> create(UUID id, ByteBuf wrapper) { return createUUID(wrapper).setID(id); }

	public static MessageBuffer<UUID> createUUID() { return new MessageBuffer.UUIDMessage(); }

	public static MessageBuffer<UUID> createUUID(PacketBuffer origin) { return new MessageBuffer.UUIDMessage(origin); }

	public static MessageBuffer<UUID> createUUID(ByteBuf wrapper) { return new MessageBuffer.UUIDMessage(wrapper); }

	/* Generic */
	@SuppressWarnings("unchecked")
	public static <D extends Data> MessageBuffer<D> create(D id) { return createGeneric((Class<D>) id.getClass()).setID(id); }

	@SuppressWarnings("unchecked")
	public static <D extends Data> MessageBuffer<D> create(D id, PacketBuffer origin) { return createGeneric((Class<D>) id.getClass(), origin).setID(id); }

	@SuppressWarnings("unchecked")
	public static <D extends Data> MessageBuffer<D> create(D id, ByteBuf wrapper) { return createGeneric((Class<D>) id.getClass(), wrapper).setID(id); }

	public static <D extends Data> MessageBuffer<D> createGeneric(Class<D> idClazz) { return new MessageBuffer.GenericMessage<>(idClazz); }

	public static <D extends Data> MessageBuffer<D> createGeneric(Class<D> idClazz, PacketBuffer origin) { return new MessageBuffer.GenericMessage<>(idClazz, origin); }

	public static <D extends Data> MessageBuffer<D> createGeneric(Class<D> idClazz, ByteBuf wrapper) { return new MessageBuffer.GenericMessage<>(idClazz, wrapper); }

}
