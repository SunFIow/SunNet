package com.$impl;

import com.sunflow.common.Connection;
import com.sunflow.message.MessageBuffer;
import com.sunflow.message.MixedMessage;
import com.sunflow.server.Server;
import com.sunflow.util.Logger;

public class SimpleServer {

	class CustomServer extends Server<CustomMsgTypes> {

		public CustomServer() { super(); }
//		public CustomServer() { super(MixedMessage::new); }

		@Override
		protected boolean onClientConnect(Connection<CustomMsgTypes> client, int clientID) {
			boolean accept = true; // Accept every connection
			MixedMessage<CustomMsgTypes> msg = new MixedMessage<>(accept ? CustomMsgTypes.ServerAccept : CustomMsgTypes.ServerDeny);
//			MessageBuffer<CustomMsgTypes> msg = MessageBuffer.create(accept ? CustomMsgTypes.ServerAccept : CustomMsgTypes.ServerDeny);
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
		protected void onMessage(Connection<CustomMsgTypes> client, MessageBuffer<CustomMsgTypes> msg) {
			try {
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

						// Send the sender's id to all other clients
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
		server = new CustomServer();

		server.create(PrivateInfo.PORT);

//		server.create(PrivateInfo.localhost, PrivateInfo.PORT);
//		server.create(PrivateInfo.localhostIP, PrivateInfo.PORT);
//
//		server.create(PrivateInfo.localPcIPv4, PrivateInfo.PORT);
//		server.create(PrivateInfo.localPcIPv6, PrivateInfo.PORT);
//
//		server.create(PrivateInfo.yourPcIPv6, PrivateInfo.PORT);
//
//		server.create(PrivateInfo.routerIP4, PrivateInfo.PORT);
//		server.create(PrivateInfo.routerIP6, PrivateInfo.PORT);
//
//		server.create(PrivateInfo.subdomain, PrivateInfo.PORT);
//		server.create(PrivateInfo.domain, PrivateInfo.PORT);

		boolean started = server.start();

		if (started) while (server.isRunning()) {
			server.update();
		}

		Logger.help("SimpleServer", "Stopped");

		server.close();

		Logger.help("SimpleServer", "Closed");
	}

}
