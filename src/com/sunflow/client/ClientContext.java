package com.sunflow.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

import com.sunflow.common.CommonContext;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;

public class ClientContext extends CommonContext {

	private Socket socket;

	public ClientContext() { this(null); }

	public ClientContext(ThreadGroup clientThreadGroup) { super(Side.Client, clientThreadGroup); }

	@Override
	public void accept(Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer) {
		throw new UnsupportedOperationException("accept can only be called from an ServerContext");
	}

	@Override
	public void async_accept(Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer) {
		throw new UnsupportedOperationException("async_accept can only be called from an ServerContext");
	}

	@Override
	public void connect(InetSocketAddress serverEndpoint,
			Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer) {
		task("clientcontext_connect", () -> {
			// Create Socket
			socket = new Socket();
			// And try to connect to Server
			socket.connect(serverEndpoint, 5000);
			socketConsumer.accept(socket);
		}, errorConsumer);
	}

	@Override
	public void async_connect(InetSocketAddress serverEndpoint,
			Consumer<Socket> socketConsumer, Consumer<IOException> errorConsumer) {
		async_task("clientcontext_async_connect", () -> {
			// Create Socket
			socket = new Socket();
			// And try to connect to Server
			socket.connect(serverEndpoint, 5000);
			socketConsumer.accept(socket);
		}, errorConsumer);
	}

	@Override
	public void close() {
		Logger.debug("ClientContext", "close()");
		super.close();
//		socket.close();
	}
}
