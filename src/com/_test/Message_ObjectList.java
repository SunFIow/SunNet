package com._test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

public class Message_ObjectList implements Serializable {
	private static final long serialVersionUID = 2458137572163721569L;

	// private ArrayList<Object> data = new ArrayList<>();
	private LinkedList<Object> data = new LinkedList<>();

	public Message_ObjectList() {}

	public void push(Object data) {
		this.data.push(data);
	}

	public Object pop() {
		return this.data.pop();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString() + "[");
		if (!data.isEmpty()) builder.append(data.peek().toString());
		for (int i = 1; i < data.size(); i++) builder.append(", " + data.get(i).toString());
		builder.append("]");
		return builder.toString();
	}

	public static byte[] send(Message_ObjectList msg) {
		// Serialize the data
		byte[] bytes = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			// Write the data into the stream
			oos.writeObject(msg);
			oos.flush();
			// Get the bytes of the data + sizeof the data
			bytes = bos.toByteArray();
			oos.close();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	public static Message_ObjectList retrieve(byte[] bytes) {
		Message_ObjectList msg = null;
		// Deserialize the data
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bis);
			msg = (Message_ObjectList) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return msg;
	}
}
