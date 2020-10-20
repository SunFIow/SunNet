package com._impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sunflow.client.Client;
import com.sunflow.common.Message;
import com.sunflow.util.Logger;

public class SimpleClient {

	// TODO Make Client ShutDown
	// when Server is not aAvaiable anymore
	// and exit got typed

	class CustomClient extends Client.Interface<CustomMsgTypes> {

		public void PingServer() {
			Message<CustomMsgTypes> msg = new Message<>(CustomMsgTypes.ServerPing);

			long timeNow = System.currentTimeMillis();

			msg.push(timeNow);
//			System.out.println("Send Ping Message to Server");
			Logger.debug("Client", "Send Ping Message to Server");
			send(msg);
//			System.out.println("Send Message");
			Logger.debug("Client", "Send Message");
		}

		public void MessageAll() {
			Message<CustomMsgTypes> msg = new Message<>(CustomMsgTypes.MessageAll);
			send(msg);
		}

		@Override
		protected void onMessage(Message<CustomMsgTypes> msg) {
			switch (msg.header.id) {
				case ServerAccept:
					// Server has responded to a ping request
					Logger.info("Client", "Server Accepted Connection, your UID (" + msg.pop() + ")");
					break;
				case ServerDeny:
					// Server has responded to a ping request
					Logger.info("Client", "Server Denied Connection, your UID (" + msg.pop() + ")");
					break;
				case ServerPing:
					// Server has responded to a ping request
					long now = System.currentTimeMillis();
					long start = msg.pop();
					Logger.info("Client", "Ping: " + (now - start) / 1000f);
					break;
				case ServerMessage:
					// Server has responded to a ping request
					int clientID = msg.pop();
					Logger.info("Client", "Hello from (" + clientID + ")");
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

		Thread.sleep(1000);

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
				} else if (line.equals("exit2")) {
					Logger.info("CLIENT IS GOING TO CLOSE");
					c.close();
				}
			}

			if (c.isConnected()) {
				c.update();
			} else {
				Logger.info(System.nanoTime() + ": Server Down");
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

		Logger.debug("Client", "Shutting down");
//		System.exit(0);
	}

}
