package com.�test.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.$impl.CustomMsgTypes;
import com.sunflow.message.IIdentifier;
import com.sunflow.message.MessageBuffer;
import com.sunflow.message.PacketBuffer;

public class SunNetNetty {
	public static class TestClass implements IIdentifier {

		private String name;
//		public TestClass() {}

		public TestClass(String s) { name = s; }

		@Override
		public int size() { return 54; } // 50 + 4

		@Override
		public void write(PacketBuffer buffer) {
//			buffer.writeString(name);
			int bytes = buffer.writeString(name, 50);
			System.out.println("ByteLegth: " + bytes);
			for (int i = bytes; i < 50; i++) {
				buffer.writeByte(0);
			}
		}

		@Override
		public void read(PacketBuffer buffer) {
			name = buffer.readString(50);
			int bytes = name.getBytes(StandardCharsets.UTF_8).length;
			for (int i = bytes; i < 50; i++) {
				buffer.readByte();
			}
		}

		@Override
		public String toString() {
			return "[" + name + "]";
		}

	}

	private enum TestEnum {
		zero, one, two, three
	}

	public static void main(String[] args) throws Exception { new SunNetNetty(); }

	public SunNetNetty() throws Exception {
		System.out.println("Write BAOS");
		byte[] bytes = writeBAOS();
		System.out.println();

		System.out.println("Write File");
		writeFO("test.sez");
		System.out.println();

		System.out.println("Read BAIS");
		readBAIS(bytes);
		System.out.println();

		System.out.println("Read File");
		readFI("test.sez");
		System.out.println();
	}

	public void bla() {}

