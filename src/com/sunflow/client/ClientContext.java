package com.sunflow.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.BiConsumer;

import com.sunflow.common.CommonContext;
import com.sunflow.util.Logger;
import com.sunflow.util.Side;

public class ClientContext extends CommonContext {

	private Socket socket;

	public ClientContext() { this(null); }

	public ClientContext(ThreadGroup clientThreadGroup) { super(Side.client, clientThreadGroup); }

	@Override
	public void async_accept(BiConsumer<IOException, Socket> socketConsumer) {
		throw new UnsupportedOperationException("async_accept can only be called from an ServerContext");
	}

	@Override
	public void async_connect(InetSocketAddress serverEndpoint,
			BiConsumer<IOException, Socket> consumer) {
		async_post("clientcontext_async_connect", () -> {
			IOException error = null;
			try {
				// Create Socket
				socket = new Socket();
				// And try to connect to Server
				socket.connect(serverEndpoint, 5000);
			} catch (IOException e) {
				error = e;
			} finally {
				consumer.accept(error, socket);
			}
		});
	}

	@Override
	public void close() throws IOException {
		Logger.debug("ClientContext", "close()");
		super.close();
//		socket.close();
	}
}
