package com.$impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sunflow.client.Client;
import com.sunflow.util.Logger;
import com.ªtest.net.MessageBuffer;
import com.ªtest.net.MixedMessage;

public class SimpleClient {

	class CustomClient extends Client.Interface<CustomMsgTypes> {
/*        */ private static final int longsttowrite = 16000;
//	   											     128000
//		private static final int longsttowrite = 2147483647;

		@Override
//		public MessageBuffer<CustomMsgTypes> blankMessage() { return MessageBuffer.createEnum(CustomMsgTypes.class); }
		public MessageBuffer<CustomMsgTypes> blankMessage() { return new MixedMessage<>(); }

		public CustomClient() { super(); }

		public void PingServer() {
//			Message<CustomMsgTypes> msg = new Message<>(CustomMsgTypes.ServerPing);
			MixedMessage<CustomMsgTypes> msg = new MixedMessage<>(CustomMsgTypes.ServerPing);
//			MessageBuffer<CustomMsgTypes> msg = MessageBuffer.create(CustomMsgTypes.ServerPing);

			long timeNow = System.currentTimeMillis();
//			msg.put(timeNow);
			msg.writeVarLong(timeNow);

			Logger.debug("Client", "Send Ping Message to Server");
			send(msg);
			Logger.debug("Client", "Send Message");
		}

		public void PingServerMULTI() {
//			Message<CustomMsgTypes> msg = new Message<>(CustomMsgTypes.ServerPing);
			MixedMessage<CustomMsgTypes> msg = new MixedMessage<>(CustomMsgTypes.ServerPing);
//			MessageBuffer<CustomMsgTypes> msg = MessageBuffer.create(CustomMsgTypes.ServerPing);

			long timeNow = System.currentTimeMillis();
//			msg.put(timeNow);
			msg.writeVarLong(timeNow);

			Logger.debug("Client", "Send Ping Message to Server");
			for (int i = 0; i < 10; i++) send(msg);
			Logger.debug("Client", "Send Message");
		}

		public void PingServerFULL() {
//			Message<CustomMsgTypes> msg = new Message<>(CustomMsgTypes.ServerPingFull);
			MixedMessage<CustomMsgTypes> msg = new MixedMessage<>(CustomMsgTypes.ServerPingFull);
//			MessageBuffer<CustomMsgTypes> msg = MessageBuffer.create(CustomMsgTypes.ServerPingFull);
			for (int i = 0; i < longsttowrite - 1; i++) {
				long timeNow = System.currentTimeMillis();
//				msg.put(timeNow);
				msg.writeLong(timeNow);
			}

			long timeNow = System.currentTimeMillis();
//			msg.put(timeNow);
			msg.writeLong(timeNow);
			System.out.println(timeNow);
			Logger.debug("Client", "Send Full-Ping Message to Server");
			send(msg);
			Logger.debug("Client", "Send Message");
		}

		public void PingServerMULTIFULL() {
//			Message<CustomMsgTypes> msg = new Message<>(CustomMsgTypes.ServerPingFull);
			MixedMessage<CustomMsgTypes> msg = new MixedMessage<>(CustomMsgTypes.ServerPingFull);
//			MessageBuffer<CustomMsgTypes> msg = MessageBuffer.create(CustomMsgTypes.ServerPingFull);
			for (int i = 0; i < longsttowrite - 1; i++) {
				long timeNow = System.currentTimeMillis();
//				msg.put(timeNow);
				msg.writeLong(timeNow);
			}

			long timeNow = System.currentTimeMillis();
//			msg.put(timeNow);
			msg.writeLong(timeNow);
			System.out.println(timeNow);
			Logger.debug("Client", "Send Multi-Full-Ping Message to Server");
			for (int i = 0; i < 10; i++) send(msg);
			Logger.debug("Client", "Send Message");
		}

		public void MessageAll() {
//			Message<CustomMsgTypes> msg = new Message<>(CustomMsgTypes.MessageAll);
			MixedMessage<CustomMsgTypes> msg = new MixedMessage<>(CustomMsgTypes.MessageAll);
//			MessageBuffer<CustomMsgTypes> msg = MessageBuffer.create(CustomMsgTypes.MessageAll);
			send(msg);
		}

