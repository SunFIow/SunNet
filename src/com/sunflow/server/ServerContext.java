package com.sunflow.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

	@Override
	public void close() throws IOException {
		Logger.debug("ServerContext", "close()");
		super.close();
//		serverSocket.close();
	}
}
