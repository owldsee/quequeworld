package net.mcreator.quequeworld.minigame;

import net.mcreator.quequeworld.event.ModEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CheckpointManager {

    public static class PlayerCheckpoint {
        public final double x, y, z;
        public final float yaw, pitch;
        public final String dimension;

        public PlayerCheckpoint(double x, double y, double z, float yaw, float pitch, String dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.dimension = dimension;
        }
    }

    private static final Map<UUID, PlayerCheckpoint> CHECKPOINTS = new HashMap<>();

    public static void setCheckpoint(ServerPlayer player) {
        PlayerCheckpoint cp = new PlayerCheckpoint(
            player.getX(),
            player.getY(),
            player.getZ(),
            player.getYRot(),
            player.getXRot(),
            player.level().dimension().location().toString()
        );
        CHECKPOINTS.put(player.getUUID(), cp);
        player.displayClientMessage(Component.literal("🚩 §a[CHECKPOINT] ¡Punto de control guardado!"), true);
    }

    public static void clearAll() {
        CHECKPOINTS.clear();
    }

    public static PlayerCheckpoint getCheckpoint(UUID playerUUID) {
        return CHECKPOINTS.get(playerUUID);
    }

}

