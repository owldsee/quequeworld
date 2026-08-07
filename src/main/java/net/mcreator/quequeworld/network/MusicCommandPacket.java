package net.mcreator.quequeworld.network;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.client.sound.ClientMusicManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MusicCommandPacket(String action, String songName) implements CustomPacketPayload {

	public static final Type<MusicCommandPacket> TYPE =
		new Type<>(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "music_command"));

	public static final StreamCodec<FriendlyByteBuf, MusicCommandPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, MusicCommandPacket::action,
		ByteBufCodecs.STRING_UTF8, MusicCommandPacket::songName,
		MusicCommandPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final MusicCommandPacket message, final IPayloadContext context) {
		context.enqueueWork(() -> {
			if (FMLEnvironment.dist == Dist.CLIENT) {
				if ("stop".equalsIgnoreCase(message.action())) {
					ClientMusicManager.stopMusic();
				} else if ("play".equalsIgnoreCase(message.action())) {
					ClientMusicManager.playMusic(message.songName());
				}
			}
		});
	}
}
