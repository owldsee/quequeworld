package net.mcreator.quequeworld.network;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.client.ClientVariables;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

public record TimerSyncPacket(boolean countdownActive, int countdownTicks, boolean dayTimerActive, boolean dayTimerPaused, int dayTimerTicks) implements CustomPacketPayload {
	public static final Type<TimerSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "timer_sync"));

	public static final StreamCodec<FriendlyByteBuf, TimerSyncPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, TimerSyncPacket::countdownActive,
			ByteBufCodecs.INT, TimerSyncPacket::countdownTicks,
			ByteBufCodecs.BOOL, TimerSyncPacket::dayTimerActive,
			ByteBufCodecs.BOOL, TimerSyncPacket::dayTimerPaused,
			ByteBufCodecs.INT, TimerSyncPacket::dayTimerTicks,
			TimerSyncPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final TimerSyncPacket message, final IPayloadContext context) {
		context.enqueueWork(() -> {
			if (FMLEnvironment.dist == Dist.CLIENT) {
				ClientVariables.countdownActive = message.countdownActive();
				ClientVariables.countdownTicks = message.countdownTicks();
				ClientVariables.dayTimerActive = message.dayTimerActive();
				ClientVariables.dayTimerPaused = message.dayTimerPaused();
				ClientVariables.dayTimerTicks = message.dayTimerTicks();
			}
		});
	}
}
