package com.ªtest.net.packet;

import java.util.function.Supplier;

import com.sunflow.common.Connection;
import com.ªtest.net.PacketBuffer;

public class NetworkEvent {
	private final PacketBuffer payload;
	private final Supplier<Context> source;

	public NetworkEvent(final PacketBuffer payload, final Supplier<Context> source) {
		this.payload = payload;
		this.source = source;
	}

	public PacketBuffer getPayload() {
		return payload;
	}

	public Supplier<Context> getSource() {
		return source;
	}

	public static class Context {
		private final Connection<?> connection;
		private boolean packetHandled;

		public Context(Connection<?> connection) {
			this.connection = connection;
		}

		public Connection<?> getConnection() {
			return connection;
		}

		public void setPacketHandled(boolean packetHandled) {
			this.packetHandled = packetHandled;
		}

		public boolean getPacketHandled() {
			return packetHandled;
		}
	}
}
