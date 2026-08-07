package net.mcreator.quequeworld.timer;

import net.mcreator.quequeworld.network.TimerSyncPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.mcreator.quequeworld.event.ModEvents;
import net.mcreator.quequeworld.world.QueQueWorldData;

public class TimerManager {
	public static int countdownTicks = 0;
	public static boolean countdownActive = false;

	public static String countdownSignal = null;

	public static int dayTimerTicks = 0;
	public static boolean dayTimerActive = false;
	public static boolean dayTimerPaused = false;

	public static void tick(MinecraftServer server) {
		boolean gamePaused = ModEvents.gamePaused;
		QueQueWorldData data = QueQueWorldData.get(server.overworld());
		boolean dirty = false;

		if (data.gamePaused != gamePaused) {
			data.gamePaused = gamePaused;
			dirty = true;
		}
		if (data.dayTimerPaused != dayTimerPaused) {
			data.dayTimerPaused = dayTimerPaused;
			dirty = true;
		}
		if (data.dayTimerActive != dayTimerActive) {
			data.dayTimerActive = dayTimerActive;
			dirty = true;
		}
		if (data.countdownActive != countdownActive) {
			data.countdownActive = countdownActive;
			dirty = true;
		}

		// 1. General Countdown
		if (countdownActive && !gamePaused) {
			countdownTicks--;

			// Beep sounds on the last 5 seconds (100, 80, 60, 40, 20 ticks remaining)
			if (countdownTicks > 0 && countdownTicks <= 100 && countdownTicks % 20 == 0) {
				float pitch = 1.0F + (float) (100 - countdownTicks) / 100.0F; // Pitch goes up from 1.0 to 1.8
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8F, pitch);
				}
			}

			if (countdownTicks <= 0) {
				countdownActive = false;
				countdownTicks = 0;
				data.countdownActive = false;
				data.countdownTicks = 0;
				dirty = true;
				// Play final bell sound
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2F, 1.0F);
					player.displayClientMessage(Component.literal("§c[Contador] ¡El tiempo ha terminado!"), false);
				}

				if (countdownSignal != null && !countdownSignal.trim().isEmpty()) {
					net.mcreator.quequeworld.signal.SignalChannelManager.emitSignal(server.overworld(), countdownSignal);
					countdownSignal = null;
				}

				syncToAll(server);
			} else if (countdownTicks % 20 == 0) {
				syncToAll(server);
				data.countdownTicks = countdownTicks;
				dirty = true;
			}
		}

		// 2. Global Day Timer
		if (dayTimerActive && !gamePaused && !dayTimerPaused) {
			dayTimerTicks--;

			if (dayTimerTicks <= 0) {
				dayTimerActive = false;
				dayTimerTicks = 0;
				data.dayTimerActive = false;
				data.dayTimerTicks = 0;
				dirty = true;

				// Broadcast end of day
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					player.displayClientMessage(Component.literal("§c[Día] ¡El tiempo límite de 2 horas ha terminado!"), false);
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 1.0F);
				}

				// Pause the game using the helper in ModEvents
				ModEvents.gamePaused = true;
				data.gamePaused = true;
				dirty = true;
				server.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_RANDOMTICKING).set(0, server);
				server.tickRateManager().setFrozen(true);

				syncToAll(server);
			} else if (dayTimerTicks % 20 == 0) {
				syncToAll(server);
				data.dayTimerTicks = dayTimerTicks;
				dirty = true;
			}
		}

		if (dirty) {
			data.setDirty();
		}
	}

	public static void syncToAll(MinecraftServer server) {
		TimerSyncPacket packet = new TimerSyncPacket(
				countdownActive,
				countdownTicks,
				dayTimerActive,
				dayTimerPaused || ModEvents.gamePaused,
				dayTimerTicks
		);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			PacketDistributor.sendToPlayer(player, packet);
		}
	}
}
