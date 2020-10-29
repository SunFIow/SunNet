package com.sunflow.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

import com.sunflow.common.CommonContext;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;

public class ServerContext extends CommonContext {

	private ServerSocket serverSocket;

	public ServerContext(InetSocketAddress endpoint) throws IOException {
		this(null, endpoint);
	}

	public ServerContext(ThreadGroup serverThreadGroup, InetSocketAddress endpoint) throws IOException {
		super(Side.Server, serverThreadGroup);
		serverSocket = new ServerSocket();
//		serverSocket.setSendBufferSize(200000);
//		serverSocket.setReceiveBufferSize(200000);
		serverSocket.bind(endpoint);
		Logger.info("SERVER", "Bound to " + serverSocket.getLocalSocketAddress());
	}

	@Override
	public void accept(Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer) {
		task("servercontext_accept", () -> {
			Socket socket = serverSocket.accept();
			socketConsumer.accept(socket);
		}, errorConsumer);
	}

	@Override
	public void async_accept(Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer) {
		async_task("servercontext_async_accept", () -> {
			Socket socket = serverSocket.accept();
//			socket.setSendBufferSize(200000);
//			socket.setReceiveBufferSize(200000);
//			System.out.println(socket.getReceiveBufferSize());
//			System.out.println(socket.getSendBufferSize());
			socketConsumer.accept(socket);
		}, errorConsumer);
	}

	@Override
	public void connect(InetSocketAddress serverEndpoint,
			Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer) {
		throw new UnsupportedOperationException("connect can only be called from an ClientContext");
	}

	@Override
	public void async_connect(InetSocketAddress serverEndpoint,
			Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer) {
		throw new UnsupportedOperationException("async_connect can only be called from an ClientContext");
	}

	@Override
	public void close() {
		Logger.debug("ServerContext", "close()");
		super.close();
//		serverSocket.close();
	}
}
