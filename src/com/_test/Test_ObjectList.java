package com._test;

import java.io.Serializable;

public class Test_ObjectList {

	public static void main(String[] args) {
		Message_ObjectList msg = new Message_ObjectList();
		System.out.println(msg);

		msg.push(1337);
		msg.push(4711);
		msg.push(new Data());
		msg.push(new Data<Data2>("test", 10, new Data2()));
		System.out.println(msg);

		byte[] bytes = Message_ObjectList.send(msg);
		System.out.println(bytes);
//		try {
//			System.out.write(bytes);
////			System.out.write('\n');
//			System.out.flush();
//		} catch (IOException | InterruptedException e) {
//			e.printStackTrace();
//		}
		msg = Message_ObjectList.retrieve(bytes);
		System.out.println(msg);
	}

	public static class Data<T extends Serializable> implements Serializable {
		private static final long serialVersionUID = -1492785180939874075L;

		private String name;
		private int size;
		private T data;

		public Data() {}

		public Data(String name, int size, T data) { super(); this.name = name; this.size = size; this.data = data; }
	}

	public static class Data2 implements Serializable {
		private static final long serialVersionUID = 6617058738883681263L;

		private String name;
		private int size;
		private Object data;
	}
}
