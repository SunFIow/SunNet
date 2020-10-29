package com.ªtest.net;

public interface Data {
	int idSize();

	void write(PacketBuffer buffer);

	void read(PacketBuffer buffer);
}
