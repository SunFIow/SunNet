package com.�test.net;

public interface Identifier {
	int headerSize();

	void write(PacketBuffer buffer);

	void read(PacketBuffer buffer);
}
