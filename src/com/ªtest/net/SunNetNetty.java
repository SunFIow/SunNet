package com.ªtest.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.$impl.CustomMsgTypes;

public class SunNetNetty {
	public static class TestClass implements Data {

		private String name;
//		public TestClass() {}

		public TestClass(String s) { name = s; }

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeString(name);
		}

		@Override
		public void read(PacketBuffer buffer) {
			name = buffer.readString();
		}

		@Override
		public String toString() {
			return "[" + name + "]";
		}
	}

	private enum TestEnum {
		zero, one, two, three
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException { new SunNetNetty(); }

	public SunNetNetty() throws ClassNotFoundException, IOException {
		bla1();
	}

	public void bla0() throws IOException, ClassNotFoundException {
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		BufferedOutputStream bout = new BufferedOutputStream(baout);
//		ObjectEncoderOutputStream out = new ObjectEncoderOutputStream(bout);
		ObjectOutputStream out = new ObjectOutputStream(bout);

		out.writeObject(TestEnum.two);
		out.close();

		byte[] bytes = baout.toByteArray();
		System.out.println(bytes.length);

		ByteArrayInputStream bain = new ByteArrayInputStream(bytes);
		BufferedInputStream bin = new BufferedInputStream(bain);
//		ObjectDecoderInputStream in = new ObjectDecoderInputStream(bin);
		ObjectInputStream in = new ObjectInputStream(bin);

		Object o = in.readObject();
		System.out.println(o);

	}

	public void bla1() throws IOException, ClassNotFoundException {
		// MessageBuffer<TestEnum> buffer = MessageBuffer.createMessage(TestEnum.two);
		// MessageBuffer<Boolean> buffer = MessageBuffer.createMessage(true);
		// MessageBuffer<Byte> buffer = MessageBuffer.createMessage((byte) 1);
		// MessageBuffer<Short> buffer = MessageBuffer.createMessage((short) 2);
		// MessageBuffer<Integer> buffer = MessageBuffer.createMessage(3);
		// MessageBuffer<Long> buffer = MessageBuffer.createMessage(4L);
		// MessageBuffer<Float> buffer = MessageBuffer.createMessage(5.5F);
		// MessageBuffer<Double> buffer = MessageBuffer.createMessage(6.6D);
		// MessageBuffer<Character> buffer = MessageBuffer.createMessage('C');
		// MessageBuffer<String> buffer = MessageBuffer.createMessage("String");
		// MessageBuffer<Date> buffer = MessageBuffer.createMessage(new Date());
		// MessageBuffer<UUID> buffer = MessageBuffer.createMessage(UUID.randomUUID());
		// buffer.setID(TestEnum.two);
		// buffer.writeID();
		// buffer.readID();
		// System.out.println(buffer.getID());

		/*
		 * MessageBuffer Testing END
		 */

//		MessageBuffer<Character> mb = new MessageBuffer<>('p');
//		mb.setID(TestEnum.two);

//		MessageBuffer<TestClass> mb = new MessageBuffer<>(
//				ReadWrite::write,
//				ReadWrite::read);

//		MessageBuffer<Boolean> mb = MessageBuffer.create(true);
//		MessageBuffer<Integer> mb = MessageBuffer.create(10);
		MessageBuffer<CustomMsgTypes> mb = MessageBuffer.create(CustomMsgTypes.ServerPing);
//		MessageBuffer<TestClass> mb = MessageBuffer.create(new TestClass("TestName");
		System.out.println("0: " + mb.getID());

//		mb.writeEnumValue(TestEnum.two);
		long now = System.currentTimeMillis();
		System.out.println("1: " + now);
		mb.writeVarLong(now);

		ByteArrayOutputStream baout = new ByteArrayOutputStream();
//		BufferedOutputStream bout = new BufferedOutputStream(baout);
		OutputStream out = baout;
		int length = mb.write(out);
		out.close();

		byte[] bytes = baout.toByteArray();
		System.out.println("2: " + bytes.length + ", " + length);

//		mb = MessageBuffer.createMessage(Boolean.class);
//		mb = MessageBuffer.createMessage(Integer.class);
//		mb = MessageBuffer.createMessage(TestEnum.class);
//		MessageBuffer<TestClass> temp = MessageBuffer.createGeneric(TestClass.class);
		MessageBuffer<CustomMsgTypes> temp = MessageBuffer.createEnum(CustomMsgTypes.class);

		ByteArrayInputStream bain = new ByteArrayInputStream(bytes);
		BufferedInputStream bin = new BufferedInputStream(bain);

		temp.read(bin);
		System.out.println("4: " + temp.getID());
		System.out.println("7: " + temp.readVarLong());

		baout = new ByteArrayOutputStream();
//		bout = new BufferedOutputStream(baout);
		out = baout;

		mb.writeLong(now);
		length = mb.write(out);
		out.flush();

//		mb.write(bout);
//		bout.flush();

		bytes = baout.toByteArray();
		System.out.println("3: " + bytes.length + ", " + length);

		bain = new ByteArrayInputStream(bytes);
		bin = new BufferedInputStream(bain);

		temp.read(bin);
		System.out.println("4: " + temp.getID());
		System.out.println("7: " + temp.readLong());

//		System.out.println(mb.readEnumValue(TestEnum.class));

		/*
		 * PacketBuffer Networking
		 */

//		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
//		buffer.writeEnumValue(TestEnum.three);
//
//		PacketBuffer tempMSG = buffer;
//		/*
//		 * NETWORKING START
//		 */
//		tempMSG = new PacketBuffer(buffer);
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		OutputStream out = new BufferedOutputStream(bos);
////		BufferedOutputStream bos = new BufferedOutputStream(out);
////		ObjectOutputStream oos = new ObjectOutputStream(bos);
////		oos.writeObject((Message<?>) null);
////		oos.flush();
//
//		tempMSG.write(out);
//		out.close();
//
//		byte[] bytes = bos.toByteArray();
//		System.out.println(bytes.length);
//
//		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//		InputStream in = new BufferedInputStream(bis);
////		BufferedInputStream bis = new BufferedInputStream(in);
////		ObjectInputStream ois = new ObjectInputStream(bis);
////		Message<?> message = (Message<?>) ois.readObject();
//		tempMSG = new PacketBuffer(Unpooled.buffer());
//		tempMSG.read(in);
//		/*
//		 * NETWORKING ENDS
//		 */
//		PacketBuffer message = tempMSG;
//
//		TestEnum id = message.readEnumValue(TestEnum.class);
//		System.out.println(id);
	}
}
