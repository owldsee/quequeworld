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

public record SoulShieldSyncPacket(boolean hasShield, boolean isThreatened, boolean isGhost, int banderines, int deudas, double dangerLevel, int minBanderines) implements CustomPacketPayload {
	public static final Type<SoulShieldSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "soul_shield_sync"));

	public static final StreamCodec<FriendlyByteBuf, SoulShieldSyncPacket> STREAM_CODEC = StreamCodec.of(
			(buf, msg) -> {
				buf.writeBoolean(msg.hasShield());
				buf.writeBoolean(msg.isThreatened());
				buf.writeBoolean(msg.isGhost());
				buf.writeInt(msg.banderines());
				buf.writeInt(msg.deudas());
				buf.writeDouble(msg.dangerLevel());
				buf.writeInt(msg.minBanderines());
			},
			buf -> new SoulShieldSyncPacket(
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readInt(),
				buf.readInt(),
				buf.readDouble(),
				buf.readInt()
			)
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final SoulShieldSyncPacket message, final IPayloadContext context) {
		context.enqueueWork(() -> {
			if (FMLEnvironment.dist == Dist.CLIENT) {
				ClientVariables.hasShield = message.hasShield();
				ClientVariables.isThreatened = message.isThreatened();
				ClientVariables.isGhost = message.isGhost();
				ClientVariables.banderines = message.banderines();
				ClientVariables.deuda = message.deudas();
				ClientVariables.dangerLevel = message.dangerLevel();
				ClientVariables.minBanderines = message.minBanderines();


				// Si es el primer sync, inicializar las variables mostradas sin lerp
				if (ClientVariables.firstSync) {
					ClientVariables.displayedBanderines = message.banderines();
					ClientVariables.displayedDeuda = message.deudas();
					ClientVariables.displayedDangerLevel = message.dangerLevel();
					ClientVariables.firstSync = false;
				}
			}
		});
	}
}
