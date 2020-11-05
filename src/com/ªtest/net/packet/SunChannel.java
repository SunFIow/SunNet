package com.ªtest.net.packet;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sunflow.common.Connection;
import com.ªtest.net.PacketBuffer;

import io.netty.buffer.Unpooled;

public class SunChannel {

	private final NetworkInstance instance;
	private final IndexedMessageCodec indexedCodec;

	public SunChannel(NetworkInstance instance) {
		this.instance = instance;
		this.indexedCodec = new IndexedMessageCodec(this);
//        instance.addListener(this::networkEventListener);
	}

	public SunChannel(NetworkInstance instance, Connection<?> connection) {
		this(instance);
//		connection.addNetEventConsumer(indexedCodec::consume); 
//      instance.addListener(this::networkEventListener);
	}

	public String getName() { return instance.getChannelName(); }

	public <MSG> void send(Connection<?> target, MSG message) {
		target.send(toBuffer(message));
	}

	private <MSG> PacketBuffer toBuffer(MSG msg) {
		final PacketBuffer bufIn = new PacketBuffer(Unpooled.buffer());
		encodeMessage(msg, bufIn);
		return bufIn;
	}

	public <MSG> void encodeMessage(MSG message, final PacketBuffer target) {
		this.indexedCodec.build(message, target);
	}

	public <MSG> IndexedMessageCodec.MessageHandler<MSG> registerMessage(int index, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
		return this.indexedCodec.addCodecIndex(index, messageType, encoder, decoder, messageConsumer);
	}

	public <M> MessageBuilder<M> messageBuilder(final Class<M> type, int id) {
		return MessageBuilder.forType(this, type, id);
	}

	public static class MessageBuilder<MSG> {
		private SunChannel channel;
		private Class<MSG> type;
		private int id;
		private BiConsumer<MSG, PacketBuffer> encoder;
		private Function<PacketBuffer, MSG> decoder;
		private BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer;

		private static <MSG> MessageBuilder<MSG> forType(final SunChannel channel, final Class<MSG> type, int id) {
			MessageBuilder<MSG> builder = new MessageBuilder<>();
			builder.channel = channel;
			builder.id = id;
			builder.type = type;
			return builder;
		}

		public MessageBuilder<MSG> encoder(BiConsumer<MSG, PacketBuffer> encoder) {
			this.encoder = encoder;
			return this;
		}

		public MessageBuilder<MSG> decoder(Function<PacketBuffer, MSG> decoder) {
			this.decoder = decoder;
			return this;
		}

		public MessageBuilder<MSG> consumer(BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer) {
			this.consumer = consumer;
			return this;
		}

		public interface ToBooleanBiFunction<T, U> {
			boolean applyAsBool(T first, U second);
		}

		/**
		 * Function returning a boolean "packet handled" indication, for simpler channel building.
		 * 
		 * @param handler
		 *            a handler
		 * @return this
		 */
		public MessageBuilder<MSG> consumer(ToBooleanBiFunction<MSG, Supplier<NetworkEvent.Context>> handler) {
			this.consumer = (msg, ctx) -> {
				boolean handled = handler.applyAsBool(msg, ctx);
				ctx.get().setPacketHandled(handled);
			};
			return this;
		}

		public void add() {
			this.channel.registerMessage(this.id, this.type, this.encoder, this.decoder, this.consumer);
		}
	}

}
