package com.ªtest.net.packet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.sunflow.util.Logger;

public class NetworkRegistry {

	private static final String NETREGISTRY = "NETREGISTRY";
	private static Map<String, NetworkInstance> instances = Collections.synchronizedMap(new HashMap<>());

	private static boolean lock = false;

	public boolean isLocked() {
		return lock;
	}

	public static void lock() {
		lock = true;
	}

	public static class ChannelBuilder {
		private String channelName;
		private Supplier<String> networkProtocolVersion;
		private Predicate<String> clientAcceptedVersions;
		private Predicate<String> serverAcceptedVersions;

		/**
		 * The name of the channel. Must be unique.
		 * 
		 * @param channelName
		 *            The name of the channel
		 * @return the channel builder
		 */
		public static ChannelBuilder named(String channelName) {
			ChannelBuilder builder = new ChannelBuilder();
			builder.channelName = channelName;
			return builder;
		}

		/**
		 * The network protocol string for this channel. This will be gathered during login and sent to
		 * the remote partner, where it will be tested with against the relevant predicate.
		 *
		 * @see #serverAcceptedVersions(Predicate)
		 * @see #clientAcceptedVersions(Predicate)
		 * @param networkProtocolVersion
		 *            A supplier of strings for network protocol version testing
		 * @return the channel builder
		 */
		public ChannelBuilder networkProtocolVersion(Supplier<String> networkProtocolVersion) {
			this.networkProtocolVersion = networkProtocolVersion;
			return this;
		}

		/**
		 * A predicate run on the client, with the {@link #networkProtocolVersion(Supplier)} string from
		 * the server, or the special value {@link NetworkRegistry#ABSENT} indicating the absence of
		 * the channel on the remote side.
		 * 
		 * @param clientAcceptedVersions
		 *            A predicate for testing
		 * @return the channel builder
		 */
		public ChannelBuilder clientAcceptedVersions(Predicate<String> clientAcceptedVersions) {
			this.clientAcceptedVersions = clientAcceptedVersions;
			return this;
		}

		/**
		 * A predicate run on the server, with the {@link #networkProtocolVersion(Supplier)} string from
		 * the server, or the special value {@link NetworkRegistry#ABSENT} indicating the absence of
		 * the channel on the remote side.
		 * 
		 * @param serverAcceptedVersions
		 *            A predicate for testing
		 * @return the channel builder
		 */
		public ChannelBuilder serverAcceptedVersions(Predicate<String> serverAcceptedVersions) {
			this.serverAcceptedVersions = serverAcceptedVersions;
			return this;
		}

		/**
		 * Create the network instance
		 * 
		 * @return the {@link NetworkInstance}
		 */
		private NetworkInstance createNetworkInstance() {
			return createInstance(channelName, networkProtocolVersion, clientAcceptedVersions, serverAcceptedVersions);
		}

		/**
		 * Build a new {@link SimpleChannel} with this builder's configuration.
		 *
		 * @return A new {@link SimpleChannel}
		 */
		public SunChannel sunChannel() {
			return new SunChannel(createNetworkInstance());
		}

		/**
		 * Creates the internal {@link NetworkInstance} that tracks the channel data.
		 * 
		 * @param name
		 *            registry name
		 * @param networkProtocolVersion
		 *            The protocol version string
		 * @param clientAcceptedVersions
		 *            The client accepted predicate
		 * @param serverAcceptedVersions
		 *            The server accepted predicate
		 * @return The {@link NetworkInstance}
		 * @throws IllegalArgumentException
		 *             if the name already exists
		 */
		private static NetworkInstance createInstance(String name, Supplier<String> networkProtocolVersion, Predicate<String> clientAcceptedVersions, Predicate<String> serverAcceptedVersions) {
			if (lock) {
				Logger.error(NETREGISTRY, "Attempted to register channel " + name + " even though registry phase is over");
				throw new IllegalArgumentException("Registration of network channels is locked");
			}
			if (instances.containsKey(name)) {
				Logger.error(NETREGISTRY, "NetworkDirection channel " + name + " already registered.");
				throw new IllegalArgumentException("NetworkDirection Channel {" + name + "} already registered");
			}
			final NetworkInstance networkInstance = new NetworkInstance(name, networkProtocolVersion, clientAcceptedVersions, serverAcceptedVersions);
			instances.put(name, networkInstance);
			return networkInstance;
		}
	}
}
