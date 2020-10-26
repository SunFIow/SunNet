package com.ªtest.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.UUID;

import javax.activation.UnsupportedDataTypeException;

import io.netty.buffer.ByteBuf;

public abstract class MessageBuffer<T> extends PacketBuffer {

	private T id;

	private MessageBuffer() { super(); }

	private MessageBuffer(PacketBuffer origin) { super(origin); }

	private MessageBuffer(ByteBuf wrapped) { super(wrapped); }

	public T getID() { return id; }

	public MessageBuffer<T> setID(T id) { this.id = id; return this; }

	@Override
	public void write(OutputStream out) throws IOException {
		boolean hasID = id != null;
		PacketBuffer idbuffer = new PacketBuffer();

		idbuffer.writeBoolean(hasID);
		if (hasID) writeID(idbuffer);
		idbuffer.write(out);

		super.write(out);
	}

	@Override
	public void read(InputStream in) throws IOException {
		super.read(in);
		boolean hasID = readBoolean();
		if (hasID) readID();
	}

	protected abstract PacketBuffer writeID(PacketBuffer idbuffer);

	protected abstract void readID();

	public static class EnumMessage<E extends Enum<E>> extends MessageBuffer<E> {
		private final Class<E> idClazz;

		public EnumMessage(Class<E> idClazz) { super(); this.idClazz = idClazz; }

		public EnumMessage(PacketBuffer origin, Class<E> idClazz) { super(origin); this.idClazz = idClazz; }

		public EnumMessage(ByteBuf wrapped, Class<E> idClazz) { super(wrapped); this.idClazz = idClazz; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
			idbuffer.writeEnumValue(super.id);
			return idbuffer;
		}

		@Override
		protected void readID() { setID(readEnumValue(idClazz)); }
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
		protected void readID() { setID(readBoolean()); }
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
		protected void readID() { setID(readByte()); }
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
		protected void readID() { setID(readShort()); }
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
		protected void readID() { setID(readVarInt()); }
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
		protected void readID() { setID(readVarLong()); }
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
		protected void readID() { setID(readFloat()); }
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
		protected void readID() { setID(readDouble()); }
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
		protected void readID() { setID(readChar()); }
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
		protected void readID() { setID(readString()); }
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
		protected void readID() { setID(readTime()); }
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
		protected void readID() { setID(readUniqueId()); }
	}

	public static class GenericMessage<G extends Data> extends MessageBuffer<G> {

		private final Class<G> idClazz;

		public GenericMessage(Class<G> idClazz) { super(); this.idClazz = idClazz; }

		public GenericMessage(PacketBuffer origin, Class<G> idClazz) { super(origin); this.idClazz = idClazz; }

		public GenericMessage(ByteBuf wrapped, Class<G> idClazz) { super(wrapped); this.idClazz = idClazz; }

