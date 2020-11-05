package com.ªtest.net.packet;

import com.ªtest.net.packet.packets.TestPackage;

public class TestNetwork {
	private static int ID = 0;

	public static final String TUTORIALMOD_NETMARKER = "TESTGAME";
	public static final String TUTORIALMOD_NETVERSION = "1.0";
	public static final String NETVERSION = TUTORIALMOD_NETMARKER + ":" + TUTORIALMOD_NETVERSION;

	public static final String NAME = "TestGame:TestNetwork";
	public static final SunChannel TEST_CHANNEL = getTestChannel();

	private static int nextID() { return ID++; }

	public static String getVersion() { return NETVERSION; }

	private static SunChannel getTestChannel() {
		return NetworkRegistry.ChannelBuilder
				.named(NAME)
				.networkProtocolVersion(() -> NETVERSION)
				.clientAcceptedVersions(s -> true)
				.serverAcceptedVersions(s -> true)
				.sunChannel();
	}

	public static void registerMessages() {
		TEST_CHANNEL.registerMessage(nextID(),
				TestPackage.class,
				TestPackage::encode,
				TestPackage::new,
				TestPackage::onMessage);

		TEST_CHANNEL.messageBuilder(TestPackage.class, nextID())
				.encoder(TestPackage::encode)
				.decoder(TestPackage::new)
				.consumer(TestPackage::onMessage)
				.add();
	}

	// Sending to Server
//	public static <MSG> void sendToServer(MSG msg) {
//		TUTORIALMOD_CHANNEL.sendToServer(msg);
//	}

//	public static <MSG> void sendTo(MSG msg, NetworkManager manager, NetworkDirection direction) {
//		TUTORIALMOD_CHANNEL.sendTo(msg, manager, direction);
//	}
}
