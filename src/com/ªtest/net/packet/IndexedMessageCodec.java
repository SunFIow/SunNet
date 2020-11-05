package com.ªtest.net.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sunflow.util.Logger;
import com.ªtest.net.PacketBuffer;

public class IndexedMessageCodec {
	private static final String SUNNET = "SUNNET";

	private Map<Integer, MessageHandler<?>> indicies = new HashMap<>();
	private Map<Class<?>, MessageHandler<?>> types = new HashMap<>();

	private SunChannel channel;

	public IndexedMessageCodec() { this(null); }

	public IndexedMessageCodec(SunChannel messageChannel) {
		channel = messageChannel;
	}

	private String getChannelName() {
		return Optional.ofNullable(channel).map(SunChannel::getName).orElse("MISSING CHANNEL");
	}

	private void logErrorOnChannel(String errorDesc) {
		Logger.error(SUNNET, errorDesc + " on channel " + getChannelName());
	}

	private static <M> void tryDecode(NetworkEvent event, MessageHandler<M> codec) {
//	private static <M> void tryDecode(PacketBuffer payload, Supplier<NetworkEvent.Context> context, MessageHandler<M> codec) {
		codec.decoder
				.map(d -> d.apply(event.getPayload()))
				.ifPresent(m -> codec.messageConsumer.accept(m, event.getSource()));
	}

	private static <M> void tryEncode(PacketBuffer target, M message, MessageHandler<M> codec) {
		codec.encoder.ifPresent(encoder -> {
			target.writeInt(codec.index);
			encoder.accept(message, target);
		});
	}

	public <MSG> void build(MSG message, PacketBuffer target) {
		@SuppressWarnings("unchecked")
		MessageHandler<MSG> messageHandler = (MessageHandler<MSG>) types.get(message.getClass());
		if (messageHandler == null) {
			logErrorOnChannel("Received invalid message " + message.getClass().getName());
			throw new IllegalArgumentException("Invalid message " + message.getClass().getName());
		}
		tryEncode(target, message, messageHandler);
	}

	void consume(NetworkEvent event) {
		if (event.getPayload() == null) {
			logErrorOnChannel("Received empty payload");
			return;
		}
		int discriminator = event.getPayload().readInt();
		final MessageHandler<?> messageHandler = indicies.get(discriminator);
		if (messageHandler == null) {
			logErrorOnChannel("Received invalid discriminator byte " + discriminator);
			return;
		}
		tryDecode(event, messageHandler);
	}

	<MSG> MessageHandler<MSG> addCodecIndex(int index, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
		return new MessageHandler<>(index, messageType, encoder, decoder, messageConsumer);
	}

	class MessageHandler<MSG> {
		private final int index;
		private final Class<MSG> messageType;
		private final Optional<BiConsumer<MSG, PacketBuffer>> encoder;
		private final Optional<Function<PacketBuffer, MSG>> decoder;
		private final BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer;

		public MessageHandler(
				int index,
				Class<MSG> messageType,
				BiConsumer<MSG, PacketBuffer> encoder,
				Function<PacketBuffer, MSG> decoder,
				BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
			this.index = index;
			this.messageType = messageType;
			this.encoder = Optional.ofNullable(encoder);
			this.decoder = Optional.ofNullable(decoder);
			this.messageConsumer = messageConsumer;
			indicies.put(index, this);
			types.put(messageType, this);
		}
	}
}
