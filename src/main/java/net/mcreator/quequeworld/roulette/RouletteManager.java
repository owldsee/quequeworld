package net.mcreator.quequeworld.roulette;

import net.mcreator.quequeworld.QuequeworldMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.*;

public class RouletteManager {

    private static boolean active = false;

    public static boolean isActive() {
        return active;
    }

    public static int runRandom(CommandSourceStack source, String optionsRaw, int durationSeconds) {
        List<String> options = parseOptions(optionsRaw);
        if (options.isEmpty()) {
            source.sendFailure(Component.literal("§c❌ Debes proporcionar al menos una opción válida separada por comas."));
            return 0;
        }

        String winner = options.get(new Random().nextInt(options.size()));
        executeRouletteAnimation(source, options, winner, durationSeconds, "RULETA ALEATORIA", false, null);
        return 1;
    }

    public static int runFakeRandom(CommandSourceStack source, String forcedResult, String optionsRaw, int durationSeconds) {
        String cleanForced = forcedResult.trim();
        if (cleanForced.isEmpty()) {
            source.sendFailure(Component.literal("§c❌ Debes especificar un resultado forzado válido."));
            return 0;
        }

        List<String> options = parseOptions(optionsRaw);
        if (options.isEmpty()) {
            options.add(cleanForced);
        } else {
            boolean containsForced = options.stream().anyMatch(opt -> opt.equalsIgnoreCase(cleanForced));
            if (!containsForced) {
                options.add(cleanForced);
            }
        }

        executeRouletteAnimation(source, options, cleanForced, durationSeconds, "RULETA ALEATORIA", true, source.getTextName());
        return 1;
    }

    public static int runRandomPlayer(CommandSourceStack source, int durationSeconds) {
        MinecraftServer server = source.getServer();
        List<String> playerNames = new ArrayList<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.getTags().contains("dios")) {
                playerNames.add(player.getScoreboardName());
            }
        }

        if (playerNames.isEmpty()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                playerNames.add(player.getScoreboardName());
            }
        }

        if (playerNames.isEmpty()) {
            source.sendFailure(Component.literal("§c❌ No hay jugadores conectados para la ruleta."));
            return 0;
        }

        String winner = playerNames.get(new Random().nextInt(playerNames.size()));
        executeRouletteAnimation(source, playerNames, winner, durationSeconds, "JUGADOR ALEATORIO", false, null);
        return 1;
    }

    private static List<String> parseOptions(String optionsRaw) {
        List<String> result = new ArrayList<>();
        if (optionsRaw == null || optionsRaw.isBlank()) return result;

        String[] parts = optionsRaw.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private static void executeRouletteAnimation(CommandSourceStack source, List<String> options, String winner, int durationSeconds, String titleCategory, boolean isFake, String executorName) {
        if (active) {
            source.sendFailure(Component.literal("§c❌ Ya hay una ruleta en ejecución en este momento."));
            return;
        }

        active = true;
        MinecraftServer server = source.getServer();
        int totalTicks = Math.max(20, durationSeconds * 20);

        if (isFake) {
            MutableComponent adminNotice = Component.literal("§7[🤫 Fakerandom por §e" + (executorName != null ? executorName : "Admin") + "§7 -> Resultado forzado: §b" + winner + "§7]");
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.hasPermissions(2) || player.getTags().contains("dios")) {
                    player.sendSystemMessage(adminNotice);
                }
            }
        }

        int currentDelay = 0;
        int step = 0;
        int interval = 2;

        while (currentDelay < totalTicks) {
            final String currentDisplayOption = options.get(step % options.size());

            QuequeworldMod.queueServerWork(currentDelay, () -> {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 10, 0));
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§e§l🎲 " + currentDisplayOption)));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§6§l" + titleCategory)));
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.MASTER, 1.0f, 1.4f);
                }
            });

            step++;
            currentDelay += interval;
            if (currentDelay > totalTicks * 0.6) interval = 4;
            if (currentDelay > totalTicks * 0.8) interval = 7;
            if (currentDelay > totalTicks * 0.9) interval = 12;
        }

        QuequeworldMod.queueServerWork(totalTicks + 5, () -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§a§l🏆 ¡" + winner + "!")));
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§f" + titleCategory)));
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0f, 1.2f);
                player.sendSystemMessage(Component.literal("\n§a§l[" + titleCategory + "] §fResultado: §e§l" + winner + "\n"));
            }
            active = false;
        });
    }
}
