package com.�test.net.packet.packets;

import com.�test.net.PacketBuffer;
import com.�test.net.packet.NetworkEvent;

public class TestPackage extends BasePacket {
	public TestPackage(PacketBuffer buf) {
	}

	@Override
	protected boolean action(NetworkEvent.Context ctx) {
		return false;
	}

}