	public byte[] writeBAOS() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bla(out);
		return out.toByteArray();
	}

	public void writeFO(String fileName) throws IOException {
		FileOutputStream out = new FileOutputStream(fileName);
		bla(out);
	}

	public void bla(OutputStream out) throws IOException {
//		MessageBuffer<BigEnum> msg = MessageBuffer.create(BigEnum.g91);
		MessageBuffer<TestClass> msg = MessageBuffer.create(new TestClass("TestName"));
		msg.writeVarLong(System.currentTimeMillis());
		int written = msg.write(out);
		out.close();
		System.out.println("written: " + written);
	}

	public void readBAIS(byte[] bytes) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		System.out.println("av: " + in.available());
		bla(in);
	}

	public void readFI(String fileName) throws IOException {
		FileInputStream in = new FileInputStream(fileName);
		System.out.println("av: " + in.available());
		bla(in);
	}

	public void bla(InputStream in) throws IOException {
//		MessageBuffer<BigEnum> msg = MessageBuffer.createEnum(BigEnum.class);
		MessageBuffer<TestClass> msg = MessageBuffer.createGeneric(TestClass.class);
//		while (in.available() < msg.headerSize()) {
//
//		}
		System.out.println("idSize: " + msg.idSize());
		msg.read(in);
		System.out.println(msg.getID());
		System.out.println(msg.readVarLong());
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

	@SuppressWarnings("unused")
	private enum BigEnum {
		a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22, a23, a24, a25, a26, a27, a28, a29, a30, a31, a32, a33, a34, a35, a36, a37, a38, a39, a40, a41, a42, a43, a44, a45, a46, a47, a48, a49, a50, a51, a52, a53, a54, a55, a56, a57, a58, a59, a60, a61, a62, a63, a64, a65, a66, a67, a68, a69, a70, a71, a72, a73, a74, a75, a76, a77, a78, a79, a80, a81, a82, a83, a84, a85, a86, a87, a88, a89, a90, a91, a92, a93, a94, a95, a96, a97, a98, a99,
		b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15, b16, b17, b18, b19, b20, b21, b22, b23, b24, b25, b26, b27, b28, b29, b30, b31, b32, b33, b34, b35, b36, b37, b38, b39, b40, b41, b42, b43, b44, b45, b46, b47, b48, b49, b50, b51, b52, b53, b54, b55, b56, b57, b58, b59, b60, b61, b62, b63, b64, b65, b66, b67, b68, b69, b70, b71, b72, b73, b74, b75, b76, b77, b78, b79, b80, b81, b82, b83, b84, b85, b86, b87, b88, b89, b90, b91, b92, b93, b94, b95, b96, b97, b98, b99,
		c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19, c20, c21, c22, c23, c24, c25, c26, c27, c28, c29, c30, c31, c32, c33, c34, c35, c36, c37, c38, c39, c40, c41, c42, c43, c44, c45, c46, c47, c48, c49, c50, c51, c52, c53, c54, c55, c56, c57, c58, c59, c60, c61, c62, c63, c64, c65, c66, c67, c68, c69, c70, c71, c72, c73, c74, c75, c76, c77, c78, c79, c80, c81, c82, c83, c84, c85, c86, c87, c88, c89, c90, c91, c92, c93, c94, c95, c96, c97, c98, c99,
		d0, d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14, d15, d16, d17, d18, d19, d20, d21, d22, d23, d24, d25, d26, d27, d28, d29, d30, d31, d32, d33, d34, d35, d36, d37, d38, d39, d40, d41, d42, d43, d44, d45, d46, d47, d48, d49, d50, d51, d52, d53, d54, d55, d56, d57, d58, d59, d60, d61, d62, d63, d64, d65, d66, d67, d68, d69, d70, d71, d72, d73, d74, d75, d76, d77, d78, d79, d80, d81, d82, d83, d84, d85, d86, d87, d88, d89, d90, d91, d92, d93, d94, d95, d96, d97, d98, d99,
		e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30, e31, e32, e33, e34, e35, e36, e37, e38, e39, e40, e41, e42, e43, e44, e45, e46, e47, e48, e49, e50, e51, e52, e53, e54, e55, e56, e57, e58, e59, e60, e61, e62, e63, e64, e65, e66, e67, e68, e69, e70, e71, e72, e73, e74, e75, e76, e77, e78, e79, e80, e81, e82, e83, e84, e85, e86, e87, e88, e89, e90, e91, e92, e93, e94, e95, e96, e97, e98, e99,
		f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21, f22, f23, f24, f25, f26, f27, f28, f29, f30, f31, f32, f33, f34, f35, f36, f37, f38, f39, f40, f41, f42, f43, f44, f45, f46, f47, f48, f49, f50, f51, f52, f53, f54, f55, f56, f57, f58, f59, f60, f61, f62, f63, f64, f65, f66, f67, f68, f69, f70, f71, f72, f73, f74, f75, f76, f77, f78, f79, f80, f81, f82, f83, f84, f85, f86, f87, f88, f89, f90, f91, f92, f93, f94, f95, f96, f97, f98, f99,
		g0, g1, g2, g3, g4, g5, g6, g7, g8, g9, g10, g11, g12, g13, g14, g15, g16, g17, g18, g19, g20, g21, g22, g23, g24, g25, g26, g27, g28, g29, g30, g31, g32, g33, g34, g35, g36, g37, g38, g39, g40, g41, g42, g43, g44, g45, g46, g47, g48, g49, g50, g51, g52, g53, g54, g55, g56, g57, g58, g59, g60, g61, g62, g63, g64, g65, g66, g67, g68, g69, g70, g71, g72, g73, g74, g75, g76, g77, g78, g79, g80, g81, g82, g83, g84, g85, g86, g87, g88, g89, g90, g91, g92, g93, g94, g95, g96, g97, g98, g99,
		h0, h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12, h13, h14, h15, h16, h17, h18, h19, h20, h21, h22, h23, h24, h25, h26, h27, h28, h29, h30, h31, h32, h33, h34, h35, h36, h37, h38, h39, h40, h41, h42, h43, h44, h45, h46, h47, h48, h49, h50, h51, h52, h53, h54, h55, h56, h57, h58, h59, h60, h61, h62, h63, h64, h65, h66, h67, h68, h69, h70, h71, h72, h73, h74, h75, h76, h77, h78, h79, h80, h81, h82, h83, h84, h85, h86, h87, h88, h89, h90, h91, h92, h93, h94, h95, h96, h97, h98, h99,
		i0, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20, i21, i22, i23, i24, i25, i26, i27, i28, i29, i30, i31, i32, i33, i34, i35, i36, i37, i38, i39, i40, i41, i42, i43, i44, i45, i46, i47, i48, i49, i50, i51, i52, i53, i54, i55, i56, i57, i58, i59, i60, i61, i62, i63, i64, i65, i66, i67, i68, i69, i70, i71, i72, i73, i74, i75, i76, i77, i78, i79, i80, i81, i82, i83, i84, i85, i86, i87, i88, i89, i90, i91, i92, i93, i94, i95, i96, i97, i98, i99,
		j0, j1, j2, j3, j4, j5, j6, j7, j8, j9, j10, j11, j12, j13, j14, j15, j16, j17, j18, j19, j20, j21, j22, j23, j24, j25, j26, j27, j28, j29, j30, j31, j32, j33, j34, j35, j36, j37, j38, j39, j40, j41, j42, j43, j44, j45, j46, j47, j48, j49, j50, j51, j52, j53, j54, j55, j56, j57, j58, j59, j60, j61, j62, j63, j64, j65, j66, j67, j68, j69, j70, j71, j72, j73, j74, j75, j76, j77, j78, j79, j80, j81, j82, j83, j84, j85, j86, j87, j88, j89, j90, j91, j92, j93, j94, j95, j96, j97, j98, j99,

	}
}
