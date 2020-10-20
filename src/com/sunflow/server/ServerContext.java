package com.sunflow.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.BiConsumer;

import com.sunflow.common.CommonContext;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;

public class ServerContext extends CommonContext {

	private ServerSocket serverSocket;

	public ServerContext(InetSocketAddress endpoint) throws IOException {
		this(null, endpoint);
	}

	public ServerContext(ThreadGroup serverThreadGroup, InetSocketAddress endpoint) throws IOException {
		super(Side.server, serverThreadGroup);
		serverSocket = new ServerSocket();
		serverSocket.bind(endpoint);
		Logger.info("SERVER", "Bound to " + serverSocket.getLocalSocketAddress());
	}

	@Override
	public void async_accept(BiConsumer<IOException, Socket> socketConsumer) {
		async_post("servercontext_async_accept", () -> {
			Socket socket = null;
			IOException error = null;
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				error = e;
			} finally {
				socketConsumer.accept(error, socket);
			}
		});
	}

	@Override
	public void async_connect(InetSocketAddress serverEndpoint,
			BiConsumer<IOException, Socket> consumer) {
		throw new UnsupportedOperationException("async_connect can only be called from an ClientContext");
	}

	private final HashMap<Socket, ObjectInputStream> oisMap = new HashMap<>(); // TODO Remove cached streams of disconnected Sockets
	private final HashMap<Socket, ObjectOutputStream> oosMap = new HashMap<>(); // TODO Remove cached streams of disconnected Sockets

	@Override
	protected ObjectInputStream getObjectInputStream(Socket socket) throws IOException {
		if (!oisMap.containsKey(socket)) oisMap.put(socket, new ObjectInputStream(new BufferedInputStream(socket.getInputStream())));
		return oisMap.get(socket);
	}

	@Override
	protected ObjectOutputStream getObjectOutputStream(Socket socket) throws IOException {
		if (!oosMap.containsKey(socket)) oosMap.put(socket, new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream())));
		return oosMap.get(socket);
	}

	@Override
	public void close() {
		Logger.debug("ServerContext", "close()");
		super.close();
//		serverSocket.close();
	}
}
