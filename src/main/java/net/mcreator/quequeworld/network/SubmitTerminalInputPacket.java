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

public record SubmitTerminalInputPacket(BlockPos pos, String inputWord) implements CustomPacketPayload {
    public static final Type<SubmitTerminalInputPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "submit_terminal_input"));

    public static final StreamCodec<FriendlyByteBuf, SubmitTerminalInputPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeBlockPos(msg.pos());
                buf.writeUtf(msg.inputWord());
            },
            buf -> new SubmitTerminalInputPacket(
                buf.readBlockPos(),
                buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final SubmitTerminalInputPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(message.pos()) instanceof TerminalBlockEntity terminal) {
                    boolean success = terminal.submitWord(player, message.inputWord());
                    if (success) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a[Terminal] ¡Palabra correcta! Señal emitida."), true);
                    } else {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[Terminal] Palabra incorrecta."), true);
                    }
                }
            }
        });
    }
}
