package net.mcreator.quequeworld.network;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.block.entity.SignalReceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateSignalReceiverPacket(BlockPos pos, String channel, String signal) implements CustomPacketPayload {
    public static final Type<UpdateSignalReceiverPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "update_signal_receiver"));

    public static final StreamCodec<FriendlyByteBuf, UpdateSignalReceiverPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeBlockPos(msg.pos());
                buf.writeUtf(msg.channel());
                buf.writeUtf(msg.signal());
            },
            buf -> new UpdateSignalReceiverPacket(
                buf.readBlockPos(),
                buf.readUtf(),
                buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final UpdateSignalReceiverPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.hasPermissions(2) || player.getTags().contains("dios") || player.isCreative()) {
                    if (player.level().getBlockEntity(message.pos()) instanceof SignalReceiverBlockEntity receiver) {
                        receiver.setChannelAndSignal(message.channel(), message.signal());
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a[Receptor] Configuración actualizada: §e" + receiver.getFullChannelSignal()), false);
                    }
                }
            }
        });
    }
}
