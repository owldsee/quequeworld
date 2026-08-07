package net.mcreator.quequeworld.network;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent from server → client to trigger a totem-style item activation animation.
 * Carries the registry ID of the item to display.
 */
public record ThreatAnimationPacket(String itemId) implements CustomPacketPayload {

    public static final Type<ThreatAnimationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "threat_animation"));

    public static final StreamCodec<FriendlyByteBuf, ThreatAnimationPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ThreatAnimationPacket::itemId,
            ThreatAnimationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ThreatAnimationPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientPacketHandler.handleThreatAnimation(message.itemId());
            }
        });
    }
}
