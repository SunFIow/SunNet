package com._impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.sunflow.common.Connection;
import com.sunflow.common.Logger;
import com.sunflow.common.Message;
import com.sunflow.server.Server;

public class SimpleServer {

	class CustomServer extends Server.Interface<CustomMsgTypes> {

		public CustomServer(int port) throws IOException { super(port); }

		public CustomServer(String host, int port) throws IOException { super(host, port); }

		public CustomServer(InetAddress host, int port) throws IOException { super(host, port); }

		public CustomServer(InetSocketAddress endpoint) throws IOException { super(endpoint); }

		@Override
		protected boolean onClientConnect(Connection<CustomMsgTypes> client) {
			// Accept every connection
			boolean accept = true;

			Message<CustomMsgTypes> msg = new Message<>();
			if (accept) msg.header.id = CustomMsgTypes.ServerAccept;
			else msg.header.id = CustomMsgTypes.ServerDeny;

			msg.push(client.getID());

			client.send(msg);

			return accept;
		}

		@Override
		protected void onClientDisconnect(Connection<CustomMsgTypes> client) {
			Logger.info("Server", "Removing client (" + client.getID() + ")");
		}

		@Override
		protected void onMessage(Connection<CustomMsgTypes> client, Message<CustomMsgTypes> msg) {
			switch (msg.header.id) {
				case ServerPing:
					Logger.info("Server", "(" + client.getID() + ") Server Ping");

					// Simply bounce back to client
					client.send(msg);
					break;

				case MessageAll:
					Logger.info("Server", "(" + client.getID() + ") Message All");
					msg = new Message<>(CustomMsgTypes.ServerMessage);
					msg.push(client.getID());
					messageAllClients(msg, client);
					break;
				default:
					break;
			}
		}

	}

	public static void main(String[] args) { new SimpleServer(); }

	public SimpleServer() {
		CustomServer server = null;
		try {
			server = new CustomServer(PrivateInfo.PORT);

//			server = new CustomServer(PrivateInfo.localhost, PrivateInfo.PORT);
//			server = new CustomServer(PrivateInfo.localhostIP, PrivateInfo.PORT);

//			server = new CustomServer(PrivateInfo.localPcIPv4, PrivateInfo.PORT);
//			server = new CustomServer(PrivateInfo.localPcIPv6, PrivateInfo.PORT);

//			server = new CustomServer(PrivateInfo.yourPcIPv6, PrivateInfo.PORT);

//			server = new CustomServer(PrivateInfo.routerIP4, PrivateInfo.PORT);
//			server = new CustomServer(PrivateInfo.routerIP6, PrivateInfo.PORT);

//			server = new CustomServer(PrivateInfo.subdomain, PrivateInfo.PORT);
//			server = new CustomServer(PrivateInfo.domain, PrivateInfo.PORT);

			server.start();

			while (true) {
				server.update();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
