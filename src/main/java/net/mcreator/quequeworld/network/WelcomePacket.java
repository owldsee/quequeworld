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

/**
 * Packet enviado del servidor al cliente para mostrar un banner o texto de bienvenida
 * durante 3 segundos en pantalla.
 * welcomeType: "img" o "texto"
 * content: id de la imagen ("dia_1", "dia_2", "dia_3") o texto a escribir
 */
public record WelcomePacket(String welcomeType, String content) implements CustomPacketPayload {

    public static final Type<WelcomePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "welcome_packet"));

    public static final StreamCodec<FriendlyByteBuf, WelcomePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WelcomePacket::welcomeType,
            ByteBufCodecs.STRING_UTF8, WelcomePacket::content,
            WelcomePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final WelcomePacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientVariables.welcomeType = message.welcomeType();
                ClientVariables.welcomeContent = message.content();
                ClientVariables.welcomeStartTime = System.currentTimeMillis();
            }
        });
    }
}
