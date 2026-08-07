package net.mcreator.quequeworld.minigame;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.mcreator.quequeworld.event.ModEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class GoalManager {
    public static boolean isTeamMode = true;
    public static int arrivalCount = 0;
    public static final Set<String> finishedEntities = new LinkedHashSet<>();
    public static final Map<Integer, String> leaderBoard = new LinkedHashMap<>();

    public static double goalSpawnX = 0;
    public static double goalSpawnY = 0;
    public static double goalSpawnZ = 0;
    public static float goalSpawnYaw = 0;
    public static float goalSpawnPitch = 0;
    public static String goalSpawnDim = "minecraft:overworld";
    public static double goalSpawnRadius = 3.0;
    public static boolean goalSpawnSet = false;

    public static void reset() {
        arrivalCount = 0;
        finishedEntities.clear();
        leaderBoard.clear();
    }

    public static void setSpawn(double x, double y, double z, float yaw, float pitch, String dim, double radius) {
        goalSpawnX = x;
        goalSpawnY = y;
        goalSpawnZ = z;
        goalSpawnYaw = yaw;
        goalSpawnPitch = pitch;
        goalSpawnDim = dim;
        goalSpawnRadius = radius;
        goalSpawnSet = true;
    }

    public static void handleGoalReach(ServerPlayer player, Level level, String sourceName) {
        if (level.isClientSide()) return;

        if (isTeamMode) {
            String teamKey = player.getScoreboardName();
            String teamDisplayName = player.getScoreboardName();
            List<ServerPlayer> teamPlayers = new ArrayList<>();
            teamPlayers.add(player);

            try {
                if (FTBTeamsAPI.api().isManagerLoaded()) {
                    var teamManager = FTBTeamsAPI.api().getManager();
                    Team team = teamManager.getTeamForPlayerID(player.getUUID()).orElse(null);
                    if (team != null) {
                        teamKey = team.getId().toString();
                        teamDisplayName = team.getName().getString();
                        teamPlayers.clear();
                        for (UUID memberUUID : team.getMembers()) {
                            ServerPlayer mp = player.server.getPlayerList().getPlayer(memberUUID);
                            if (mp != null) {
                                teamPlayers.add(mp);
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}

            if (finishedEntities.contains(teamKey)) {
                return;
            }

            finishedEntities.add(teamKey);
            arrivalCount++;
            leaderBoard.put(arrivalCount, teamDisplayName);

            final String finalTeamName = teamDisplayName;
            final int finalPos = arrivalCount;
            Component msg = Component.literal("🚩 §e[META] ¡El equipo §b" + finalTeamName + " §e(por §f" + player.getScoreboardName() + "§e) ha llegado en §a" + finalPos + "º Lugar§e!§r");
            player.server.getPlayerList().broadcastSystemMessage(msg, false);

            teleportTeamToGoalSpawn(player, teamPlayers);
        } else {
            String pKey = player.getUUID().toString();
            if (finishedEntities.contains(pKey)) {
                return;
            }

            finishedEntities.add(pKey);
            arrivalCount++;
            leaderBoard.put(arrivalCount, player.getScoreboardName());

            final int finalPos = arrivalCount;
            Component msg = Component.literal("🚩 §e[META] ¡El jugador §b" + player.getScoreboardName() + " §eha llegado en §a" + finalPos + "º Lugar§e!§r");
            player.server.getPlayerList().broadcastSystemMessage(msg, false);

            List<ServerPlayer> soloList = Collections.singletonList(player);
            teleportTeamToGoalSpawn(player, soloList);
        }
    }

    private static void teleportTeamToGoalSpawn(ServerPlayer triggeringPlayer, List<ServerPlayer> playersToTeleport) {
        if (!goalSpawnSet) {
            triggeringPlayer.displayClientMessage(Component.literal("⚠️ §cNo se ha configurado el spawn de llegada a la meta con /qqw meta setspawn."), false);
            return;
        }

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(goalSpawnDim));
        ServerLevel targetLevel = triggeringPlayer.server.getLevel(dimKey);
        if (targetLevel == null) {
            targetLevel = (ServerLevel) triggeringPlayer.level();
        }

        for (ServerPlayer p : playersToTeleport) {
            Vec3 safePos = ModEvents.findSafeGround(targetLevel, goalSpawnX, goalSpawnY, goalSpawnZ, goalSpawnRadius);
            p.stopRiding();
            p.stopSleeping();
            p.teleportTo(targetLevel, safePos.x, safePos.y, safePos.z, goalSpawnYaw, goalSpawnPitch);
            ModEvents.playPuffEffect(targetLevel, safePos.x, safePos.y, safePos.z);
        }
    }
}