		int i = 0;

		@Override
//		protected void onMessage(Message<CustomMsgTypes> msg) {
//		protected void onMessage(MixedMessage<CustomMsgTypes> msg) {
		protected void onMessage(MessageBuffer<CustomMsgTypes> msg) {
//			MessageBuffer<CustomMsgTypes> msg = MessageBuffer.createEnum(CustomMsgTypes.class, buffer);
			switch (msg.getID()) {
				case ServerAccept:
					// Server has responded to a ping request
//					int clientID0 = msg.pop();
					int clientID0 = msg.readVarInt();
					Logger.info("Client", "Server Accepted Connection, your UID (" + clientID0 + ")");
					break;
				case ServerDeny:
					// Server has responded to a ping request
//					int clientID1 = msg.pop();
					int clientID1 = msg.readVarInt();
					Logger.info("Client", "Server Denied Connection, your UID (" + clientID1 + ")");
					break;
				case ServerPing:
					// Server has responded to a ping request
					long now = System.currentTimeMillis();
//					long start = msg.pop();
					long start = msg.readVarLong();
					Logger.info("Client", "Ping: " + (now - start) / 1000f);
					break;
				case ServerPingFull:
					// Server has responded to a ping request
					long now2 = System.currentTimeMillis();
//					long start = msg.pop();
					for (int i = 0; i < longsttowrite - 1; i++) msg.readLong();
					long start2 = msg.readLong();
					Logger.info("Client", "Ping2: " + (now2 - start2) / 1000f + " - " + start2);
					break;
				case ServerMessage:
					// Server has responded to a ping request
//					int clientID2 = msg.pop();
					int clientID2 = msg.readVarInt();
					Logger.info("Client", "Hello from (" + clientID2 + ")");
					break;
				default:
					break;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new SimpleClient();
	}

	public SimpleClient() throws Exception {
		CustomClient c = new CustomClient();

		c.connect(PrivateInfo.localhost, PrivateInfo.PORT);
//		c.connect(PrivateInfo.localhostIP, PrivateInfo.PORT);

//		c.connect(PrivateInfo.localPcIPv4, PrivateInfo.PORT);
//		c.connect(PrivateInfo.localPcIPv6, PrivateInfo.PORT);

//		c.connect(PrivateInfo.yourPcIPv6, PrivateInfo.PORT);

//		c.connect(PrivateInfo.routerIPv4, PrivateInfo.PORT);
//		c.connect(PrivateInfo.routerIPv6, PrivateInfo.PORT);

//		c.connect(PrivateInfo.subdomain, PrivateInfo.PORT);
//		c.connect(PrivateInfo.domain, PrivateInfo.PORT);

		// TODO Make interaction via keypresses not console input
//		boolean key[] = { false, false, false }; 
//		boolean old_key[] = { false, false, false };

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		boolean bQuit = false;
		while (!bQuit) {
			if (br.ready()) {
				String line = br.readLine();

				if (line.equals("ping")) {
					c.PingServer();
				} else if (line.equals("all")) {
					c.MessageAll();
				} else if (line.equals("exit")) {
					Logger.info("GOING TO EXIT");
					bQuit = true;
				} else if (line.equals("multiping")) {
					c.PingServerMULTI();
				} else if (line.equals("fullping")) {
					c.PingServerFULL();
				} else if (line.equals("multifullping")) {
//					for (int i = 0; i < 10; i++) 
					c.PingServerMULTIFULL();
				}
			}

			if (c.isConnected()) {
				c.update();
			} else {
				Logger.info("Client", "Server Down");
				bQuit = true;
			}
		}
		c.close();

		Thread[] threads = new Thread[Thread.activeCount()];
		int count = Thread.enumerate(threads);
		Logger.debug("Client", count + " Threads are still alive");
		for (int i = 0; i < count && i < threads.length; i++) {
			Logger.debug("Client", threads[i]);
		}

		Logger.info("Client", "Shutting down");
//		System.exit(0);
	}

}
