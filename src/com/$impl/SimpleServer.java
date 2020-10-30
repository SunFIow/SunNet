package com.$impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.sunflow.common.Connection;
import com.sunflow.server.Server;
import com.sunflow.util.Logger;
import com.ªtest.net.MessageBuffer;
import com.ªtest.net.MixedMessage;

public class SimpleServer {

	class CustomServer extends Server.Interface<CustomMsgTypes> {

		@Override
//		public MessageBuffer<CustomMsgTypes> blankMessage() { return MessageBuffer.createEnum(CustomMsgTypes.class); }
		public MessageBuffer<CustomMsgTypes> blankMessage() { return new MixedMessage<>(); }

		public CustomServer() { super(); }

		public CustomServer(int port) { super(port); }

		public CustomServer(String host, int port) { super(host, port); }

		public CustomServer(InetAddress host, int port) { super(host, port); }

		public CustomServer(InetSocketAddress endpoint) { super(endpoint); }

		@Override
		protected boolean onClientConnect(Connection<CustomMsgTypes> client, int clientID) {
			// Accept every connection
			boolean accept = true;
//			Message<CustomMsgTypes> msg = new Message<>(accept ? CustomMsgTypes.ServerAccept : CustomMsgTypes.ServerDeny);
			MixedMessage<CustomMsgTypes> msg = new MixedMessage<>(accept ? CustomMsgTypes.ServerAccept : CustomMsgTypes.ServerDeny);
//			MessageBuffer<CustomMsgTypes> msg = MessageBuffer.create(accept ? CustomMsgTypes.ServerAccept : CustomMsgTypes.ServerDeny);
//			msg.put(clientID);
			msg.writeVarInt(clientID);
			client.send(msg);
			return accept;
		}

		@Override
		protected void onClientDisconnect(Connection<CustomMsgTypes> client) {
			Logger.info("Server", "Removing client (" + client.getID() + ")");
		}

		int i = 0;

		@Override
//		protected void onMessage(Connection<CustomMsgTypes> client, Message<CustomMsgTypes> msg) {
//		protected void onMessage(Connection<CustomMsgTypes> client, MixedMessage<CustomMsgTypes> msg) {
		protected void onMessage(Connection<CustomMsgTypes> client, MessageBuffer<CustomMsgTypes> msg) {
			try {
//				MessageBuffer<CustomMsgTypes> msg = MessageBuffer.createEnum(CustomMsgTypes.class, buffer);
				switch (msg.getID()) {
					case ServerPing:
						Logger.info("Server", "(" + client.getID() + ") Server Ping");

						// Simply bounce back to client
						client.send(msg);
						break;

					case ServerPingFull:
						Logger.info("Server", "(" + client.getID() + ") Server Ping2");

						// Simply bounce back to client
						client.send(msg);
						break;

					case MessageAll:
						Logger.info("Server", "(" + client.getID() + ") Message All");
//						msg = new Message<>(CustomMsgTypes.ServerMessage);
//						msg.put(client.getID());

//						msg = new MixedMessage<>(CustomMsgTypes.ServerMessage);
						msg = MessageBuffer.create(CustomMsgTypes.ServerMessage);
						msg.writeVarInt(client.getID());
						messageAllClients(msg, client);
						break;
					default:
						break;
				}
			} catch (Exception e) {
				if (i++ % 100 == 0) e.printStackTrace();

			}
		}

	}

	public static void main(String[] args) { new SimpleServer(); }

	public SimpleServer() {
		CustomServer server = null;

		server = new CustomServer(PrivateInfo.PORT);
//		server = new CustomServer();

//		server = new CustomServer(PrivateInfo.localhost, PrivateInfo.PORT);
//		server = new CustomServer(PrivateInfo.localhostIP, PrivateInfo.PORT);
//
//		server = new CustomServer(PrivateInfo.localPcIPv4, PrivateInfo.PORT);
//		server = new CustomServer(PrivateInfo.localPcIPv6, PrivateInfo.PORT);
//
//		server = new CustomServer(PrivateInfo.yourPcIPv6, PrivateInfo.PORT);
//
//		server = new CustomServer(PrivateInfo.routerIP4, PrivateInfo.PORT);
//		server = new CustomServer(PrivateInfo.routerIP6, PrivateInfo.PORT);
//
//		server = new CustomServer(PrivateInfo.subdomain, PrivateInfo.PORT);
//		server = new CustomServer(PrivateInfo.domain, PrivateInfo.PORT);

		boolean started = server.start();

		if (started) while (server.isRunning()) {
			server.update();
		}

		server.close();
	}

}
