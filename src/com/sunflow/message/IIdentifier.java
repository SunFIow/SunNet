package com.sunflow.message;

public interface IIdentifier {
	int size();

	void write(PacketBuffer buffer);

	void read(PacketBuffer buffer);
}
