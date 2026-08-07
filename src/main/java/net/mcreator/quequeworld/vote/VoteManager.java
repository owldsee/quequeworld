package net.mcreator.quequeworld.vote;

import net.mcreator.quequeworld.QuequeworldMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VoteManager {

    public static class VoteSession {
        private final String title;
        private final List<String> options;
        private final Set<UUID> eligibleVoters;
        private final Map<UUID, String> votes = new ConcurrentHashMap<>();
        private boolean active = true;
        private boolean rouletteActive = false;

        public VoteSession(String title, List<String> options, Set<UUID> eligibleVoters) {
            this.title = title;
            this.options = new ArrayList<>(options);
            this.eligibleVoters = eligibleVoters != null ? new HashSet<>(eligibleVoters) : null;
        }

        public String getTitle() { return title; }
        public List<String> getOptions() { return options; }
        public Map<UUID, String> getVotes() { return votes; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isRouletteActive() { return rouletteActive; }
        public void setRouletteActive(boolean rouletteActive) { this.rouletteActive = rouletteActive; }

        public boolean isEligible(UUID playerUUID) {
            if (eligibleVoters == null || eligibleVoters.isEmpty()) return true;
            return eligibleVoters.contains(playerUUID);
        }
    }

    private static VoteSession currentSession = null;

    public static VoteSession getCurrentSession() {
        return currentSession;
    }

    public static VoteSession createSession(String title, List<String> options, Set<UUID> eligibleVoters) {
        currentSession = new VoteSession(title, options, eligibleVoters);
        return currentSession;
    }

    public static void cancelSession() {
        if (currentSession != null) {
            currentSession.setActive(false);
            currentSession = null;
        }
    }

    public static boolean castVote(ServerPlayer player, String option) {
        if (currentSession == null || !currentSession.isActive()) {
            player.sendSystemMessage(Component.literal("§c❌ No hay ninguna votación activa en este momento."));
            return false;
        }

        if (!currentSession.isEligible(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§c❌ No tienes permiso para votar en esta encuesta."));
            return false;
        }

        if (!currentSession.getOptions().contains(option)) {
            player.sendSystemMessage(Component.literal("§c❌ La opción '" + option + "' no es válida en esta votación."));
            return false;
        }

        currentSession.getVotes().put(player.getUUID(), option);
        player.sendSystemMessage(Component.literal("§a✔ Tu voto por '§e" + option + "§a' ha sido registrado correctamente."));
        return true;
    }

    public static void sendVoteBroadcast(MinecraftServer server, VoteSession session) {
        MutableComponent header = Component.literal("\n§e========================================\n")
                .append(Component.literal("§6§l🗳️ VOTACIÓN: §f" + session.getTitle() + "\n"))
                .append(Component.literal("§7Haz clic en una opción para votar:\n\n"));

        for (String option : session.getOptions()) {
            MutableComponent btn = Component.literal("  §8[ §b§lVOTAR: " + option + " §8] ")
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qqw votacion votar " + option))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("§aHaz clic para votar por §e" + option))));
            header.append(btn).append(Component.literal("\n"));
        }

        header.append(Component.literal("\n§e========================================\n"));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (session.isEligible(player.getUUID())) {
                player.sendSystemMessage(header);
            }
        }
    }

    public static void revealResults(CommandSourceStack source, int durationSeconds) {
        if (currentSession == null) {
            source.sendFailure(Component.literal("❌ No hay ninguna votación para revelar resultados."));
            return;
        }

        final VoteSession session = currentSession;
        session.setActive(false); // Invalidate interactive buttons immediately
        session.setRouletteActive(true);

        MinecraftServer server = source.getServer();

        // 1. Calculate vote counts
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, List<String>> votersPerOption = new LinkedHashMap<>();

        for (String opt : session.getOptions()) {
            counts.put(opt, 0);
            votersPerOption.put(opt, new ArrayList<>());
        }

        for (Map.Entry<UUID, String> entry : session.getVotes().entrySet()) {
            String opt = entry.getValue();
            if (counts.containsKey(opt)) {
                counts.put(opt, counts.get(opt) + 1);
                ServerPlayer voter = server.getPlayerList().getPlayer(entry.getKey());
                String voterName = voter != null ? voter.getScoreboardName() : entry.getKey().toString().substring(0, 8);
                votersPerOption.get(opt).add(voterName);
            }
        }

        // 2. Find winners
        int maxVotes = -1;
        List<String> winners = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                winners.clear();
                winners.add(entry.getKey());
            } else if (entry.getValue() == maxVotes) {
                winners.add(entry.getKey());
            }
        }

        // Handle case if zero votes cast
        if (maxVotes <= 0) {
            winners.clear();
            winners.add("Sin Votos");
        }

        // 3. Detailed Admin Report for Dioses
        MutableComponent adminReport = Component.literal("\n§e=== 📊 RESULTADOS DETALLADOS (DIOSES) ===\n")
                .append(Component.literal("§fTítulo: §b" + session.getTitle() + "\n"))
                .append(Component.literal("§fTotal Votos Emitidos: §a" + session.getVotes().size() + "\n\n"));

        for (String opt : session.getOptions()) {
            int cnt = counts.get(opt);
            List<String> voters = votersPerOption.get(opt);
            String voterListStr = voters.isEmpty() ? "§7(Ninguno)" : "§7(" + String.join(", ", voters) + ")";
            adminReport.append(Component.literal("§e• " + opt + ": §a" + cnt + " voto(s) " + voterListStr + "\n"));
        }
        adminReport.append(Component.literal("§e========================================\n"));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.hasPermissions(2) || player.getTags().contains("dios")) {
                player.sendSystemMessage(adminReport);
            }
        }

        // 4. Roulette Animation
        int totalTicks = Math.max(20, durationSeconds * 20);
        List<String> optionsPool = session.getOptions();

        // Schedule steps of roulette
        int currentDelay = 0;
        int step = 0;
        int interval = 2; // Start fast

        while (currentDelay < totalTicks) {
            final int nextOptionIndex = step % optionsPool.size();
            final String currentDisplayOption = optionsPool.get(nextOptionIndex);

            QuequeworldMod.queueServerWork(currentDelay, () -> {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 10, 0));
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§e§l🎲 " + currentDisplayOption)));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§7Votación en curso...")));
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.MASTER, 1.0f, 1.4f);
                }
            });

            step++;
            currentDelay += interval;
            if (currentDelay > totalTicks * 0.6) interval = 4;
            if (currentDelay > totalTicks * 0.8) interval = 7;
            if (currentDelay > totalTicks * 0.9) interval = 12;
        }

        // 5. Final Winner Announcement
        final boolean isTie = winners.size() > 1 && maxVotes > 0;
        final String winnerText = isTie ? String.join(", ", winners) : (winners.isEmpty() ? "Sin Resultado" : winners.get(0));
        final int finalMaxVotes = maxVotes;

        QuequeworldMod.queueServerWork(totalTicks + 10, () -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
                if (isTie) {
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§c§l¡EMPATE!")));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§e" + winnerText + " §7(" + finalMaxVotes + " votos)")));
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0f, 0.8f);
                } else {
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§a§l🏆 ¡" + winnerText + "!")));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§fGanador de la votación §7(" + finalMaxVotes + " votos)")));
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0f, 1.2f);
                }

                player.sendSystemMessage(Component.literal("\n§a§l[VOTACIÓN FINALIZADA] §fGanador: §e" + winnerText + " §7(" + finalMaxVotes + " votos)\n"));
            }
            session.setRouletteActive(false);
            currentSession = null;
        });
    }
}
