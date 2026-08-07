package net.mcreator.quequeworld.minigame;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.event.ModEvents;
import net.mcreator.quequeworld.event.SoulShieldEventHandler;
import net.mcreator.quequeworld.network.ThreatAnimationPacket;
import net.mcreator.quequeworld.world.QueQueWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;

@EventBusSubscriber(modid = QuequeworldMod.MODID)
public class DeathAndRespawnManager {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag data = player.getPersistentData();
        boolean hadShield = player.getTags().contains("tiene_escudo");
        boolean wasThreatened = player.getTags().contains("amenazado");

        data.putBoolean("qqw_had_shield", hadShield);
        data.putBoolean("qqw_was_threatened", wasThreatened);

        ListTag invNbt = player.getInventory().save(new ListTag());
        data.put("qqw_saved_inv", invNbt);
        data.putInt("qqw_saved_selected_slot", player.getInventory().selected);

        data.putDouble("qqw_death_x", player.getX());
        data.putDouble("qqw_death_y", player.getY() + 0.5);
        data.putDouble("qqw_death_z", player.getZ());
        data.putString("qqw_death_dim", player.level().dimension().location().toString());
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.getTags().contains("tiene_escudo") || player.getTags().contains("qqw_mecanica_morir_disabled")) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        ServerPlayer original = (ServerPlayer) event.getOriginal();
        ServerPlayer clone = (ServerPlayer) event.getEntity();

        CompoundTag srcData = original.getPersistentData();
        CompoundTag destData = clone.getPersistentData();

        destData.merge(srcData.copy());
        clone.getTags().addAll(original.getTags());

        int savedSelectedSlot = srcData.contains("qqw_saved_selected_slot") ? srcData.getInt("qqw_saved_selected_slot") : original.getInventory().selected;

        boolean hadShield = destData.getBoolean("qqw_had_shield");
        boolean mechanicsDisabled = clone.getTags().contains("qqw_mecanica_morir_disabled");

        if (mechanicsDisabled) {
            if (destData.contains("qqw_saved_inv")) {
                ListTag invNbt = destData.getList("qqw_saved_inv", Tag.TAG_COMPOUND);
                clone.getInventory().load(invNbt);
            }
        } else {
            if (hadShield) {
                handleShieldBroken(original, clone, destData);
            } else {
                handleThreatened(original, clone, destData);
            }
        }

        clone.getInventory().selected = savedSelectedSlot;

        destData.remove("qqw_had_shield");
        destData.remove("qqw_was_threatened");
        destData.remove("qqw_saved_inv");
        destData.remove("qqw_saved_selected_slot");
        destData.remove("qqw_death_x");
        destData.remove("qqw_death_y");
        destData.remove("qqw_death_z");
        destData.remove("qqw_death_dim");
        destData.putBoolean("qqw_sync_force", true);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.getTags().contains("qqw_en_desafio")) {
            CheckpointManager.PlayerCheckpoint cp = CheckpointManager.getCheckpoint(player.getUUID());

            ServerLevel targetLevel = null;
            double targetX, targetY, targetZ;
            float targetYaw, targetPitch;

            if (cp != null) {
                for (ServerLevel level : player.getServer().getAllLevels()) {
                    if (level.dimension().location().toString().equals(cp.dimension)) {
                        targetLevel = level;
                        break;
                    }
                }
                if (targetLevel == null) targetLevel = player.serverLevel();
                targetX = cp.x;
                targetY = cp.y;
                targetZ = cp.z;
                targetYaw = cp.yaw;
                targetPitch = cp.pitch;
            } else {
                QueQueWorldData data = QueQueWorldData.get(player.serverLevel());
                if (data.desafioSpawnSet) {
                    for (ServerLevel level : player.getServer().getAllLevels()) {
                        if (level.dimension().location().toString().equals(data.desafioSpawnDim)) {
                            targetLevel = level;
                            break;
                        }
                    }
                    if (targetLevel == null) targetLevel = player.serverLevel();
                    net.minecraft.world.phys.Vec3 safePos = ModEvents.findSafeGround(targetLevel, data.desafioSpawnX, data.desafioSpawnY, data.desafioSpawnZ, 5.0);
                    targetX = safePos.x;
                    targetY = safePos.y;
                    targetZ = safePos.z;
                    targetYaw = data.desafioSpawnYaw;
                    targetPitch = data.desafioSpawnPitch;
                } else if (player.getPersistentData().contains("marked_x")) {
                    String dim = player.getPersistentData().getString("marked_dim");
                    for (ServerLevel level : player.getServer().getAllLevels()) {
                        if (level.dimension().location().toString().equals(dim)) {
                            targetLevel = level;
                            break;
                        }
                    }
                    if (targetLevel == null) targetLevel = player.serverLevel();

                    double mx = player.getPersistentData().getDouble("marked_x");
                    double my = player.getPersistentData().getDouble("marked_y");
                    double mz = player.getPersistentData().getDouble("marked_z");
                    float myaw = player.getPersistentData().getFloat("marked_yaw");
                    float mpitch = player.getPersistentData().getFloat("marked_pitch");

                    net.minecraft.world.phys.Vec3 safePos = ModEvents.findSafeGround(targetLevel, mx, my, mz, 5.0);
                    targetX = safePos.x;
                    targetY = safePos.y;
                    targetZ = safePos.z;
                    targetYaw = myaw;
                    targetPitch = mpitch;
                } else {
                    return;
                }
            }

            teleportAndOrientPlayer(player, targetLevel, targetX, targetY, targetZ, targetYaw, targetPitch);
        }
    }

    @SubscribeEvent
    public static void onDesafioPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.getTags().contains("qqw_en_desafio")) {
            serverPlayer.getFoodData().setFoodLevel(20);
            serverPlayer.getFoodData().setSaturation(20.0f);

            if (serverPlayer.getY() < serverPlayer.level().getMinBuildHeight() - 10) {
                CheckpointManager.PlayerCheckpoint cp = CheckpointManager.getCheckpoint(serverPlayer.getUUID());

                ServerLevel targetLevel = null;
                double targetX, targetY, targetZ;
                float targetYaw, targetPitch;

                if (cp != null) {
                    for (ServerLevel level : serverPlayer.getServer().getAllLevels()) {
                        if (level.dimension().location().toString().equals(cp.dimension)) {
                            targetLevel = level;
                            break;
                        }
                    }
                    if (targetLevel == null) targetLevel = serverPlayer.serverLevel();
                    targetX = cp.x;
                    targetY = cp.y;
                    targetZ = cp.z;
                    targetYaw = cp.yaw;
                    targetPitch = cp.pitch;
                } else {
                    QueQueWorldData data = QueQueWorldData.get(serverPlayer.serverLevel());
                    if (data.desafioSpawnSet) {
                        for (ServerLevel level : serverPlayer.getServer().getAllLevels()) {
                            if (level.dimension().location().toString().equals(data.desafioSpawnDim)) {
                                targetLevel = level;
                                break;
                            }
                        }
                        if (targetLevel == null) targetLevel = serverPlayer.serverLevel();
                        net.minecraft.world.phys.Vec3 safe = ModEvents.findSafeGround(targetLevel, data.desafioSpawnX, data.desafioSpawnY, data.desafioSpawnZ, 5.0);
                        targetX = safe.x;
                        targetY = safe.y;
                        targetZ = safe.z;
                        targetYaw = data.desafioSpawnYaw;
                        targetPitch = data.desafioSpawnPitch;
                    } else {
                        return;
                    }
                }

                serverPlayer.setDeltaMovement(0, 0, 0);
                serverPlayer.fallDistance = 0.0f;
                teleportAndOrientPlayer(serverPlayer, targetLevel, targetX, targetY, targetZ, targetYaw, targetPitch);
                serverPlayer.displayClientMessage(Component.literal("⚠️ §cCaíste al vacío. Rescatado al Checkpoint/Spawn de desafío."), true);
            }
        }
    }

    public static void teleportAndOrientPlayer(ServerPlayer player, ServerLevel targetLevel, double x, double y, double z, float yaw, float pitch) {
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(yaw);

        player.teleportTo(targetLevel, x, y, z, yaw, pitch);

        try {
            player.connection.teleport(x, y, z, yaw, pitch, Collections.emptySet());
        } catch (Exception ignored) {}

        ModEvents.playPuffEffect(targetLevel, x, y, z);
    }

    private static void handleShieldBroken(ServerPlayer original, ServerPlayer clone, CompoundTag data) {
        original.removeTag("tiene_escudo");
        clone.removeTag("tiene_escudo");

        if (data.contains("qqw_saved_inv")) {
            ListTag invNbt = data.getList("qqw_saved_inv", Tag.TAG_COMPOUND);
            clone.getInventory().load(invNbt);
        }

        QuequeworldMod.queueServerWork(5, () -> {
            if (clone.isAlive()) {
                playDirectSound(clone, SoundEvents.GLASS_BREAK, 1.0f, 0.5f);
                PacketDistributor.sendToPlayer(clone, new ThreatAnimationPacket("quequeworld:escudo_visual"));

                Component title = Component.literal("")
                    .append(Component.literal("\uE101 ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("ESCUDO ROTO").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .append(Component.literal(" \uE101").withStyle(ChatFormatting.WHITE));
                Component subtitle = Component.literal("¡El Escudo de Alma de " + clone.getScoreboardName() + " se ha roto!").withStyle(ChatFormatting.YELLOW);
                sendTitleToAll(clone.getServer(), title, subtitle, 10, 50, 20);
            }
        });

        broadcastToAll(clone.getServer(),
            "[\"\","
            + "{\"text\":\"🛡️ ¡El Escudo de Alma de \",\"color\":\"gold\",\"bold\":true},"
            + "{\"text\":\"" + clone.getScoreboardName() + "\",\"color\":\"yellow\",\"bold\":true},"
            + "{\"text\":\" se ha roto! Su vida ahora corre peligro.\",\"color\":\"gold\",\"bold\":false}"
            + "]"
        );
    }

    private static void handleThreatened(ServerPlayer original, ServerPlayer clone, CompoundTag data) {
        original.removeTag("amenazado");
        clone.addTag("amenazado");

        dropInventoryAtDeathPos(clone, data, 0.5);
        clone.getInventory().clearContent();

        int banderines = data.getInt("qqw_banderines");
        int deuda = data.getInt("qqw_deuda_banderines");

        banderines = Math.max(0, banderines - 1);
        deuda = Math.min(8, deuda + 2);

        data.putInt("qqw_banderines", banderines);
        data.putInt("qqw_deuda_banderines", deuda);

        final int currentDeuda = deuda;

        QuequeworldMod.queueServerWork(5, () -> {
            if (clone.isAlive()) {
                playDirectSound(clone, SoundEvents.WITHER_SPAWN, 1.0f, 0.5f);
                PacketDistributor.sendToPlayer(clone, new ThreatAnimationPacket("quequeworld:amenazado_visual"));

                Component title = Component.literal("")
                    .append(Component.literal("\uE100 ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("¡AMENAZADO!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                    .append(Component.literal(" \uE100").withStyle(ChatFormatting.WHITE));
                Component subtitle = Component.literal(clone.getScoreboardName() + " entra al Desafío (Deuda: " + currentDeuda + " Banderines)").withStyle(ChatFormatting.RED);
                sendTitleToAll(clone.getServer(), title, subtitle, 10, 60, 20);
            }
        });

        broadcastToAll(clone.getServer(),
            "[\"\","
            + "{\"text\":\"☠️ ¡AMENAZADO! \",\"color\":\"dark_red\",\"bold\":true},"
            + "{\"text\":\"" + clone.getScoreboardName() + "\",\"color\":\"red\",\"underlined\":true},"
            + "{\"text\":\" ha muerto sin escudo (Deuda: " + currentDeuda + " banderines).\",\"color\":\"gray\",\"bold\":false}"
            + "]"
        );
    }

    private static void dropInventoryAtDeathPos(ServerPlayer clone, CompoundTag data, double dropChance) {
        if (!data.contains("qqw_saved_inv")) return;

        double dx = data.getDouble("qqw_death_x");
        double dy = data.getDouble("qqw_death_y");
        double dz = data.getDouble("qqw_death_z");
        String dimStr = data.getString("qqw_death_dim");

        MinecraftServer server = clone.getServer();
        if (server == null) return;

        ServerLevel deathLevel = null;
        try {
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
            deathLevel = server.getLevel(dimKey);
        } catch (Exception ignored) {}

        if (deathLevel == null) {
            deathLevel = clone.serverLevel();
        }

        net.minecraft.world.entity.player.Inventory tempInv = new net.minecraft.world.entity.player.Inventory(clone);
        ListTag invNbt = data.getList("qqw_saved_inv", Tag.TAG_COMPOUND);
        tempInv.load(invNbt);

        for (int i = 0; i < tempInv.getContainerSize(); i++) {
            ItemStack stack = tempInv.getItem(i);
            if (stack.isEmpty()) continue;

            if (dropChance >= 1.0 || Math.random() < dropChance) {
                ItemEntity itemEntity = new ItemEntity(deathLevel, dx, dy, dz, stack.copy());
                itemEntity.setDeltaMovement(
                    (Math.random() - 0.5) * 0.15,
                    0.2 + Math.random() * 0.1,
                    (Math.random() - 0.5) * 0.15
                );
                itemEntity.setPickUpDelay(40);
                deathLevel.addFreshEntity(itemEntity);
            }
        }
    }

    private static void playDirectSound(ServerPlayer player, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        if (player.level() instanceof ServerLevel sl) {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
        }
        try {
            player.connection.send(new ClientboundSoundPacket(
                net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                SoundSource.MASTER,
                player.getX(), player.getY(), player.getZ(),
                volume * 2.0f, pitch, player.level().random.nextLong()
            ));
        } catch (Exception ignored) {}
    }

    private static void sendTitleToAll(MinecraftServer server, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            try {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
                if (title != null) {
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                }
                if (subtitle != null) {
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            } catch (Exception ignored) {}
        }
    }

    private static void broadcastToAll(MinecraftServer server, String jsonMessage) {
        if (server == null) return;
        Component component;
        try {
            component = Component.Serializer.fromJson(jsonMessage, server.registryAccess());
        } catch (Exception e) {
            return;
        }
        if (component == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(component);
        }
    }
}
