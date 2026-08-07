package net.mcreator.quequeworld.event;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.network.ThreatAnimationPacket;
import net.mcreator.quequeworld.world.QueQueWorldData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Maneja la mecánica del Escudo de Alma, la etiqueta "Amenazado", "Eliminado" al morir.
 *
 * REQUISITO DEL SERVIDOR:
 *   /gamerule keepInventory true
 */
public class SoulShieldEventHandler {



    // ─────────────────────────────────────────────────────────────────────────
    // ESCUDO ROTO → restaurar inventario + efectos + mensaje
    // ─────────────────────────────────────────────────────────────────────────
    private static void handleShieldBroken(ServerPlayer original, ServerPlayer clone, CompoundTag data) {
        // Quitar tag de ambas entidades (clone lo heredó de restoreFrom en tags internos)
        original.removeTag("tiene_escudo");
        clone.removeTag("tiene_escudo");

        // Restaurar inventario completo al clone
        if (data.contains("qqw_saved_inv")) {
            ListTag invNbt = data.getList("qqw_saved_inv", Tag.TAG_COMPOUND);
            clone.getInventory().load(invNbt);
        }

        // Ejecutar efectos con delay de 5 ticks para asegurar que el cliente de respawn los reciba
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

        // ── MENSAJE para todos ──
        broadcastToAll(clone.getServer(),
            "[\"\","
            + "{\"text\":\"🛡️ ¡El Escudo de Alma de \",\"color\":\"gold\",\"bold\":true},"
            + "{\"text\":\"" + clone.getScoreboardName() + "\",\"color\":\"yellow\",\"bold\":true},"
            + "{\"text\":\" se ha roto! Su vida ahora corre peligro.\",\"color\":\"gold\",\"bold\":false}"
            + "]"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIN ESCUDO → amenazado: droppear items, efectos, animación de amenaza
    // ─────────────────────────────────────────────────────────────────────────
    private static void handleThreatened(ServerPlayer original, ServerPlayer clone, CompoundTag data) {
        original.removeTag("amenazado"); // Por si ya lo tenía antes
        clone.addTag("amenazado");

        // ── DROPPEAR 50% del inventario en la posición de muerte ──
        dropInventoryAtDeathPos(clone, data, 0.5);
        // Con keepInventory=true el clone ya tiene los items → limpiar lo que quedó
        clone.getInventory().clearContent();

        // ── MECÁNICA DE BANDERINES Y DEUDA ──
        int banderines = data.getInt("qqw_banderines");
        int deuda = data.getInt("qqw_deuda_banderines");

        net.mcreator.quequeworld.config.QueQueDifficultyConfig.DangerSettings cfg = net.mcreator.quequeworld.config.QueQueDifficultyConfig.instance.danger;
        int banderinLossRatio = cfg != null ? cfg.banderin_loss_ratio : 5;
        int banderinLossBase = cfg != null ? cfg.banderin_loss_base : 0;
        int banderinesLost = banderinLossBase + (banderinLossRatio > 0 ? (banderines / banderinLossRatio) : 0);

        int deudaGainRatio = cfg != null ? cfg.deuda_gain_ratio : 10;
        int deudaGainPerRatio = cfg != null ? cfg.deuda_gain_per_ratio : 2;
        int deudaGainBase = cfg != null ? cfg.deuda_gain_base : 1;
        int deudaGained = deudaGainBase + (deudaGainRatio > 0 ? (banderines / deudaGainRatio) * deudaGainPerRatio : 0);

        banderines = Math.max(0, banderines - banderinesLost);
        deuda = Math.min(8, deuda + deudaGained);

        data.putInt("qqw_banderines", banderines);
        data.putInt("qqw_deuda_banderines", deuda);

        final int currentDeuda = deuda;

        // Ejecutar efectos con delay de 5 ticks
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

        // ── MENSAJE para todos ──
        broadcastToAll(clone.getServer(),
            "[\"\","
            + "{\"text\":\"☠️ ¡AMENAZADO! \",\"color\":\"dark_red\",\"bold\":true},"
            + "{\"text\":\"" + clone.getScoreboardName() + "\",\"color\":\"red\",\"underlined\":true},"
            + "{\"text\":\" ha muerto sin escudo (Deuda: " + currentDeuda + " banderines).\",\"color\":\"gray\",\"bold\":false}"
            + "]"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METODOS ADMINISTRATIVOS (Llamados por Comandos)
    // ─────────────────────────────────────────────────────────────────────────

    public static void applyShield(ServerPlayer player) {
        player.addTag("tiene_escudo");
        player.removeTag("amenazado");
        player.removeTag("fantasma");
        
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            player.setGameMode(GameType.SURVIVAL);
        }

        playDirectSound(player, SoundEvents.BEACON_ACTIVATE, 1.0f, 1.0f);
        PacketDistributor.sendToPlayer(player, new ThreatAnimationPacket("quequeworld:escudo_visual"));

        Component title = Component.literal("")
            .append(Component.literal("\uE101 ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("ESCUDO ACTIVO").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal(" \uE101").withStyle(ChatFormatting.WHITE));
        Component subtitle = Component.literal("¡Se ha restaurado el Escudo de Alma de " + player.getScoreboardName() + "!").withStyle(ChatFormatting.YELLOW);
        sendTitleToAll(player.getServer(), title, subtitle, 10, 50, 20);

        player.getPersistentData().putBoolean("qqw_sync_force", true);
    }

    public static void applyThreat(ServerPlayer player) {
        player.removeTag("tiene_escudo");
        player.addTag("amenazado");
        player.removeTag("fantasma");

        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            player.setGameMode(GameType.SURVIVAL);
        }

        playDirectSound(player, SoundEvents.WITHER_SPAWN, 1.0f, 1.0f);
        PacketDistributor.sendToPlayer(player, new ThreatAnimationPacket("quequeworld:amenazado_visual"));

        Component title = Component.literal("")
            .append(Component.literal("\uE100 ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("¡AMENAZADO!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
            .append(Component.literal(" \uE100").withStyle(ChatFormatting.WHITE));
        Component subtitle = Component.literal(player.getScoreboardName() + " entra al Desafío de Eliminación").withStyle(ChatFormatting.RED);
        sendTitleToAll(player.getServer(), title, subtitle, 10, 60, 20);

        player.getPersistentData().putBoolean("qqw_sync_force", true);
    }

    public static void applyEliminated(ServerPlayer player) {
        player.removeTag("tiene_escudo");
        player.removeTag("amenazado");
        player.addTag("fantasma");

        // Mantener en Survival para desempeñar el Rol Auxiliar Fantasma
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            player.setGameMode(GameType.SURVIVAL);
        }


        // Efectos y trueno
        if (player.level() instanceof ServerLevel sl) {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 2.0f, 1.0f);
        }
        playDirectSound(player, SoundEvents.LIGHTNING_BOLT_THUNDER, 1.5f, 1.0f);
        PacketDistributor.sendToPlayer(player, new ThreatAnimationPacket("quequeworld:eliminado_visual"));

        Component title = Component.literal("")
            .append(Component.literal("\uE102 ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("¡ELIMINADO!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            .append(Component.literal(" \uE102").withStyle(ChatFormatting.WHITE));
        Component subtitle = Component.literal(player.getScoreboardName() + " pasa al Rol de Fantasma Auxiliar").withStyle(ChatFormatting.GRAY);
        sendTitleToAll(player.getServer(), title, subtitle, 10, 80, 20);

        broadcastToAll(player.getServer(),
            "[\"\","
            + "{\"text\":\"❌ ¡ELIMINADO! \",\"color\":\"dark_red\",\"bold\":true},"
            + "{\"text\":\"" + player.getScoreboardName() + "\",\"color\":\"red\",\"bold\":true},"
            + "{\"text\":\" ha sido eliminado y pasa al Rol de Fantasma Auxiliar.\",\"color\":\"gray\",\"bold\":false}"
            + "]"
        );

        player.getPersistentData().putBoolean("qqw_sync_force", true);
    }


    public static void addBanderin(ServerPlayer player, int cantidad) {
        CompoundTag tagData = player.getPersistentData();
        int banderines = tagData.getInt("qqw_banderines");
        int deudas = tagData.getInt("qqw_deuda_banderines");

        // Si tiene deuda, pagarla primero con la cantidad recibida
        boolean hadDebt = (deudas > 0);
        int paid = 0;
        if (hadDebt) {
            paid = Math.min(deudas, cantidad);
            deudas -= paid;
            tagData.putInt("qqw_deuda_banderines", deudas);
        }

        // Solo sumar a banderines el remanente fuera de la deuda
        int remainder = cantidad - paid;
        if (remainder > 0) {
            banderines += remainder;
            tagData.putInt("qqw_banderines", banderines);
        }

        final int finalDeuda = deudas;

        // Efectos del banderín
        playDirectSound(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        PacketDistributor.sendToPlayer(player, new ThreatAnimationPacket("quequeworld:banderin_visual"));

        Component title = Component.literal("")
            .append(Component.literal("\uE103 ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("BANDERÍN").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .append(Component.literal(" \uE103").withStyle(ChatFormatting.WHITE));
        String subtitleText = "¡" + player.getScoreboardName() + " ha recibido " + cantidad + " banderín" + (cantidad > 1 ? "es" : "") + "!";
        if (hadDebt) {
            subtitleText += " (Deuda: " + finalDeuda + ")";
        }
        Component subtitle = Component.literal(subtitleText).withStyle(ChatFormatting.BLUE);
        sendTitleToAll(player.getServer(), title, subtitle, 10, 60, 20);

        // Notificación en el chat del jugador
        player.sendSystemMessage(Component.literal("§a[QueQueWorld] ¡Has recibido " + cantidad + " banderín" + (cantidad > 1 ? "es" : "") + "!"));
        if (hadDebt && paid > 0) {
            player.sendSystemMessage(Component.literal("§a[QueQueWorld] Has pagado " + paid + " banderín" + (paid > 1 ? "es" : "") + " de tu deuda. Deuda restante: " + finalDeuda));
        }

        // Si pagó toda su deuda, remover el estado de amenaza
        if (finalDeuda == 0) {
            checkAndClearThreat(player, finalDeuda);
        }

        tagData.putBoolean("qqw_sync_force", true);
    }

    public static void removeBanderin(ServerPlayer player, int cantidad) {
        CompoundTag tagData = player.getPersistentData();
        int banderines = tagData.getInt("qqw_banderines");
        banderines = Math.max(0, banderines - cantidad);
        tagData.putInt("qqw_banderines", banderines);
        player.sendSystemMessage(Component.literal("§c[QueQueWorld] Se te han retirado " + cantidad + " banderín" + (cantidad > 1 ? "es" : "") + ". Total restante: " + banderines));
        tagData.putBoolean("qqw_sync_force", true);
    }

    public static void setBanderines(ServerPlayer player, int cantidad) {
        CompoundTag tagData = player.getPersistentData();
        int banderines = Math.max(0, cantidad);
        tagData.putInt("qqw_banderines", banderines);
        player.sendSystemMessage(Component.literal("§a[QueQueWorld] Tus banderines se han establecido en: " + banderines));
        tagData.putBoolean("qqw_sync_force", true);
    }

    public static void addDeuda(ServerPlayer player, int cantidad) {
        CompoundTag tagData = player.getPersistentData();
        int deudas = tagData.getInt("qqw_deuda_banderines");
        deudas = Math.min(8, deudas + cantidad);
        tagData.putInt("qqw_deuda_banderines", deudas);
        player.sendSystemMessage(Component.literal("§c[QueQueWorld] Se te ha añadido " + cantidad + " de deuda de banderines. Deuda total: " + deudas + "/8"));
        tagData.putBoolean("qqw_sync_force", true);
    }

    public static void removeDeuda(ServerPlayer player, int cantidad) {
        CompoundTag tagData = player.getPersistentData();
        int deudas = tagData.getInt("qqw_deuda_banderines");
        deudas = Math.max(0, deudas - cantidad);
        tagData.putInt("qqw_deuda_banderines", deudas);
        player.sendSystemMessage(Component.literal("§a[QueQueWorld] Se te ha reducido " + cantidad + " de deuda de banderines. Deuda restante: " + deudas));
        checkAndClearThreat(player, deudas);
        tagData.putBoolean("qqw_sync_force", true);
    }

    public static void setDeuda(ServerPlayer player, int cantidad) {
        CompoundTag tagData = player.getPersistentData();
        int deudas = Math.max(0, Math.min(8, cantidad));
        tagData.putInt("qqw_deuda_banderines", deudas);
        player.sendSystemMessage(Component.literal("§a[QueQueWorld] Tu deuda de banderines se ha establecido en: " + deudas + "/8"));
        checkAndClearThreat(player, deudas);
        tagData.putBoolean("qqw_sync_force", true);
    }

    public static void checkAndClearThreat(ServerPlayer player, int finalDebt) {
        if (player.getTags().contains("amenazado") && finalDebt == 0) {
            player.removeTag("amenazado");
            
            // Sonido de cura/éxito para todos
            playDirectSound(player, SoundEvents.PLAYER_LEVELUP, 1.0f, 0.8f);
            
            // Mensaje de liberación de amenaza
            Component cleanTitle = Component.literal("")
                .append(Component.literal("\uE103 ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("AMENAZA QUITADA").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .append(Component.literal(" \uE103").withStyle(ChatFormatting.WHITE));
            Component cleanSub = Component.literal("¡" + player.getScoreboardName() + " pagó su deuda y sale del Desafío!").withStyle(ChatFormatting.WHITE);
            sendTitleToAll(player.getServer(), cleanTitle, cleanSub, 10, 60, 20);

            broadcastToAll(player.getServer(),
                "[\"\","
                + "{\"text\":\"💖 ¡AMENAZA QUITADA! \",\"color\":\"green\",\"bold\":true},"
                + "{\"text\":\"" + player.getScoreboardName() + "\",\"color\":\"white\",\"bold\":true},"
                + "{\"text\":\" ha recuperado los banderines perdidos y sale del Desafío de Eliminación.\",\"color\":\"gray\",\"bold\":false}"
                + "]"
            );
        }
    }

    public static double getDangerLevel(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        int banderines = tag.getInt("qqw_banderines");
        int deudas = tag.getInt("qqw_deuda_banderines");
        
        QueQueWorldData worldData = QueQueWorldData.get(player.serverLevel());
        int minima = worldData.minBanderines;
        
        net.mcreator.quequeworld.config.QueQueDifficultyConfig.DangerSettings cfg = net.mcreator.quequeworld.config.QueQueDifficultyConfig.instance.danger;
        double base = cfg != null ? cfg.base_danger : 0.50D;
        double banderinBonus = cfg != null ? cfg.danger_per_banderin : 0.02D;
        double deudaPenalty = cfg != null ? cfg.danger_penalty_per_deuda : 0.05D;

        double percent = base + ((banderines - minima) * banderinBonus) - (deudas * deudaPenalty);
        return Math.max(0.0D, percent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

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

        net.minecraft.world.entity.player.Inventory tempInv =
                new net.minecraft.world.entity.player.Inventory(clone);
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

    private static void dropCurrentInventory(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double dx = player.getX();
        double dy = player.getY() + 0.5;
        double dz = player.getZ();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level, dx, dy, dz, stack.copy());
                itemEntity.setDeltaMovement(
                    (Math.random() - 0.5) * 0.15,
                    0.2 + Math.random() * 0.1,
                    (Math.random() - 0.5) * 0.15
                );
                itemEntity.setPickUpDelay(40);
                level.addFreshEntity(itemEntity);
            }
        }
        player.getInventory().clearContent();
    }

    private static void playDirectSound(ServerPlayer player,
                                         net.minecraft.sounds.SoundEvent sound,
                                         float volume, float pitch) {
        if (player.level() instanceof ServerLevel sl) {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    sound, SoundSource.PLAYERS, volume, pitch);
        }
        try {
            player.connection.send(new ClientboundSoundPacket(
                net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT
                    .wrapAsHolder(sound),
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
            } catch (Exception e) {
                QuequeworldMod.LOGGER.error("[QQW] Error enviando título en pantalla a " + player.getScoreboardName(), e);
            }
        }
    }

    private static void broadcastToAll(MinecraftServer server, String jsonMessage) {
        if (server == null) return;
        Component component;
        try {
            component = Component.Serializer.fromJson(jsonMessage, server.registryAccess());
        } catch (Exception e) {
            QuequeworldMod.LOGGER.error("[QQW] Error parseando mensaje de broadcast: {}", e.getMessage());
            return;
        }
        if (component == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(component);
        }
    }
}
