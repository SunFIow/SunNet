package com.ªtest.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SunNetNetty {
	public static class TestClass implements Data {

//		public TestClass() {}

		public TestClass(String s) {}

		@Override
		public void write(PacketBuffer buffer) {

		}

		@Override
		public void read(PacketBuffer buffer) {

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

//		MessageBuffer<Boolean> mb = MessageBuffer.createMessage(true);
//		MessageBuffer<Integer> mb = MessageBuffer.createMessage(10);
//		MessageBuffer<TestEnum> mb = MessageBuffer.createMessage(TestEnum.class);
		TestClass tc = new TestClass("");
		MessageBuffer<TestClass> mb = MessageBuffer.create(tc);
//		MessageBuffer<TestClass> mb = MessageBuffer.createGeneric(TestClass.class);
//		mb.setID(tc);
		System.out.println(mb.getID());

//		mb.writeEnumValue(TestEnum.two);
		long now = System.currentTimeMillis();
		System.out.println(now);
		mb.writeLong(now);

		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		BufferedOutputStream bout = new BufferedOutputStream(baout);

		mb.write(bout);
		bout.close();

		byte[] bytes = baout.toByteArray();
		System.out.println(bytes.length);

//		mb = MessageBuffer.createMessage(Boolean.class);
//		mb = MessageBuffer.createMessage(Integer.class);
//		mb = MessageBuffer.createMessage(TestEnum.class);
		mb = MessageBuffer.createGeneric(TestClass.class);

		ByteArrayInputStream bain = new ByteArrayInputStream(bytes);
		BufferedInputStream bin = new BufferedInputStream(bain);

		// THREE
		mb.read(bin);
		// ONE
		System.out.println(mb.getID());
		System.out.println(mb.readLong());
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
