package com.ªtest.net.packet.packets;

import com.ªtest.net.PacketBuffer;
import com.ªtest.net.packet.NetworkEvent;

public class TestPackage extends BasePacket {
	public TestPackage(PacketBuffer buf) {
	}

	@Override
	protected boolean action(NetworkEvent.Context ctx) {
		return false;
	}

}
