package net.mcreator.quequeworld.voice;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.events.VoiceDistanceEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.item.ModItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ForgeVoicechatPlugin
public class QueQueVoicechatPlugin implements VoicechatPlugin {

    private static VoicechatApi voicechatApi;
    private static VoicechatServerApi serverApi;
    private final Map<UUID, PlayerAudioProcessor> processors = new ConcurrentHashMap<>();

    public static VoicechatApi getVoicechatApi() {
        return voicechatApi;
    }

    public static VoicechatServerApi getServerApi() {
        return serverApi;
    }

    @Override
    public String getPluginId() {
        return QuequeworldMod.MODID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        voicechatApi = api;
        if (api instanceof VoicechatServerApi sApi) {
            serverApi = sApi;
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoiceDistanceEvent.class, this::onVoiceDistance);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected);
    }

    private void onVoiceDistance(VoiceDistanceEvent event) {
        Object rawPlayer = event.getSenderConnection().getPlayer().getPlayer();
        if (rawPlayer instanceof ServerPlayer player) {
            if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.MEGAPHONE.get()) ||
                player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.MEGAPHONE.get())) {
                event.setDistance(event.getDistance() * 2.5f);
            }
        }
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        Object rawPlayer = event.getSenderConnection().getPlayer().getPlayer();
        if (rawPlayer instanceof ServerPlayer player) {
            // 1. GLOBAL ORB LOGIC (Broadcast static audio to all players except sender)
            if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.GLOBAL_ORB.get()) ||
                player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.GLOBAL_ORB.get())) {
                
                event.cancel();
                
                if (serverApi == null) return;
                
                try {
                    StaticSoundPacket staticPacket = event.getPacket().toStaticSoundPacket();
                    MinecraftServer server = player.getServer();
                    if (server != null) {
                        for (ServerPlayer target : server.getPlayerList().getPlayers()) {
                            // Skip sender so player doesn't hear local echo
                            if (target.getUUID().equals(player.getUUID())) continue;
                            
                            VoicechatConnection conn = serverApi.getConnectionOf(target.getUUID());
                            if (conn != null && conn.isConnected()) {
                                serverApi.sendStaticSoundPacketTo(conn, staticPacket);
                            }
                        }
                    }
                } catch (Exception e) {
                    QuequeworldMod.LOGGER.error("Error in Global Orb broadcast for player " + player.getScoreboardName(), e);
                }
                return;
            }

            // 2. MEGAPHONE FILTER LOGIC
            if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.MEGAPHONE.get()) ||
                player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.MEGAPHONE.get())) {
                
                if (voicechatApi == null) return;
                
                UUID uuid = player.getUUID();
                PlayerAudioProcessor processor = processors.computeIfAbsent(uuid, k -> new PlayerAudioProcessor(voicechatApi));
                
                try {
                    byte[] originalData = event.getPacket().getOpusEncodedData();
                    byte[] filteredData = processor.process(originalData);
                    event.getPacket().setOpusEncodedData(filteredData);
                } catch (Exception e) {
                    QuequeworldMod.LOGGER.error("Error processing megaphone voice for player " + player.getScoreboardName(), e);
                }
            }
        }
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        UUID uuid = event.getPlayerUuid();
        PlayerAudioProcessor processor = processors.remove(uuid);
        if (processor != null) {
            processor.close();
        }
    }

    // --- VOICE GROUP MANAGEMENT API ---

    public static Group createOrGetGroup(String name, Group.Type type) {
        if (serverApi == null) return null;

        for (Group group : serverApi.getGroups()) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }

        try {
            return serverApi.groupBuilder()
                    .setName(name)
                    .setType(type)
                    .setPersistent(true)
                    .setHidden(false)
                    .build();
        } catch (Exception e) {
            QuequeworldMod.LOGGER.error("Failed to create voice group: " + name, e);
            return null;
        }
    }

    public static boolean movePlayerToGroup(ServerPlayer player, Group group) {
        if (serverApi == null || player == null) return false;
        VoicechatConnection conn = serverApi.getConnectionOf(player.getUUID());
        if (conn != null && conn.isConnected()) {
            conn.setGroup(group);
            return true;
        }
        return false;
    }

    public static void clearPlayerGroup(ServerPlayer player) {
        if (serverApi == null || player == null) return;
        VoicechatConnection conn = serverApi.getConnectionOf(player.getUUID());
        if (conn != null && conn.isConnected()) {
            conn.setGroup(null);
        }
    }

    public static int clearAllGroups(MinecraftServer server) {
        if (serverApi == null || server == null) return 0;
        int count = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            VoicechatConnection conn = serverApi.getConnectionOf(player.getUUID());
            if (conn != null && conn.isConnected() && conn.isInGroup()) {
                conn.setGroup(null);
                count++;
            }
        }

        try {
            java.util.List<UUID> groupIds = new java.util.ArrayList<>();
            for (Group group : serverApi.getGroups()) {
                groupIds.add(group.getId());
            }
            for (UUID id : groupIds) {
                serverApi.removeGroup(id);
            }
        } catch (Exception e) {
            QuequeworldMod.LOGGER.error("Error purging voice groups", e);
        }

        return count;
    }

    public static int moveAllToGroup(MinecraftServer server, Group group) {
        if (serverApi == null || server == null || group == null) return 0;
        int count = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            VoicechatConnection conn = serverApi.getConnectionOf(player.getUUID());
            if (conn != null && conn.isConnected()) {
                conn.setGroup(group);
                count++;
            }
        }
        return count;
    }

    public static int setupFTBTeamGroups(MinecraftServer server) {
        if (serverApi == null || server == null) return 0;

        try {
            if (!FTBTeamsAPI.api().isManagerLoaded()) {
                QuequeworldMod.LOGGER.warn("FTB Teams Manager is not loaded yet.");
                return 0;
            }

            TeamManager teamManager = FTBTeamsAPI.api().getManager();
            Collection<Team> teams = teamManager.getTeams();
            int movedPlayers = 0;

            for (Team team : teams) {
                if (team.getMembers().isEmpty()) continue;

                String teamName = team.getShortName();
                if (teamName == null || teamName.isEmpty()) {
                    teamName = team.getName().getString();
                }

                Group voiceGroup = createOrGetGroup(teamName, Group.Type.OPEN);
                if (voiceGroup == null) continue;

                for (UUID memberId : team.getMembers()) {
                    ServerPlayer player = server.getPlayerList().getPlayer(memberId);
                    if (player != null) {
                        if (movePlayerToGroup(player, voiceGroup)) {
                            movedPlayers++;
                        }
                    }
                }
            }
            return movedPlayers;
        } catch (Throwable t) {
            QuequeworldMod.LOGGER.error("Error setting up FTB Team groups", t);
            return 0;
        }
    }

    private static class PlayerAudioProcessor {
        private final OpusDecoder decoder;
        private final OpusEncoder encoder;

        public PlayerAudioProcessor(VoicechatApi api) {
            this.decoder = api.createDecoder();
            this.encoder = api.createEncoder();
        }

        public byte[] process(byte[] inputData) {
            short[] pcm = decoder.decode(inputData);
            short[] filtered = applyMegaphoneFilter(pcm);
            return encoder.encode(filtered);
        }

        public void close() {
            if (!decoder.isClosed()) decoder.close();
            if (!encoder.isClosed()) encoder.close();
        }

        private short[] applyMegaphoneFilter(short[] pcm) {
            if (pcm == null) return null;
            short[] output = new short[pcm.length];

            // Megaphone Bandpass filter (300Hz to 3400Hz voice horn response simulation)
            double gain = 1.6;
            double alpha_lp = 0.45;
            double alpha_hp = 0.85;

            double last_in = 0;
            double last_out_hp = 0;
            double last_out_lp = 0;

            for (int i = 0; i < pcm.length; i++) {
                double input = pcm[i] * gain;

                // Soft saturation instead of hard clipping
                input = Math.tanh(input / 32768.0) * 32767.0;

                double hp_out = alpha_hp * (last_out_hp + input - last_in);
                last_in = input;
                last_out_hp = hp_out;

                double lp_out = last_out_lp + alpha_lp * (hp_out - last_out_lp);
                last_out_lp = lp_out;

                if (lp_out > 32767) lp_out = 32767;
                else if (lp_out < -32768) lp_out = -32768;

                output[i] = (short) lp_out;
            }
            return output;
        }
    }
}