		@Override
		protected PacketBuffer writeID(PacketBuffer idbuffer) {
//			idbuffer.write(super.id);
			super.id.write(idbuffer);
			return idbuffer;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void readID() {
			try {
				System.out.println("clazz: " + idClazz);
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

	/*
	 * Generic Class
	 */
	@SuppressWarnings("unchecked")
	public static <C, E extends Enum<E>, G extends Data> MessageBuffer<C> createMessage(Class<C> idClazz) throws UnsupportedDataTypeException {
		if (Enum.class.isAssignableFrom(idClazz))
			return (MessageBuffer<C>) new MessageBuffer.EnumMessage<E>((Class<E>) idClazz);
		if (idClazz == Boolean.class || idClazz == boolean.class)
			return (MessageBuffer<C>) new MessageBuffer.BooleanMessage();
		if (idClazz == Byte.class || idClazz == byte.class)
			return (MessageBuffer<C>) new MessageBuffer.ByteMessage();
		if (idClazz == Short.class || idClazz == short.class)
			return (MessageBuffer<C>) new MessageBuffer.ShortMessage();
		if (idClazz == Integer.class || idClazz == int.class)
			return (MessageBuffer<C>) new MessageBuffer.IntegerMessage();
		if (idClazz == Long.class || idClazz == long.class)
			return (MessageBuffer<C>) new MessageBuffer.LongMessage();
		if (idClazz == Float.class || idClazz == float.class)
			return (MessageBuffer<C>) new MessageBuffer.FloatMessage();
		if (idClazz == Double.class || idClazz == double.class)
			return (MessageBuffer<C>) new MessageBuffer.DoubleMessage();
		if (idClazz == Character.class || idClazz == char.class)
			return (MessageBuffer<C>) new MessageBuffer.CharacterMessage();
		if (idClazz == String.class)
			return (MessageBuffer<C>) new MessageBuffer.StringMessage();
		if (Date.class.isAssignableFrom(idClazz))
			return (MessageBuffer<C>) new MessageBuffer.TimeMessage();
		if (idClazz == UUID.class)
			return (MessageBuffer<C>) new MessageBuffer.UUIDMessage();
		if (Data.class.isAssignableFrom(idClazz))
			return (MessageBuffer<C>) new MessageBuffer.GenericMessage<G>((Class<G>) idClazz);

		throw new UnsupportedDataTypeException(
				"[" + idClazz + "] is not a supported MessageBuffer id\n\t\t" +
						"supported types are" + "\n\t\t" +
						"[" + Enum.class + "]" + "\n\t\t" +
						"[" + Boolean.class + "]" +
						"[" + Integer.class + "]" +
						"[" + Float.class + "]" +
						"[" + Double.class + "]" + "\n\t\t" +
						"[" + Character.class + "]" +
						"[" + String.class + "]" + "\n\t\t" +
						"[" + Date.class + "]" +
						"[" + UUID.class + "]" + "\n\t\t" +
						"[" + Data.class + "]");
	}

	@SuppressWarnings("unchecked")
	public static <C, E extends Enum<E>, G extends Data> MessageBuffer<C> createMessage(Class<C> idClazz, PacketBuffer origin) throws UnsupportedDataTypeException {
		if (Enum.class.isAssignableFrom(idClazz))
			return (MessageBuffer<C>) new MessageBuffer.EnumMessage<E>(origin, (Class<E>) idClazz);
		if (idClazz == Boolean.class || idClazz == boolean.class)
			return (MessageBuffer<C>) new MessageBuffer.BooleanMessage(origin);
		if (idClazz == Byte.class || idClazz == byte.class)
			return (MessageBuffer<C>) new MessageBuffer.ByteMessage(origin);
		if (idClazz == Short.class || idClazz == short.class)
			return (MessageBuffer<C>) new MessageBuffer.ShortMessage(origin);
		if (idClazz == Integer.class || idClazz == int.class)
			return (MessageBuffer<C>) new MessageBuffer.IntegerMessage(origin);
		if (idClazz == Long.class || idClazz == long.class)
			return (MessageBuffer<C>) new MessageBuffer.LongMessage(origin);
		if (idClazz == Float.class || idClazz == float.class)
			return (MessageBuffer<C>) new MessageBuffer.FloatMessage(origin);
		if (idClazz == Double.class || idClazz == double.class)
			return (MessageBuffer<C>) new MessageBuffer.DoubleMessage(origin);
		if (idClazz == Character.class || idClazz == char.class)
			return (MessageBuffer<C>) new MessageBuffer.CharacterMessage(origin);
		if (idClazz == String.class)
			return (MessageBuffer<C>) new MessageBuffer.StringMessage(origin);
		if (Date.class.isAssignableFrom(idClazz))
			return (MessageBuffer<C>) new MessageBuffer.TimeMessage(origin);
		if (idClazz == UUID.class)
			return (MessageBuffer<C>) new MessageBuffer.UUIDMessage(origin);
		if (Data.class.isAssignableFrom(idClazz))
			return (MessageBuffer<C>) new MessageBuffer.GenericMessage<G>(origin, (Class<G>) idClazz);

		throw new UnsupportedDataTypeException("[" + idClazz + "] is not a supported MessageBuffer id\n\t\t" +
				"supported classes are" + "\n\t\t" +
				"[" + Enum.class + "]" + "\n\t\t" +
				"[" + Boolean.class + "]" +
				"[" + Integer.class + "]" +
				"[" + Float.class + "]" +
				"[" + Double.class + "]" + "\n\t\t" +
				"[" + Character.class + "]" +
				"[" + String.class + "]" + "\n\t\t" +
				"[" + Date.class + "]" +
				"[" + UUID.class + "]" + "\n\t\t" +
				"[" + Data.class + "]");
	}

	@SuppressWarnings("unchecked")
	public static <C, E extends Enum<E>, G extends Data> MessageBuffer<C> createMessage(Class<C> idClazz, ByteBuf wrapper) throws UnsupportedDataTypeException {
		if (Enum.class.isAssignableFrom(idClazz))
			return (MessageBuffer<C>) new MessageBuffer.EnumMessage<E>(wrapper, (Class<E>) idClazz);
		if (idClazz == Boolean.class || idClazz == boolean.class)
			return (MessageBuffer<C>) new MessageBuffer.BooleanMessage(wrapper);
		if (idClazz == Byte.class || idClazz == byte.class)
			return (MessageBuffer<C>) new MessageBuffer.ByteMessage(wrapper);
		if (idClazz == Short.class || idClazz == short.class)
			return (MessageBuffer<C>) new MessageBuffer.ShortMessage(wrapper);
		if (idClazz == Integer.class || idClazz == int.class)
			return (MessageBuffer<C>) new MessageBuffer.IntegerMessage(wrapper);
		if (idClazz == Long.class || idClazz == long.class)
			return (MessageBuffer<C>) new MessageBuffer.LongMessage(wrapper);
		if (idClazz == Float.class || idClazz == float.class)
			return (MessageBuffer<C>) new MessageBuffer.FloatMessage(wrapper);
		if (idClazz == Double.class || idClazz == double.class)
			return (MessageBuffer<C>) new MessageBuffer.DoubleMessage(wrapper);
		if (idClazz == Character.class || idClazz == char.class)
			return (MessageBuffer<C>) new MessageBuffer.CharacterMessage(wrapper);
		if (idClazz == String.class)
			return (MessageBuffer<C>) new MessageBuffer.StringMessage(wrapper);
		if (Date.class.isAssignableFrom(idClazz))
			return (MessageBuffer<C>) new MessageBuffer.TimeMessage(wrapper);
		if (idClazz == UUID.class)
			return (MessageBuffer<C>) new MessageBuffer.UUIDMessage(wrapper);
		if (Data.class.isAssignableFrom(idClazz))
			return (MessageBuffer<C>) new MessageBuffer.GenericMessage<G>(wrapper, (Class<G>) idClazz);

		throw new UnsupportedDataTypeException("[" + idClazz + "] is not a supported MessageBuffer id\n\t\t" +
				"supported classes are" + "\n\t\t" +
				"[" + Enum.class + "]" + "\n\t\t" +
				"[" + Boolean.class + "]" +
				"[" + Integer.class + "]" +
				"[" + Float.class + "]" +
				"[" + Double.class + "]" + "\n\t\t" +
				"[" + Character.class + "]" +
				"[" + String.class + "]" + "\n\t\t" +
				"[" + Date.class + "]" +
				"[" + UUID.class + "]" + "\n\t\t" +
				"[" + Data.class + "]");
	}

	/* Enum */

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> MessageBuffer<E> createMessage(E id) {
		return new MessageBuffer.EnumMessage<E>((Class<E>) id.getClass()).setID(id);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> MessageBuffer<E> createMessage(E id, PacketBuffer origin) {
		return new MessageBuffer.EnumMessage<E>(origin, (Class<E>) id.getClass()).setID(id);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> MessageBuffer<E> createMessage(E id, ByteBuf wrapper) {
		return new MessageBuffer.EnumMessage<E>(wrapper, (Class<E>) id.getClass()).setID(id);
	}

	/* Boolean */
	public static MessageBuffer<Boolean> createMessage(boolean id) {
		return new MessageBuffer.BooleanMessage().setID(id);
	}

	public static MessageBuffer<Boolean> createMessage(boolean id, PacketBuffer origin) {
		return new MessageBuffer.BooleanMessage(origin).setID(id);
	}

	public static MessageBuffer<Boolean> createMessage(boolean id, ByteBuf wrapper) {
		return new MessageBuffer.BooleanMessage(wrapper).setID(id);
	}

	/* Byte */
	public static MessageBuffer<Byte> createMessage(byte id) {
		return new MessageBuffer.ByteMessage().setID(id);
	}

	public static MessageBuffer<Byte> createMessage(byte id, PacketBuffer origin) {
		return new MessageBuffer.ByteMessage(origin).setID(id);
	}

	public static MessageBuffer<Byte> createMessage(byte id, ByteBuf wrapper) {
		return new MessageBuffer.ByteMessage(wrapper).setID(id);
	}

	/* Short */
	public static MessageBuffer<Short> createMessage(short id) {
		return new MessageBuffer.ShortMessage().setID(id);
	}

	public static MessageBuffer<Short> createMessage(short id, PacketBuffer origin) {
		return new MessageBuffer.ShortMessage(origin).setID(id);
	}

	public static MessageBuffer<Short> createMessage(short id, ByteBuf wrapper) {
		return new MessageBuffer.ShortMessage(wrapper).setID(id);
	}

	/* Integer */
	public static MessageBuffer<Integer> createMessage(int id) {
		return new MessageBuffer.IntegerMessage().setID(id);
	}

	public static MessageBuffer<Integer> createMessage(int id, PacketBuffer origin) {
		return new MessageBuffer.IntegerMessage(origin).setID(id);
	}

	public static MessageBuffer<Integer> createMessage(int id, ByteBuf wrapper) {
		return new MessageBuffer.IntegerMessage(wrapper).setID(id);
	}

	/* Long */
	public static MessageBuffer<Long> createMessage(long id) {
		return new MessageBuffer.LongMessage().setID(id);
	}

	public static MessageBuffer<Long> createMessage(long id, PacketBuffer origin) {
		return new MessageBuffer.LongMessage(origin).setID(id);
	}

	public static MessageBuffer<Long> createMessage(long id, ByteBuf wrapper) {
		return new MessageBuffer.LongMessage(wrapper).setID(id);
	}

	/* Float */
	public static MessageBuffer<Float> createMessage(float id) {
		return new MessageBuffer.FloatMessage().setID(id);
	}

	public static MessageBuffer<Float> createMessage(float id, PacketBuffer origin) {
		return new MessageBuffer.FloatMessage(origin).setID(id);
	}

	public static MessageBuffer<Float> createMessage(float id, ByteBuf wrapper) {
		return new MessageBuffer.FloatMessage(wrapper).setID(id);
	}

	/* Double */
	public static MessageBuffer<Double> createMessage(double id) {
		return new MessageBuffer.DoubleMessage().setID(id);
	}

	public static MessageBuffer<Double> createMessage(double id, PacketBuffer origin) {
		return new MessageBuffer.DoubleMessage(origin).setID(id);
	}

	public static MessageBuffer<Double> createMessage(double id, ByteBuf wrapper) {
		return new MessageBuffer.DoubleMessage(wrapper).setID(id);
	}

	/* Character */
	public static MessageBuffer<Character> createMessage(char id) {
		return new MessageBuffer.CharacterMessage().setID(id);
	}

	public static MessageBuffer<Character> createMessage(char id, PacketBuffer origin) {
		return new MessageBuffer.CharacterMessage(origin).setID(id);
	}

	public static MessageBuffer<Character> createMessage(char id, ByteBuf wrapper) {
		return new MessageBuffer.CharacterMessage(wrapper).setID(id);
	}

	/* String */
	public static MessageBuffer<String> createMessage(String id) {
		return new MessageBuffer.StringMessage().setID(id);
	}

	public static MessageBuffer<String> createMessage(String id, PacketBuffer origin) {
		return new MessageBuffer.StringMessage(origin).setID(id);
	}

	public static MessageBuffer<String> createMessage(String id, ByteBuf wrapper) {
		return new MessageBuffer.StringMessage(wrapper).setID(id);
	}

	/* Time */
	public static MessageBuffer<Date> createMessage(Date id) {
		return new MessageBuffer.TimeMessage().setID(id);
	}

	public static MessageBuffer<Date> createMessage(Date id, PacketBuffer origin) {
		return new MessageBuffer.TimeMessage(origin).setID(id);
	}

	public static MessageBuffer<Date> createMessage(Date id, ByteBuf wrapper) {
		return new MessageBuffer.TimeMessage(wrapper).setID(id);
	}

	/* UUID */
	public static MessageBuffer<UUID> createMessage(UUID id) {
		return new MessageBuffer.UUIDMessage().setID(id);
	}

	public static MessageBuffer<UUID> createMessage(UUID id, PacketBuffer origin) {
		return new MessageBuffer.UUIDMessage(origin).setID(id);
	}

	public static MessageBuffer<UUID> createMessage(UUID id, ByteBuf wrapper) {
		return new MessageBuffer.UUIDMessage(wrapper).setID(id);
	}

	/* MessageType */
	@SuppressWarnings("unchecked")
	public static <G extends Data> MessageBuffer<G> createMessage(G id) {
		return new MessageBuffer.GenericMessage<G>((Class<G>) id.getClass()).setID(id);
	}

	@SuppressWarnings("unchecked")
	public static <G extends Data> MessageBuffer<G> createMessage(G id, PacketBuffer origin) {
		return new MessageBuffer.GenericMessage<G>(origin, (Class<G>) id.getClass()).setID(id);
	}

	@SuppressWarnings("unchecked")
	public static <G extends Data> MessageBuffer<G> createMessage(G id, ByteBuf wrapper) {
		return new MessageBuffer.GenericMessage<G>(wrapper, (Class<G>) id.getClass()).setID(id);
	}

}
