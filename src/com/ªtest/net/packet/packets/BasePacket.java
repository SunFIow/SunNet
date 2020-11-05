package com.ªtest.net.packet.packets;

import java.util.function.Supplier;

import com.ªtest.net.PacketBuffer;
import com.ªtest.net.packet.NetworkEvent;

public abstract class BasePacket {

	protected BasePacket(PacketBuffer buf) {}

	protected BasePacket() {}

	public void encode(PacketBuffer buf) {}

	public final void onMessage(Supplier<NetworkEvent.Context> ctx) { ctx.get().setPacketHandled(action(ctx.get())); }

	protected abstract boolean action(NetworkEvent.Context ctx);

}
