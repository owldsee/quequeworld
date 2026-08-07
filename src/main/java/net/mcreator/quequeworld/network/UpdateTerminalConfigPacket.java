package net.mcreator.quequeworld.network;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.block.entity.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateTerminalConfigPacket(BlockPos pos, String emitSignal, String expectedWord) implements CustomPacketPayload {
    public static final Type<UpdateTerminalConfigPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "update_terminal_config"));

    public static final StreamCodec<FriendlyByteBuf, UpdateTerminalConfigPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeBlockPos(msg.pos());
                buf.writeUtf(msg.emitSignal());
                buf.writeUtf(msg.expectedWord());
            },
            buf -> new UpdateTerminalConfigPacket(
                buf.readBlockPos(),
                buf.readUtf(),
                buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final UpdateTerminalConfigPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.hasPermissions(2) || player.getTags().contains("dios") || player.isCreative() || player.getMainHandItem().getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof net.mcreator.quequeworld.block.TerminalBlock) {
                    if (player.level().getBlockEntity(message.pos()) instanceof TerminalBlockEntity terminal) {
                        terminal.setConfig(message.expectedWord(), message.emitSignal());
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a[Terminal] Configuración guardada correctamente."), false);
                    }
                }
            }
        });
    }
}
