package com.�test.net;

public interface Data {
	int idSize();

	void write(PacketBuffer buffer);

	void read(PacketBuffer buffer);
}
