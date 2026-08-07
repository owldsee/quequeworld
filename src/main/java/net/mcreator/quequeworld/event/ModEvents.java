package net.mcreator.quequeworld.event;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.item.ModItems;
import net.mcreator.quequeworld.world.QueQueWorldData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.mcreator.quequeworld.item.BaseRingItem;
import net.mcreator.quequeworld.item.DiamondRingItem;
import net.mcreator.quequeworld.item.GoldRingItem;
import net.mcreator.quequeworld.item.EmeraldRingItem;
import net.mcreator.quequeworld.item.CopperRingItem;
import net.mcreator.quequeworld.item.LapisRingItem;
import net.mcreator.quequeworld.item.AmethystRingItem;
import net.mcreator.quequeworld.item.NetheriteRingItem;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.mcreator.quequeworld.network.SoulShieldSyncPacket;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.tags.DamageTypeTags;
import net.mcreator.quequeworld.config.QueQueDifficultyConfig;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.suggestion.SuggestionProvider;

@EventBusSubscriber(modid = QuequeworldMod.MODID)
public class ModEvents {

	public static boolean gamePaused = false;

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_FTB_TEAMS = (context, builder) -> {
		try {
			if (dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().isManagerLoaded()) {
				var teamManager = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager();
				for (dev.ftb.mods.ftbteams.api.Team t : teamManager.getTeams()) {
					String shortName = t.getShortName();
					String name = t.getName().getString();
					builder.suggest(shortName);
					if (!name.equalsIgnoreCase(shortName)) {
						builder.suggest(name);
					}
				}
			}
		} catch (Throwable ignored) {}
		return builder.buildFuture();
	};

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		Level level = player.level();

		if (level.isClientSide()) {
			return;
		}

		CompoundTag data = player.getPersistentData();

		// 1.3 LOGICA DE SINCRO DE ESCUDO Y AMENAZA
		boolean hasShield = player.getTags().contains("tiene_escudo");
		boolean isThreatened = player.getTags().contains("amenazado");
		boolean isGhost = player.getTags().contains("fantasma");
		int banderines = data.getInt("qqw_banderines");
		int deudas = data.getInt("qqw_deuda_banderines");
		double dangerLevel = 0.50D;
		int minBanderines = 0;
		if (player instanceof ServerPlayer serverPlayer) {
			dangerLevel = net.mcreator.quequeworld.event.SoulShieldEventHandler.getDangerLevel(serverPlayer);
			minBanderines = net.mcreator.quequeworld.world.QueQueWorldData.get(serverPlayer.serverLevel()).minBanderines;
		}
		
		boolean prevShield = data.getBoolean("qqw_sync_shield");
		boolean prevThreat = data.getBoolean("qqw_sync_threatened");
		boolean prevGhost = data.getBoolean("qqw_sync_ghost");
		int prevBanderines = data.getInt("qqw_sync_banderines");
		int prevDeudas = data.getInt("qqw_sync_deudas");
		double prevDanger = data.getDouble("qqw_sync_danger");
		int prevMinBanderines = data.getInt("qqw_sync_min_banderines");
		boolean forceSync = data.getBoolean("qqw_sync_force");
		
		if (prevShield != hasShield || prevThreat != isThreatened || prevGhost != isGhost || prevBanderines != banderines || prevDeudas != deudas || Math.abs(prevDanger - dangerLevel) > 0.0001D || prevMinBanderines != minBanderines || forceSync) {
			data.putBoolean("qqw_sync_shield", hasShield);
			data.putBoolean("qqw_sync_threatened", isThreatened);
			data.putBoolean("qqw_sync_ghost", isGhost);
			data.putInt("qqw_sync_banderines", banderines);
			data.putInt("qqw_sync_deudas", deudas);
			data.putDouble("qqw_sync_danger", dangerLevel);
			data.putInt("qqw_sync_min_banderines", minBanderines);
			data.putBoolean("qqw_sync_force", false);
			
			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new SoulShieldSyncPacket(hasShield, isThreatened, isGhost, banderines, deudas, dangerLevel, minBanderines));
			}
		}


		// 1. LÓGICA DE PAUSA (CONGELAMIENTO DE JUGADOR)
		if (gamePaused) {
			if (!player.getTags().contains("dios")) {
				if (!data.contains("paused_x")) {
					data.putDouble("paused_x", player.getX());
					data.putDouble("paused_y", player.getY());
					data.putDouble("paused_z", player.getZ());
					data.putFloat("paused_yaw", player.getYRot());
					data.putFloat("paused_pitch", player.getXRot());
				} else {
					if (player instanceof ServerPlayer serverPlayer) {
						serverPlayer.teleportTo(serverPlayer.serverLevel(), data.getDouble("paused_x"), data.getDouble("paused_y"), data.getDouble("paused_z"), player.getYRot(), player.getXRot());
						player.setDeltaMovement(0, 0, 0);
						player.hurtMarked = true;
					}
				}
				// Si está pausado, no permitir avanzar en otras canalizaciones
				return;
			}
		}

		// 1.2 LÓGICA DE CONGELAMIENTO DE CÁMARA (CINEMÁTICA)
		if (player.getTags().contains("qqw_spectating_dios")) {
			if (!data.contains("cine_x")) {
				data.putDouble("cine_x", player.getX());
				data.putDouble("cine_y", player.getY());
				data.putDouble("cine_z", player.getZ());
				data.putFloat("cine_yaw", player.getYRot());
				data.putFloat("cine_pitch", player.getXRot());
			} else {
				if (player instanceof ServerPlayer serverPlayer) {
					serverPlayer.teleportTo(serverPlayer.serverLevel(), data.getDouble("cine_x"), data.getDouble("cine_y"), data.getDouble("cine_z"), data.getFloat("cine_yaw"), data.getFloat("cine_pitch"));
					player.setDeltaMovement(0, 0, 0);
					player.hurtMarked = true;
				}
			}
		} else {
			if (data.contains("cine_x")) {
				data.remove("cine_x");
				data.remove("cine_y");
				data.remove("cine_z");
				data.remove("cine_yaw");
				data.remove("cine_pitch");
			}
		}

		// 1.5 LÓGICA DE MODO AVENTURA EN LA PLAZA (ZONA SEGURA)
		if (player instanceof ServerPlayer serverPlayer) {
			QueQueDifficultyConfig.SafeZone sz = QueQueDifficultyConfig.instance.safe_zone;
			if (sz != null && sz.enabled) {
				boolean isOverworld = serverPlayer.level().dimension() == Level.OVERWORLD;
				double dx = serverPlayer.getX() - sz.x;
				double dz = serverPlayer.getZ() - sz.z;
				double distSq = dx * dx + dz * dz;
				double radiusSq = sz.radius_blocks * sz.radius_blocks;

				boolean isInside = isOverworld && (distSq <= radiusSq);
				boolean hasSwitchedTag = serverPlayer.getTags().contains("qqw_switched_to_adventure");
				boolean isBypass = serverPlayer.getTags().contains("dios") || serverPlayer.hasPermissions(2);

				if (isInside) {
					if (!hasSwitchedTag && !isBypass) {
						if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL) {
							serverPlayer.setGameMode(GameType.ADVENTURE);
							serverPlayer.addTag("qqw_switched_to_adventure");
							serverPlayer.displayClientMessage(Component.literal("§eHas entrado al área del Castillo"), true);
						}
					}
				} else {
					if (hasSwitchedTag) {
						serverPlayer.setGameMode(GameType.SURVIVAL);
						serverPlayer.removeTag("qqw_switched_to_adventure");
						serverPlayer.displayClientMessage(Component.literal("§aHas salido del área del Castillo"), true);
					}
				}
			}
		}

		// 2. LÓGICA DEL ESPEJO DE ENDER
		if (data.contains("warp_cast_timer")) {
			int timer = data.getInt("warp_cast_timer");
			if (timer > 0) {
				timer--;
				data.putInt("warp_cast_timer", timer);

				// Partículas de portal
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
				}

				// Precargar el chunk de destino en cada tick
				if (player.getServer() != null) {
					try {
						double targetX;
						double targetZ;
						String targetDimStr;
						if (data.getBoolean("mirror_has_return")) {
							targetX = data.getDouble("mirror_return_x");
							targetZ = data.getDouble("mirror_return_z");
							targetDimStr = data.getString("mirror_return_dim");
						} else {
							QueQueWorldData worldData = QueQueWorldData.get((ServerLevel) level);
							targetX = worldData.spawnX;
							targetZ = worldData.spawnZ;
							targetDimStr = worldData.spawnDim;
						}
						ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(targetDimStr));
						ServerLevel targetLevel = player.getServer().getLevel(targetDim);
						if (targetLevel != null) {
							net.minecraft.world.level.ChunkPos chunkPos = new net.minecraft.world.level.ChunkPos((int) targetX >> 4, (int) targetZ >> 4);
							targetLevel.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.PORTAL, chunkPos, 3, BlockPos.containing(targetX, player.getY(), targetZ));
						}
					} catch (Exception e) {
						// Ignorar
					}
				}

				if (timer == 0) {
					// Finalizar casteo
					data.remove("warp_cast_timer");
					data.remove("warp_cast_health");

					// Remover lentitud extrema
					player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

					// Verificar si el jugador tiene el espejo de Ender en mano (principal o secundaria)
					ItemStack mainHand = player.getMainHandItem();
					ItemStack offHand = player.getOffhandItem();
					ItemStack mirrorStack = ItemStack.EMPTY;
					if (mainHand.is(ModItems.ENDER_MIRROR.get())) {
						mirrorStack = mainHand;
					} else if (offHand.is(ModItems.ENDER_MIRROR.get())) {
						mirrorStack = offHand;
					}

					if (!mirrorStack.isEmpty()) {
						if (player instanceof ServerPlayer serverPlayer) {
							performEnderMirrorTeleport(serverPlayer, mirrorStack);
						}
					}
				}
			}
		}

		// 3. LÓGICA DE LA PERLA DE QUEQUE (8 TICKS DE CANALIZACIÓN)
		if (data.contains("pearl_teleport_timer")) {
			int timer = data.getInt("pearl_teleport_timer");
			if (timer > 0) {
				// Verificar si sigue teniendo una perla de queque en la mano
				ItemStack mainHand = player.getMainHandItem();
				ItemStack offHand = player.getOffhandItem();
				boolean holdingPearl = mainHand.is(ModItems.QUEQUE_PEARL.get()) || offHand.is(ModItems.QUEQUE_PEARL.get());
				if (!holdingPearl) {
					data.remove("pearl_teleport_timer");
					data.remove("pearl_teleport_x");
					data.remove("pearl_teleport_y");
					data.remove("pearl_teleport_z");
					data.remove("pearl_teleport_yaw");
					data.remove("pearl_teleport_pitch");
					data.remove("pearl_teleport_dim");
					player.displayClientMessage(Component.literal("§cTeletransportación cancelada."), true);
					return;
				}

				timer--;
				data.putInt("pearl_teleport_timer", timer);

				double x = data.getDouble("pearl_teleport_x");
				double z = data.getDouble("pearl_teleport_z");
				String dimStr = data.getString("pearl_teleport_dim");

				// Precargar el chunk de destino en cada tick
				if (player.getServer() != null) {
					try {
						ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
						ServerLevel targetLevel = player.getServer().getLevel(targetDim);
						if (targetLevel != null) {
							net.minecraft.world.level.ChunkPos chunkPos = new net.minecraft.world.level.ChunkPos((int) x >> 4, (int) z >> 4);
							targetLevel.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.PORTAL, chunkPos, 3, BlockPos.containing(x, player.getY(), z));
						}
					} catch (Exception e) {
						// Ignorar
					}
				}

				// Partículas mágicas en cada tick
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.INSTANT_EFFECT, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
				}

				if (timer == 0) {
					data.remove("pearl_teleport_timer");
					double targetX = data.getDouble("pearl_teleport_x");
					double targetY = data.getDouble("pearl_teleport_y");
					double targetZ = data.getDouble("pearl_teleport_z");
					float targetYaw = data.getFloat("pearl_teleport_yaw");
					float targetPitch = data.getFloat("pearl_teleport_pitch");
					String targetDimStr = data.getString("pearl_teleport_dim");

					data.remove("pearl_teleport_x");
					data.remove("pearl_teleport_y");
					data.remove("pearl_teleport_z");
					data.remove("pearl_teleport_yaw");
					data.remove("pearl_teleport_pitch");
					data.remove("pearl_teleport_dim");

					if (player instanceof ServerPlayer serverPlayer) {
						try {
							ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(targetDimStr));
							ServerLevel targetLevel = serverPlayer.server.getLevel(targetDim);
							if (targetLevel != null) {
								BlockPos basePos = BlockPos.containing(targetX, targetY, targetZ);
								BlockPos safePos = findSafePosition(targetLevel, basePos);

								if (safePos == null) {
									// Destino obstruido: cancelar teletransporte sin consumir perla
									level.playSound(null, player.getX(), player.getY(), player.getZ(),
										SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
									player.displayClientMessage(Component.literal("§c¡Destino obstruido! Teletransporte cancelado por seguridad."), true);
									return;
								}

								double finalX = safePos.getX() + 0.5;
								double finalY = safePos.getY();
								double finalZ = safePos.getZ() + 0.5;

								// Consumir una perla de queque de la mano del jugador
								if (mainHand.is(ModItems.QUEQUE_PEARL.get())) {
									mainHand.shrink(1);
								} else if (offHand.is(ModItems.QUEQUE_PEARL.get())) {
									offHand.shrink(1);
								}

								// Sonido de teletransporte antes
								level.playSound(null, player.getX(), player.getY(), player.getZ(),
									SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
								if (level instanceof ServerLevel sl) {
									sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
								}

								// Teletransportar
								serverPlayer.teleportTo(targetLevel, finalX, finalY, finalZ, targetYaw, targetPitch);

								// Sonido de teletransporte después
								targetLevel.playSound(null, finalX, finalY, finalZ,
									SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
								targetLevel.sendParticles(ParticleTypes.PORTAL, finalX, finalY + 1.0, finalZ, 30, 0.5, 0.5, 0.5, 0.1);
							}
						} catch (Exception e) {
							player.displayClientMessage(Component.literal("§cError al teletransportar."), true);
						}
					}
				}
			}
		}
	}

	private static void performEnderMirrorTeleport(ServerPlayer player, ItemStack mirrorStack) {
		CompoundTag data = player.getPersistentData();
		ServerLevel level = player.serverLevel();
		
		if (data.getBoolean("mirror_has_return")) {
			// RETORNO A LA BASE
			double x = data.getDouble("mirror_return_x");
			double y = data.getDouble("mirror_return_y");
			double z = data.getDouble("mirror_return_z");
			float yaw = data.getFloat("mirror_return_yaw");
			float pitch = data.getFloat("mirror_return_pitch");
			String dimStr = data.getString("mirror_return_dim");
			
			// Remover marcador de retorno
			data.remove("mirror_has_return");
			data.remove("mirror_return_x");
			data.remove("mirror_return_y");
			data.remove("mirror_return_z");
			data.remove("mirror_return_yaw");
			data.remove("mirror_return_pitch");
			data.remove("mirror_return_dim");
			
			try {
				ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
				ServerLevel targetLevel = player.server.getLevel(targetDim);
				if (targetLevel != null) {
					// Sonidos y partículas antes de irse
					level.playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
					level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
					
					// Teletransportar
					player.teleportTo(targetLevel, x, y, z, yaw, pitch);
					
					// Sonidos y partículas al llegar
					targetLevel.playSound(null, x, y, z,
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
					targetLevel.playSound(null, x, y, z,
						SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
					targetLevel.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 50, 0.5, 0.5, 0.5, 0.1);
					
					// Aplicar debilidad por 15 segundos (300 ticks)
					player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0, false, false, false));
					
					// Consumir el espejo
					mirrorStack.shrink(1);
				}
			} catch (Exception e) {
				player.displayClientMessage(Component.literal("§cError al regresar a la base."), true);
			}
		} else {
			// IR A LA PLAZA
			// Guardar posición de retorno actual
			data.putBoolean("mirror_has_return", true);
			data.putDouble("mirror_return_x", player.getX());
			data.putDouble("mirror_return_y", player.getY());
			data.putDouble("mirror_return_z", player.getZ());
			data.putFloat("mirror_return_yaw", player.getYRot());
			data.putFloat("mirror_return_pitch", player.getXRot());
			data.putString("mirror_return_dim", level.dimension().location().toString());
			
			// Obtener coords de Plaza
			net.mcreator.quequeworld.world.QueQueWorldData worldData = net.mcreator.quequeworld.world.QueQueWorldData.get(level);
			double x = worldData.spawnX;
			double y = worldData.spawnY;
			double z = worldData.spawnZ;
			float yaw = worldData.spawnYaw;
			float pitch = worldData.spawnPitch;
			String dimStr = worldData.spawnDim;
			
			try {
				ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
				ServerLevel targetLevel = player.server.getLevel(targetDim);
				if (targetLevel != null) {
					// Sonidos y partículas antes de irse
					level.playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
					level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
					
					// Teletransportar
					player.teleportTo(targetLevel, x, y, z, yaw, pitch);
					
					// Sonidos y partículas al llegar
					targetLevel.playSound(null, x, y, z,
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
					targetLevel.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 30, 0.5, 0.5, 0.5, 0.1);
				}
			} catch (Exception e) {
				player.displayClientMessage(Component.literal("§cError al viajar a la plaza."), true);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide()) {
			player.getPersistentData().putBoolean("qqw_sync_force", true);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new net.mcreator.quequeworld.network.TimerSyncPacket(
					net.mcreator.quequeworld.timer.TimerManager.countdownActive,
					net.mcreator.quequeworld.timer.TimerManager.countdownTicks,
					net.mcreator.quequeworld.timer.TimerManager.dayTimerActive,
					net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused || gamePaused,
					net.mcreator.quequeworld.timer.TimerManager.dayTimerTicks
				));
			}
		}
	}

	@SubscribeEvent
	public static void onEntityTickPre(EntityTickEvent.Pre event) {
		Entity entity = event.getEntity();
		if (entity.level().isClientSide()) {
			return;
		}

		// 1. PREVENCIÓN DE MOBS HOSTILES EN EL CASTILLO (Zona Segura Dinámica)
		QueQueDifficultyConfig.SafeZone safeZone = QueQueDifficultyConfig.instance.safe_zone;
		if (safeZone != null && safeZone.enabled) {
			if (entity instanceof net.minecraft.world.entity.monster.Enemy || entity instanceof net.minecraft.world.entity.monster.Monster) {
				boolean isEasyNpc = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace().equals("easy_npc");
				if (!isEasyNpc && entity.level().dimension() == Level.OVERWORLD) {
					double dx = entity.getX() - safeZone.x;
					double dz = entity.getZ() - safeZone.z;
					double r = safeZone.radius_blocks;
					if ((dx * dx + dz * dz) <= (r * r)) {
						entity.discard();
						return;
					}
				}
			}
		}

		// 2. MECÁNICA ESPECIAL: CREEPERS INSTANT EXPLODE
		QueQueDifficultyConfig.Mechanics mech = QueQueDifficultyConfig.instance.mechanics;
		if (mech != null && mech.creepers_instant_explode) {
			if (entity instanceof net.minecraft.world.entity.monster.Creeper creeper) {
				LivingEntity target = creeper.getTarget();
				if (target instanceof Player) {
					if (creeper.distanceToSqr(target) <= 9.0) { // 3 * 3 = 9
						float power = creeper.isPowered() ? 6.0f : 3.0f;
						creeper.level().explode(creeper, creeper.getX(), creeper.getY(), creeper.getZ(), power, Level.ExplosionInteraction.MOB);
						creeper.discard();
						return;
					}
				}
			}
		}

		// 3. LÓGICA DE PAUSA (CONGELAMIENTO)
		if (gamePaused) {
			if (!(entity instanceof Player)) {
				entity.setDeltaMovement(0, 0, 0);
				entity.setPos(entity.xo, entity.yo, entity.zo);
				entity.hurtMarked = true;
			}
		}
	}

	@SubscribeEvent
	public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
		LivingEntity target = event.getEntity();
		Level level = target.level();

		if (level.isClientSide()) {
			return;
		}

		// Inmunidad al fuego para monstruos (Configurable)
		if (target instanceof net.minecraft.world.entity.monster.Enemy || target instanceof net.minecraft.world.entity.monster.Monster) {
			QueQueDifficultyConfig.Mechanics mech = QueQueDifficultyConfig.instance.mechanics;
			if (mech != null && mech.mobs_immune_to_fire) {
				if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
					event.setCanceled(true);
					target.clearFire();
					return;
				}
			}
		}

		// 1. Detección de daño para interrumpir el casteo del Espejo de Ender
		if (target instanceof Player player) {
			CompoundTag data = player.getPersistentData();
			if (data.contains("warp_cast_timer")) {
				// Cancelar canalización
				data.remove("warp_cast_timer");
				data.remove("warp_cast_health");
				player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

				// Sonido e indicador visual de cancelación
				level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
				}
			}
		}

		// Lógica de Anillos de Curios
		if (target instanceof ServerPlayer player) {
			// A. Desgaste por combate en anillos de Oro, Cobre y Diamante
			ItemStack goldRing = getEquippedCurio(player, GoldRingItem.class);
			if (!goldRing.isEmpty()) {
				int dmg = Math.max(1, Math.round(event.getAmount()));
				goldRing.hurtAndBreak(dmg, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
			}
			ItemStack copperRing = getEquippedCurio(player, CopperRingItem.class);
			if (!copperRing.isEmpty()) {
				copperRing.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
			}
			ItemStack diamondRing = getEquippedCurio(player, DiamondRingItem.class);
			if (!diamondRing.isEmpty()) {
				diamondRing.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
			}

			// B. Mitigación de Fuego y Lava (Netherite Ring)
			if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
				ItemStack netheriteRing = getEquippedCurio(player, NetheriteRingItem.class);
				if (!netheriteRing.isEmpty()) {
					event.setCanceled(true);
					event.setAmount(0);
					return;
				}
			}

			// C. Mitigación de Proyectiles (Amethyst Ring)
			if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
				ItemStack amethystRing = getEquippedCurio(player, AmethystRingItem.class);
				if (!amethystRing.isEmpty()) {
					int tier = ((AmethystRingItem) amethystRing.getItem()).getTier();
					if (tier >= 2) {
						float reduction = (tier == 2 || tier == 3) ? 2.0F : 4.0F;
						event.setAmount(Math.max(0.0F, event.getAmount() - reduction));
					}
				}
			}

			// D. Mitigación de Explosiones (Netherite Ring)
			if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
				ItemStack netheriteRing = getEquippedCurio(player, NetheriteRingItem.class);
				if (!netheriteRing.isEmpty()) {
					int tier = ((NetheriteRingItem) netheriteRing.getItem()).getTier();
					float reduction = (tier == 1 || tier == 2) ? 4.0F : (tier == 3 || tier == 4) ? 8.0F : 16.0F;
					float originalAmount = event.getAmount();
					float newAmount = Math.max(0.0F, originalAmount - reduction);
					float mitigated = originalAmount - newAmount;
					event.setAmount(newAmount);
					if (mitigated > 0) {
						int durDamage = Math.round(mitigated * 20.0F);
						netheriteRing.hurtAndBreak(durDamage, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("qqw")
			// 1. reload
			.then(Commands.literal("reload")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					QueQueDifficultyConfig.load();
					context.getSource().sendSuccess(() -> Component.literal("§a[QueQueWorld] Configuración de dificultad recargada con éxito."), true);
					return 1;
				})
			)
			// Música custom (/qqw music [cancion] | stop)
			.then(Commands.literal("music")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("stop")
					.executes(context -> {
						PacketDistributor.sendToAllPlayers(new net.mcreator.quequeworld.network.MusicCommandPacket("stop", ""));
						context.getSource().sendSuccess(() -> Component.literal("§e[QQW] Música detenida para todos los jugadores (Fade Out 0.5s)."), true);
						return 1;
					})
				)
				.then(Commands.literal("play")
					.then(Commands.argument("cancion", com.mojang.brigadier.arguments.StringArgumentType.string())
						.executes(context -> {
							String song = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "cancion");
							PacketDistributor.sendToAllPlayers(new net.mcreator.quequeworld.network.MusicCommandPacket("play", song));
							context.getSource().sendSuccess(() -> Component.literal("§a[QQW] Reproduciendo música §e" + song + "§a (Fade In 0.5s)."), true);
							return 1;
						})
					)
				)
				.then(Commands.argument("cancion", com.mojang.brigadier.arguments.StringArgumentType.string())
					.executes(context -> {
						String song = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "cancion");
						PacketDistributor.sendToAllPlayers(new net.mcreator.quequeworld.network.MusicCommandPacket("play", song));
						context.getSource().sendSuccess(() -> Component.literal("§a[QQW] Reproduciendo música §e" + song + "§a (Fade In 0.5s)."), true);
						return 1;
					})
				)
			)
			
			// Comandos de desafío
			.then(Commands.literal("spawndesafio")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					ServerPlayer p = context.getSource().getPlayerOrException();
					net.mcreator.quequeworld.world.QueQueWorldData data = net.mcreator.quequeworld.world.QueQueWorldData.get((ServerLevel) p.level());
					data.desafioSpawnX = p.getX();
					data.desafioSpawnY = p.getY();
					data.desafioSpawnZ = p.getZ();
					data.desafioSpawnYaw = p.getYRot();
					data.desafioSpawnPitch = p.getXRot();
					data.desafioSpawnDim = p.level().dimension().location().toString();
					data.desafioSpawnSet = true;
					data.setDirty();
					context.getSource().sendSuccess(() -> Component.literal("§aSpawn de desafío configurado en tu posición actual."), true);
					return 1;
				})
				.then(Commands.argument("pos", net.minecraft.commands.arguments.coordinates.Vec3Argument.vec3())
					.executes(context -> {
						net.minecraft.world.phys.Vec3 pos = net.minecraft.commands.arguments.coordinates.Vec3Argument.getVec3(context, "pos");
						ServerPlayer p = context.getSource().getPlayerOrException();
						net.mcreator.quequeworld.world.QueQueWorldData data = net.mcreator.quequeworld.world.QueQueWorldData.get((ServerLevel) p.level());
						data.desafioSpawnX = pos.x;
						data.desafioSpawnY = pos.y;
						data.desafioSpawnZ = pos.z;
						data.desafioSpawnYaw = p.getYRot();
						data.desafioSpawnPitch = p.getXRot();
						data.desafioSpawnDim = p.level().dimension().location().toString();
						data.desafioSpawnSet = true;
						data.setDirty();
						context.getSource().sendSuccess(() -> Component.literal("§aSpawn de desafío configurado en " + pos.x + " " + pos.y + " " + pos.z), true);
						return 1;
					})
				)
			)
			.then(Commands.literal("mecanicamorir_a")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
					.executes(context -> {
						ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
						boolean state = !target.getTags().contains("qqw_mecanica_morir_disabled");
						context.getSource().sendSuccess(() -> Component.literal("§eMecánica de morir para " + target.getScoreboardName() + " está: " + (state ? "§aACTIVADA (Normal)" : "§cDESACTIVADA (Segura)")), false);
						return 1;
					})
					.then(Commands.argument("estado", com.mojang.brigadier.arguments.BoolArgumentType.bool())
						.executes(context -> {
							ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
							boolean val = com.mojang.brigadier.arguments.BoolArgumentType.getBool(context, "estado");
							if (val) {
								target.removeTag("qqw_mecanica_morir_disabled");
								context.getSource().sendSuccess(() -> Component.literal("§aMecánica de morir ACTIVADA para " + target.getScoreboardName() + "."), true);
							} else {
								target.addTag("qqw_mecanica_morir_disabled");
								context.getSource().sendSuccess(() -> Component.literal("§cMecánica de morir DESACTIVADA para " + target.getScoreboardName() + ". (Seguro)"), true);
							}
							return 1;
						})
					)
				)
			)
			.then(Commands.literal("mecanicamorir")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					net.minecraft.server.MinecraftServer server = context.getSource().getServer();
					context.getSource().sendSuccess(() -> Component.literal("§eEstado de Mecánica de Morir:"), false);
					for (ServerPlayer p : server.getPlayerList().getPlayers()) {
						if (p.getTags().contains("dios")) continue;
						boolean state = !p.getTags().contains("qqw_mecanica_morir_disabled");
						context.getSource().sendSuccess(() -> Component.literal(" - " + p.getScoreboardName() + ": " + (state ? "§aACTIVADA" : "§cDESACTIVADA")), false);
					}
					return 1;
				})
				.then(Commands.argument("estado", com.mojang.brigadier.arguments.BoolArgumentType.bool())
					.executes(context -> {
						net.minecraft.server.MinecraftServer server = context.getSource().getServer();
						boolean val = com.mojang.brigadier.arguments.BoolArgumentType.getBool(context, "estado");
						int count = 0;
						for (ServerPlayer p : server.getPlayerList().getPlayers()) {
							if (p.getTags().contains("dios")) continue;
							if (val) {
								p.removeTag("qqw_mecanica_morir_disabled");
							} else {
								p.addTag("qqw_mecanica_morir_disabled");
							}
							count++;
						}
						final int finalCount = count;
						context.getSource().sendSuccess(() -> Component.literal(val ? "§aMecánica de morir ACTIVADA para " + finalCount + " jugadores." : "§cMecánica de morir DESACTIVADA para " + finalCount + " jugadores."), true);
						return 1;
					})
				)
			)
			
			// 2. dios
			.then(Commands.literal("dios")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("value", BoolArgumentType.bool())
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						boolean val = BoolArgumentType.getBool(context, "value");
						if (val) {
							player.addTag("dios");
							context.getSource().sendSuccess(() -> Component.literal("Modo dios activado."), true);
						} else {
							player.removeTag("dios");
							context.getSource().sendSuccess(() -> Component.literal("Modo dios desactivado."), true);
						}
						return 1;
					})
				)
				.executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					if (player.getTags().contains("dios")) {
						player.removeTag("dios");
						context.getSource().sendSuccess(() -> Component.literal("Modo dios desactivado."), true);
					} else {
						player.addTag("dios");
						context.getSource().sendSuccess(() -> Component.literal("Modo dios activado."), true);
					}
					return 1;
				})
			)
			
			// 3. desvanecer
			.then(Commands.literal("desvanecer")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					ServerLevel serverLevel = player.serverLevel();
					net.minecraft.world.scores.Scoreboard scoreboard = serverLevel.getScoreboard();
					
					if (player.hasEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY)) {
						player.removeEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY);
						net.minecraft.world.scores.PlayerTeam team = scoreboard.getPlayerTeam("dios_oculto");
						if (team != null) {
							scoreboard.removePlayerFromTeam(player.getScoreboardName(), team);
						}
						serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 2.0F, 1.0F);
						serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.5F, 1.2F);
						serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
						serverLevel.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1.0, player.getZ(), 100, 0.5, 1.0, 0.5, 0.05);
						context.getSource().sendSuccess(() -> Component.literal("§a¡Te has revelado con gloria!"), true);
					} else {
						player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.INVISIBILITY, MobEffectInstance.INFINITE_DURATION, 0, false, false, false));
						net.minecraft.world.scores.PlayerTeam team = scoreboard.getPlayerTeam("dios_oculto");
						if (team == null) {
							team = scoreboard.addPlayerTeam("dios_oculto");
							team.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.NEVER);
						}
						scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
						serverLevel.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.3, 0.5, 0.3, 0.05);
						serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.5F, 1.2F);
						context.getSource().sendSuccess(() -> Component.literal("§eTe has ocultado de los mortales."), true);
					}
					return 1;
				})
			)
			
			// 4. pausa
			.then(Commands.literal("pausa")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					net.minecraft.server.MinecraftServer server = context.getSource().getServer();
					if (gamePaused) {
						unpauseGame(server);
						context.getSource().sendSuccess(() -> Component.literal("Juego despausado."), true);
					} else {
						gamePaused = true;
						server.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_RANDOMTICKING).set(0, server);
						server.tickRateManager().setFrozen(true);
						context.getSource().sendSuccess(() -> Component.literal("Juego pausado."), true);
					}
					net.mcreator.quequeworld.timer.TimerManager.syncToAll(server);
					return 1;
				})
			)
			
			// 4.5. emitir
			.then(Commands.literal("emitir")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.argument("canal_senal", com.mojang.brigadier.arguments.StringArgumentType.string())
					.executes(context -> {
						String cs = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "canal_senal");
						net.mcreator.quequeworld.signal.SignalChannelManager.emitSignal(context.getSource().getLevel(), cs);
						context.getSource().sendSuccess(() -> Component.literal("§a[QQW] Señal emitida: §e" + cs), true);
						return 1;
					})
				)
			)
			
			// 5. contar
			.then(Commands.literal("contar")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.argument("segundos", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
					.executes(context -> {
						int segundos = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "segundos");
						net.minecraft.server.MinecraftServer server = context.getSource().getServer();
						if (segundos <= 0) {
							net.mcreator.quequeworld.timer.TimerManager.countdownActive = false;
							net.mcreator.quequeworld.timer.TimerManager.countdownTicks = 0;
							net.mcreator.quequeworld.timer.TimerManager.countdownSignal = null;
							context.getSource().sendSuccess(() -> Component.literal("§cContador detenido."), true);
						} else {
							net.mcreator.quequeworld.timer.TimerManager.countdownTicks = segundos * 20;
							net.mcreator.quequeworld.timer.TimerManager.countdownActive = true;
							net.mcreator.quequeworld.timer.TimerManager.countdownSignal = null;
							context.getSource().sendSuccess(() -> Component.literal("§aContador iniciado: " + segundos + " segundos."), true);
						}
						net.mcreator.quequeworld.timer.TimerManager.syncToAll(server);
						return 1;
					})
					.then(Commands.argument("canal_senal", com.mojang.brigadier.arguments.StringArgumentType.string())
						.executes(context -> {
							int segundos = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "segundos");
							String cs = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "canal_senal");
							net.minecraft.server.MinecraftServer server = context.getSource().getServer();
							if (segundos <= 0) {
								net.mcreator.quequeworld.timer.TimerManager.countdownActive = false;
								net.mcreator.quequeworld.timer.TimerManager.countdownTicks = 0;
								net.mcreator.quequeworld.timer.TimerManager.countdownSignal = null;
								context.getSource().sendSuccess(() -> Component.literal("§cContador detenido."), true);
							} else {
								net.mcreator.quequeworld.timer.TimerManager.countdownTicks = segundos * 20;
								net.mcreator.quequeworld.timer.TimerManager.countdownActive = true;
								net.mcreator.quequeworld.timer.TimerManager.countdownSignal = cs;
								context.getSource().sendSuccess(() -> Component.literal("§aContador iniciado: " + segundos + " segundos. Emitirá: §e" + cs), true);
							}
							net.mcreator.quequeworld.timer.TimerManager.syncToAll(server);
							return 1;
						})
					)
				)
				.executes(context -> {
					net.mcreator.quequeworld.timer.TimerManager.countdownActive = false;
					net.mcreator.quequeworld.timer.TimerManager.countdownTicks = 0;
					net.mcreator.quequeworld.timer.TimerManager.countdownSignal = null;
					context.getSource().sendSuccess(() -> Component.literal("§cContador detenido."), true);
					net.mcreator.quequeworld.timer.TimerManager.syncToAll(context.getSource().getServer());
					return 1;
				})
			)
			
			// 6. empezardia
			.then(Commands.literal("empezardia")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.executes(context -> startDay(context, 120))
				.then(Commands.argument("minutos", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
					.executes(context -> {
						int minutos = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "minutos");
						return startDay(context, minutos);
					})
				)
			)
			
			// 7. extratiempo
			.then(Commands.literal("extratiempo")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.argument("minutos", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
					.executes(context -> {
						int minutos = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "minutos");
						if (net.mcreator.quequeworld.timer.TimerManager.dayTimerActive) {
							net.mcreator.quequeworld.timer.TimerManager.dayTimerTicks += minutos * 60 * 20;
							context.getSource().sendSuccess(() -> Component.literal("§aAgregados " + minutos + " minutos extra al tiempo del día."), true);
							net.mcreator.quequeworld.timer.TimerManager.syncToAll(context.getSource().getServer());
						} else {
							context.getSource().sendFailure(Component.literal("§cEl reloj del día no está activo. Usa /empezardia primero."));
						}
						return 1;
					})
				)
			)
			
			// 8. findia
			.then(Commands.literal("findia")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.executes(context -> {
					net.mcreator.quequeworld.timer.TimerManager.dayTimerActive = false;
					net.mcreator.quequeworld.timer.TimerManager.dayTimerTicks = 0;
					context.getSource().sendSuccess(() -> Component.literal("§cTiempo del día finalizado manualmente."), true);
					
					net.minecraft.server.MinecraftServer server = context.getSource().getServer();
					for (ServerPlayer player : server.getPlayerList().getPlayers()) {
						player.displayClientMessage(Component.literal("§c[Día] ¡El tiempo del desafío ha finalizado!"), false);
						player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 1.0F);
					}
					
					gamePaused = true;
					server.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_RANDOMTICKING).set(0, server);
					server.tickRateManager().setFrozen(true);
					
					net.mcreator.quequeworld.timer.TimerManager.syncToAll(server);
					return 1;
				})
			)
			
			// 9. confesar
			.then(Commands.literal("confesar")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.literal("confesado")
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						net.mcreator.quequeworld.world.QueQueWorldData worldData = net.mcreator.quequeworld.world.QueQueWorldData.get(player.serverLevel());
						worldData.confesadoX = player.getX();
						worldData.confesadoY = player.getY();
						worldData.confesadoZ = player.getZ();
						worldData.confesadoYaw = player.getYRot();
						worldData.confesadoPitch = player.getXRot();
						worldData.confesadoDim = player.level().dimension().location().toString();
						worldData.confesadoSet = true;
						worldData.setDirty();
						context.getSource().sendSuccess(() -> Component.literal("§aPosición del confesado establecida en tu ubicación actual."), true);
						return 1;
					})
				)
				.then(Commands.literal("camara")
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						net.mcreator.quequeworld.world.QueQueWorldData worldData = net.mcreator.quequeworld.world.QueQueWorldData.get(player.serverLevel());
						worldData.camaraX = player.getX();
						worldData.camaraY = player.getY();
						worldData.camaraZ = player.getZ();
						worldData.camaraYaw = player.getYRot();
						worldData.camaraPitch = player.getXRot();
						worldData.camaraDim = player.level().dimension().location().toString();
						worldData.camaraSet = true;
						worldData.setDirty();
						context.getSource().sendSuccess(() -> Component.literal("§aPosición de la cámara establecida en tu ubicación actual."), true);
						return 1;
					})
				)
				.then(Commands.literal("traer")
					.then(Commands.argument("objetivo", net.minecraft.commands.arguments.EntityArgument.player())
						.executes(context -> {
							ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "objetivo");
							ServerPlayer sender = context.getSource().getPlayerOrException();
							net.mcreator.quequeworld.world.QueQueWorldData worldData = net.mcreator.quequeworld.world.QueQueWorldData.get(sender.serverLevel());
							
							if (!worldData.confesadoSet || !worldData.camaraSet) {
								context.getSource().sendFailure(Component.literal("§cDeber registrar la posición de la cámara y del confesado antes de traer a alguien."));
								return 0;
							}
							
							CompoundTag tData = target.getPersistentData();
							tData.putDouble("confess_marked_x", target.getX());
							tData.putDouble("confess_marked_y", target.getY());
							tData.putDouble("confess_marked_z", target.getZ());
							tData.putFloat("confess_marked_yaw", target.getYRot());
							tData.putFloat("confess_marked_pitch", target.getXRot());
							tData.putString("confess_marked_dim", target.level().dimension().location().toString());
							tData.putString("confess_marked_gamemode", target.gameMode.getGameModeForPlayer().name());
							
							// Guardar y limpiar inventario para confesionario
							net.minecraft.nbt.ListTag invNbt = target.getInventory().save(new net.minecraft.nbt.ListTag());
							tData.put("qqw_confess_saved_inv", invNbt);

							if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
								top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
									net.minecraft.nbt.ListTag curiosList = new net.minecraft.nbt.ListTag();
									handler.getCurios().forEach((identifier, slotHandler) -> {
										net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
										for (int i = 0; i < stacks.getSlots(); i++) {
											net.minecraft.world.item.ItemStack stack = stacks.getStackInSlot(i);
											if (!stack.isEmpty()) {
												CompoundTag itemTag = new CompoundTag();
												itemTag.putString("Identifier", identifier);
												itemTag.putInt("Slot", i);
												net.minecraft.nbt.Tag serializedStack = stack.save(target.level().registryAccess());
												itemTag.put("Item", serializedStack);
												curiosList.add(itemTag);
											}
										}
									});
									tData.put("qqw_confess_saved_curios", curiosList);
								});
							}

							target.getInventory().clearContent();
							if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
								top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
									handler.getCurios().forEach((identifier, slotHandler) -> {
										net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
										for (int i = 0; i < stacks.getSlots(); i++) {
											stacks.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
										}
									});
								});
							}
							
							CompoundTag sData = sender.getPersistentData();
							sData.putDouble("dios_marked_x", sender.getX());
							sData.putDouble("dios_marked_y", sender.getY());
							sData.putDouble("dios_marked_z", sender.getZ());
							sData.putFloat("dios_marked_yaw", sender.getYRot());
							sData.putFloat("dios_marked_pitch", sender.getXRot());
							sData.putString("dios_marked_dim", sender.level().dimension().location().toString());
							
							playPuffEffect(target.serverLevel(), target.getX(), target.getY(), target.getZ());
							playPuffEffect(sender.serverLevel(), sender.getX(), sender.getY(), sender.getZ());
							
							ResourceKey<Level> targetDimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(worldData.confesadoDim));
							ServerLevel targetLevel = sender.server.getLevel(targetDimKey);
							if (targetLevel != null) {
								target.teleportTo(targetLevel, worldData.confesadoX, worldData.confesadoY, worldData.confesadoZ, worldData.confesadoYaw, worldData.confesadoPitch);
								playPuffEffect(targetLevel, worldData.confesadoX, worldData.confesadoY, worldData.confesadoZ);
							}
							
							ResourceKey<Level> camaraDimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(worldData.camaraDim));
							ServerLevel camaraLevel = sender.server.getLevel(camaraDimKey);
							if (camaraLevel != null) {
								sender.teleportTo(camaraLevel, worldData.camaraX, worldData.camaraY, worldData.camaraZ, worldData.camaraYaw, worldData.camaraPitch);
								playPuffEffect(camaraLevel, worldData.camaraX, worldData.camaraY, worldData.camaraZ);
							}
							
							target.setGameMode(GameType.ADVENTURE);
							context.getSource().sendSuccess(() -> Component.literal("§aJugador " + target.getScoreboardName() + " traído al confesionario."), true);
							return 1;
						})
					)
				)
				.then(Commands.literal("regresar")
					.executes(context -> {
						net.minecraft.server.MinecraftServer server = context.getSource().getServer();
						int count = 0;
						for (ServerPlayer player : server.getPlayerList().getPlayers()) {
							CompoundTag pData = player.getPersistentData();
							
							if (pData.contains("confess_marked_x")) {
								double x = pData.getDouble("confess_marked_x");
								double y = pData.getDouble("confess_marked_y");
								double z = pData.getDouble("confess_marked_z");
								float yaw = pData.getFloat("confess_marked_yaw");
								float pitch = pData.getFloat("confess_marked_pitch");
								String dimStr = pData.getString("confess_marked_dim");
								String gmStr = pData.getString("confess_marked_gamemode");
								
								playPuffEffect(player.serverLevel(), player.getX(), player.getY(), player.getZ());
								
								ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
								ServerLevel targetLevel = server.getLevel(dimKey);
								if (targetLevel != null) {
									player.teleportTo(targetLevel, x, y, z, yaw, pitch);
									playPuffEffect(targetLevel, x, y, z);
								}
								
								GameType gm = GameType.byName(gmStr, GameType.SURVIVAL);
								player.setGameMode(gm);
								
								// Restaurar inventarios
								if (pData.contains("qqw_confess_saved_inv")) {
									net.minecraft.nbt.ListTag invNbt = pData.getList("qqw_confess_saved_inv", net.minecraft.nbt.Tag.TAG_COMPOUND);
									player.getInventory().load(invNbt);
									pData.remove("qqw_confess_saved_inv");
								}

								if (net.neoforged.fml.ModList.get().isLoaded("curios") && pData.contains("qqw_confess_saved_curios")) {
									final net.minecraft.nbt.ListTag curiosList = pData.getList("qqw_confess_saved_curios", net.minecraft.nbt.Tag.TAG_COMPOUND);
									top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
										handler.getCurios().forEach((identifier, slotHandler) -> {
											net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
											for (int idx = 0; idx < stacks.getSlots(); idx++) {
												stacks.setStackInSlot(idx, net.minecraft.world.item.ItemStack.EMPTY);
											}
										});
										for (int j = 0; j < curiosList.size(); j++) {
											CompoundTag itemTag = curiosList.getCompound(j);
											String identifier = itemTag.getString("Identifier");
											int slot = itemTag.getInt("Slot");
											CompoundTag stackTag = itemTag.getCompound("Item");
											java.util.Optional<net.minecraft.world.item.ItemStack> parsedOpt = 
												net.minecraft.world.item.ItemStack.parse(player.level().registryAccess(), stackTag);
											if (parsedOpt.isPresent()) {
												net.minecraft.world.item.ItemStack stack = parsedOpt.get();
												var slotHandler = handler.getCurios().get(identifier);
												if (slotHandler != null) {
													net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
													if (slot >= 0 && slot < stacks.getSlots()) {
														stacks.setStackInSlot(slot, stack);
													}
												}
											}
										}
									});
									pData.remove("qqw_confess_saved_curios");
								}
								
								pData.remove("confess_marked_x");
								pData.remove("confess_marked_y");
								pData.remove("confess_marked_z");
								pData.remove("confess_marked_yaw");
								pData.remove("confess_marked_pitch");
								pData.remove("confess_marked_dim");
								pData.remove("confess_marked_gamemode");
								count++;
							}
							
							if (pData.contains("dios_marked_x")) {
								double x = pData.getDouble("dios_marked_x");
								double y = pData.getDouble("dios_marked_y");
								double z = pData.getDouble("dios_marked_z");
								float yaw = pData.getFloat("dios_marked_yaw");
								float pitch = pData.getFloat("dios_marked_pitch");
								String dimStr = pData.getString("dios_marked_dim");
								
								playPuffEffect(player.serverLevel(), player.getX(), player.getY(), player.getZ());
								
								ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
								ServerLevel targetLevel = server.getLevel(dimKey);
								if (targetLevel != null) {
									player.teleportTo(targetLevel, x, y, z, yaw, pitch);
									playPuffEffect(targetLevel, x, y, z);
								}
								
								pData.remove("dios_marked_x");
								pData.remove("dios_marked_y");
								pData.remove("dios_marked_z");
								pData.remove("dios_marked_yaw");
								pData.remove("dios_marked_pitch");
								pData.remove("dios_marked_dim");
							}
						}
						
						final int finalCount = count;
						context.getSource().sendSuccess(() -> Component.literal("Retornados " + finalCount + " confesados e inquisidores a sus posiciones originales."), true);
						return 1;
					})
				)
				.executes(context -> {
					ServerPlayer sender = context.getSource().getPlayerOrException();
					net.mcreator.quequeworld.world.QueQueWorldData worldData = net.mcreator.quequeworld.world.QueQueWorldData.get(sender.serverLevel());
					
					if (!worldData.camaraSet) {
						context.getSource().sendFailure(Component.literal("§cLa posición de la cámara del confesionario no está marcada. Usa /confesar camara primero."));
						return 0;
					}
					
					playPuffEffect(sender.serverLevel(), sender.getX(), sender.getY(), sender.getZ());
					
					ResourceKey<Level> camaraDimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(worldData.camaraDim));
					ServerLevel camaraLevel = sender.server.getLevel(camaraDimKey);
					if (camaraLevel != null) {
						sender.teleportTo(camaraLevel, worldData.camaraX, worldData.camaraY, worldData.camaraZ, worldData.camaraYaw, worldData.camaraPitch);
						playPuffEffect(camaraLevel, worldData.camaraX, worldData.camaraY, worldData.camaraZ);
					}
					
					context.getSource().sendSuccess(() -> Component.literal("§aTeletransportado al confesionario (cámara)."), true);
					return 1;
				})
			)
			
			// 10. spawnmirror
			.then(Commands.literal("spawnmirror")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					net.mcreator.quequeworld.world.QueQueWorldData data = net.mcreator.quequeworld.world.QueQueWorldData.get(player.serverLevel());
					data.spawnX = player.getX();
					data.spawnY = player.getY();
					data.spawnZ = player.getZ();
					data.spawnYaw = player.getYRot();
					data.spawnPitch = player.getXRot();
					data.spawnDim = player.level().dimension().location().toString();
					data.setDirty();
					context.getSource().sendSuccess(() -> Component.literal("Punto de spawn del Espejo de Ender actualizado a tus coordenadas actuales."), true);
					return 1;
				})
			)
			
			// 11. marcarlos
			.then(Commands.literal("marcarlos")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					net.minecraft.server.MinecraftServer server = context.getSource().getServer();
					int count = 0;
					for (ServerPlayer player : server.getPlayerList().getPlayers()) {
						if (player.getTags().contains("dios")) continue;
						CompoundTag pData = player.getPersistentData();
						pData.putDouble("marked_x", player.getX());
						pData.putDouble("marked_y", player.getY());
						pData.putDouble("marked_z", player.getZ());
						pData.putFloat("marked_yaw", player.getYRot());
						pData.putFloat("marked_pitch", player.getXRot());
						pData.putString("marked_dim", player.level().dimension().location().toString());
						count++;
					}
					final int finalCount = count;
					context.getSource().sendSuccess(() -> Component.literal("Posición guardada para " + finalCount + " jugadores."), true);
					return 1;
				})
			)
			
			// 11.5. pausardia
			.then(Commands.literal("pausardia")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.argument("value", BoolArgumentType.bool())
					.executes(context -> {
						boolean val = BoolArgumentType.getBool(context, "value");
						net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused = val;
						net.mcreator.quequeworld.timer.TimerManager.syncToAll(context.getSource().getServer());
						if (val) {
							context.getSource().sendSuccess(() -> Component.literal("§aReloj del día pausado."), true);
						} else {
							context.getSource().sendSuccess(() -> Component.literal("§aReloj del día reanudado."), true);
						}
						return 1;
					})
				)
				.executes(context -> {
					boolean val = !net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused;
					net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused = val;
					net.mcreator.quequeworld.timer.TimerManager.syncToAll(context.getSource().getServer());
					if (val) {
						context.getSource().sendSuccess(() -> Component.literal("§aReloj del día pausado."), true);
					} else {
						context.getSource().sendSuccess(() -> Component.literal("§aReloj del día reanudado."), true);
					}
					return 1;
				})
			)
			
			// 12. regresarlos y regresar
			.then(Commands.literal("regresarlos")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.executes(context -> performRegresarlosTodos(context.getSource()))
			)
			.then(Commands.literal("regresar")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.argument("jugador", EntityArgument.player())
					.executes(context -> {
						ServerPlayer target = EntityArgument.getPlayer(context, "jugador");
						return performRegresarJugador(context.getSource(), target);
					})
				)
				.executes(context -> performRegresarlosTodos(context.getSource()))
			)
			
			// 13. traerlos
			.then(Commands.literal("traerlos")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.executes(context -> {
					Entity sender = context.getSource().getEntity();
					if (sender != null) {
						return performTraerlos(context.getSource(), sender.getX(), sender.getY(), sender.getZ(), (ServerLevel) sender.level(), 5.0);
					} else {
						context.getSource().sendFailure(Component.literal("Este comando debe ser llamado por una entidad si no se especifican coordenadas."));
						return 0;
					}
				})
				.then(Commands.literal("todos")
					.executes(context -> {
						Entity sender = context.getSource().getEntity();
						if (sender != null) {
							return performTraerlos(context.getSource(), sender.getX(), sender.getY(), sender.getZ(), (ServerLevel) sender.level(), 5.0);
						} else {
							context.getSource().sendFailure(Component.literal("Este comando debe ser llamado por una entidad."));
							return 0;
						}
					})
					.then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.0))
						.executes(context -> {
							Entity sender = context.getSource().getEntity();
							double radius = DoubleArgumentType.getDouble(context, "radius");
							if (sender != null) {
								return performTraerlos(context.getSource(), sender.getX(), sender.getY(), sender.getZ(), (ServerLevel) sender.level(), radius);
							} else {
								context.getSource().sendFailure(Component.literal("Este comando debe ser llamado por una entidad."));
								return 0;
							}
						})
					)
				)
				.then(Commands.literal("jugador")
					.then(Commands.argument("jugador", EntityArgument.player())
						.executes(context -> {
							ServerPlayer target = EntityArgument.getPlayer(context, "jugador");
							Entity sender = context.getSource().getEntity();
							double x = sender != null ? sender.getX() : target.getX();
							double y = sender != null ? sender.getY() : target.getY();
							double z = sender != null ? sender.getZ() : target.getZ();
							ServerLevel level = sender != null ? (ServerLevel) sender.level() : target.serverLevel();
							return performTraerlosJugador(context.getSource(), target, x, y, z, level, false);
						})
					)
				)
				.then(Commands.literal("equipo")
					.then(Commands.argument("equipo", com.mojang.brigadier.arguments.StringArgumentType.string())
						.suggests(SUGGEST_FTB_TEAMS)
						.executes(context -> {
							String teamArg = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "equipo");
							return performTraerlosEquipo(context.getSource(), teamArg, 5.0);
						})
						.then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.0))
							.executes(context -> {
								String teamArg = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "equipo");
								double radius = DoubleArgumentType.getDouble(context, "radius");
								return performTraerlosEquipo(context.getSource(), teamArg, radius);
							})
						)
					)
				)
				.then(Commands.literal("desafio")
					.executes(context -> {
						Entity sender = context.getSource().getEntity();
						if (sender != null) {
							return performTraerlosDesafio(context.getSource(), sender.getX(), sender.getY(), sender.getZ(), (ServerLevel) sender.level(), 5.0, null);
						} else {
							context.getSource().sendFailure(Component.literal("Este comando debe ser llamado por una entidad."));
							return 0;
						}
					})
					.then(Commands.argument("jugador", EntityArgument.player())
						.executes(context -> {
							ServerPlayer target = EntityArgument.getPlayer(context, "jugador");
							Entity sender = context.getSource().getEntity();
							double x = sender != null ? sender.getX() : target.getX();
							double y = sender != null ? sender.getY() : target.getY();
							double z = sender != null ? sender.getZ() : target.getZ();
							ServerLevel level = sender != null ? (ServerLevel) sender.level() : target.serverLevel();
							return performTraerlosDesafio(context.getSource(), x, y, z, level, 5.0, target);
						})
					)
				)
				.then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.0))
					.executes(context -> {
						Entity sender = context.getSource().getEntity();
						double radius = DoubleArgumentType.getDouble(context, "radius");
						if (sender != null) {
							return performTraerlos(context.getSource(), sender.getX(), sender.getY(), sender.getZ(), (ServerLevel) sender.level(), radius);
						} else {
							context.getSource().sendFailure(Component.literal("Este comando debe ser llamado por una entidad si no se especifican coordenadas."));
							return 0;
						}
					})
				)
				.then(Commands.argument("target", EntityArgument.entity())
					.executes(context -> {
						Entity target = EntityArgument.getEntity(context, "target");
						return performTraerlos(context.getSource(), target.getX(), target.getY(), target.getZ(), (ServerLevel) target.level(), 5.0);
					})
					.then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.0))
						.executes(context -> {
							Entity target = EntityArgument.getEntity(context, "target");
							double radius = DoubleArgumentType.getDouble(context, "radius");
							return performTraerlos(context.getSource(), target.getX(), target.getY(), target.getZ(), (ServerLevel) target.level(), radius);
						})
					)
				)
				.then(Commands.argument("pos", Vec3Argument.vec3())
					.executes(context -> {
						net.minecraft.world.phys.Vec3 pos = Vec3Argument.getVec3(context, "pos");
						return performTraerlos(context.getSource(), pos.x, pos.y, pos.z, context.getSource().getLevel(), 5.0);
					})
					.then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.0))
						.executes(context -> {
							net.minecraft.world.phys.Vec3 pos = Vec3Argument.getVec3(context, "pos");
							double radius = DoubleArgumentType.getDouble(context, "radius");
							return performTraerlos(context.getSource(), pos.x, pos.y, pos.z, context.getSource().getLevel(), radius);
						})
					)
				)
			)
			
			// 14. escudo
			.then(Commands.literal("escudo")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
					.executes(context -> {
						ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
						net.mcreator.quequeworld.event.SoulShieldEventHandler.applyShield(target);
						context.getSource().sendSuccess(() -> Component.literal("§a[QueQueWorld] Escudo restaurado para " + target.getScoreboardName()), true);
						return 1;
					})
				)
			)
			
			// 14.5. comprarescudo
			.then(Commands.literal("comprarescudo")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
					.then(Commands.argument("item", net.minecraft.commands.arguments.item.ItemArgument.item(event.getBuildContext()))
						.then(Commands.argument("cantidad", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
							.executes(context -> {
								ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
								if (target.getTags().contains("tiene_escudo")) {
									target.displayClientMessage(Component.literal("§c[QueQueWorld] Ya tienes un Escudo de Alma activo."), false);
									context.getSource().sendFailure(Component.literal("§c" + target.getScoreboardName() + " ya tiene un Escudo de Alma activo."));
									return 0;
								}
								net.minecraft.commands.arguments.item.ItemInput itemInput = net.minecraft.commands.arguments.item.ItemArgument.getItem(context, "item");
								int cantidad = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "cantidad");

								net.minecraft.world.item.ItemStack dummyStack = itemInput.createItemStack(1, false);
								net.minecraft.world.item.Item requiredItem = dummyStack.getItem();
								int countInInventory = 0;
								for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
									ItemStack stack = target.getInventory().getItem(i);
									if (stack.is(requiredItem)) {
										countInInventory += stack.getCount();
									}
								}

								if (countInInventory >= cantidad) {
									int remainingToRemove = cantidad;
									for (int i = 0; i < target.getInventory().getContainerSize() && remainingToRemove > 0; i++) {
										ItemStack stack = target.getInventory().getItem(i);
										if (stack.is(requiredItem)) {
											int toRemove = Math.min(remainingToRemove, stack.getCount());
											stack.shrink(toRemove);
											remainingToRemove -= toRemove;
										}
									}

									net.mcreator.quequeworld.event.SoulShieldEventHandler.applyShield(target);
									target.displayClientMessage(Component.literal("§a[QueQueWorld] ¡Has comprado y activado el Escudo de Alma!"), false);
									context.getSource().sendSuccess(() -> Component.literal("§a[QueQueWorld] Escudo de Alma comprado con éxito para " + target.getScoreboardName()), true);
									return 1;
								} else {
									target.displayClientMessage(Component.literal("§c[QueQueWorld] No tienes suficientes recursos (" + requiredItem.getDescription().getString() + ") para comprar el Escudo de Alma."), false);
									context.getSource().sendFailure(Component.literal("§c" + target.getScoreboardName() + " no tiene suficientes " + requiredItem.getDescription().getString() + " (" + countInInventory + "/" + cantidad + ")"));
									return 0;
								}
							})
						)
					)
				)
			)
			
			// 15. amenazar
			.then(Commands.literal("amenazar")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
					.executes(context -> {
						ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
						net.mcreator.quequeworld.event.SoulShieldEventHandler.applyThreat(target);
						context.getSource().sendSuccess(() -> Component.literal("§c[QueQueWorld] Amenaza aplicada a " + target.getScoreboardName()), true);
						return 1;
					})
				)
			)
			
			// 16. eliminar
			.then(Commands.literal("eliminar")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
					.executes(context -> {
						ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
						net.mcreator.quequeworld.event.SoulShieldEventHandler.applyEliminated(target);
						context.getSource().sendSuccess(() -> Component.literal("§4[QueQueWorld] Jugador " + target.getScoreboardName() + " eliminado de la serie"), true);
						return 1;
					})
				)
			)
			
			// 17. banderin
			.then(Commands.literal("banderin")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("agregar")
					.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
						.then(Commands.argument("cantidad", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
							.executes(context -> {
								ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
								int cantidad = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "cantidad");
								net.mcreator.quequeworld.event.SoulShieldEventHandler.addBanderin(target, cantidad);
								context.getSource().sendSuccess(() -> Component.literal("§b[QueQueWorld] Agregados " + cantidad + " banderines a " + target.getScoreboardName()), true);
								return 1;
							})
						)
						.executes(context -> {
							ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
							net.mcreator.quequeworld.event.SoulShieldEventHandler.addBanderin(target, 1);
							context.getSource().sendSuccess(() -> Component.literal("§b[QueQueWorld] Agregado 1 banderín a " + target.getScoreboardName()), true);
							return 1;
						})
					)
				)
				.then(Commands.literal("quitar")
					.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
						.then(Commands.argument("cantidad", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
							.executes(context -> {
								ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
								int cantidad = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "cantidad");
								net.mcreator.quequeworld.event.SoulShieldEventHandler.removeBanderin(target, cantidad);
								context.getSource().sendSuccess(() -> Component.literal("§b[QueQueWorld] Retirados " + cantidad + " banderines a " + target.getScoreboardName()), true);
								return 1;
							})
						)
					)
				)
				.then(Commands.literal("set")
					.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
						.then(Commands.argument("cantidad", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
							.executes(context -> {
								ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
								int cantidad = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "cantidad");
								net.mcreator.quequeworld.event.SoulShieldEventHandler.setBanderines(target, cantidad);
								context.getSource().sendSuccess(() -> Component.literal("§b[QueQueWorld] Banderines de " + target.getScoreboardName() + " establecidos en " + cantidad), true);
								return 1;
							})
						)
					)
				)
				.then(Commands.literal("minima")
					.then(Commands.argument("cantidad", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
						.executes(context -> {
							int cantidad = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "cantidad");
							net.mcreator.quequeworld.world.QueQueWorldData worldData = net.mcreator.quequeworld.world.QueQueWorldData.get(context.getSource().getLevel());
							worldData.minBanderines = cantidad;
							worldData.setDirty();
							context.getSource().sendSuccess(() -> Component.literal("§a[QueQueWorld] Cantidad mínima de banderines de referencia establecida en " + cantidad), true);
							return 1;
						})
					)
				)
			)
			
			// 18. deudas
			.then(Commands.literal("deudas")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("agregar")
					.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
						.then(Commands.argument("cantidad", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
							.executes(context -> {
								ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
								int cantidad = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "cantidad");
								net.mcreator.quequeworld.event.SoulShieldEventHandler.addDeuda(target, cantidad);
								context.getSource().sendSuccess(() -> Component.literal("§b[QueQueWorld] Agregada de deuda de " + cantidad + " banderines a " + target.getScoreboardName()), true);
								return 1;
							})
						)
						.executes(context -> {
							ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
							net.mcreator.quequeworld.event.SoulShieldEventHandler.addDeuda(target, 1);
							context.getSource().sendSuccess(() -> Component.literal("§b[QueQueWorld] Agregada de deuda de 1 banderín a " + target.getScoreboardName()), true);
							return 1;
						})
					)
				)
				.then(Commands.literal("quitar")
					.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
						.then(Commands.argument("cantidad", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
							.executes(context -> {
								ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
								int cantidad = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "cantidad");
								net.mcreator.quequeworld.event.SoulShieldEventHandler.removeDeuda(target, cantidad);
								context.getSource().sendSuccess(() -> Component.literal("§b[QueQueWorld] Retirada de deuda de " + cantidad + " banderines a " + target.getScoreboardName()), true);
								return 1;
							})
						)
					)
				)
				.then(Commands.literal("set")
					.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.player())
						.then(Commands.argument("cantidad", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
							.executes(context -> {
								ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "jugador");
								int cantidad = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "cantidad");
								net.mcreator.quequeworld.event.SoulShieldEventHandler.setDeuda(target, cantidad);
								context.getSource().sendSuccess(() -> Component.literal("§b[QueQueWorld] Deuda de " + target.getScoreboardName() + " establecida en " + cantidad + "/8"), true);
								return 1;
							})
						)
					)
				)
			)
			
			
			// 18.6. portales (creacion/apertura)
			.then(Commands.literal("portales")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.literal("bloquear")
					.executes(context -> {
						net.mcreator.quequeworld.world.QueQueWorldData data = net.mcreator.quequeworld.world.QueQueWorldData.get(context.getSource().getLevel());
						data.portalesRestringidos = true;
						data.setDirty();
						context.getSource().sendSuccess(() -> Component.literal("🔒 §cCreación/apertura de portales bloqueada para jugadores comunes."), true);
						return 1;
					})
				)
				.then(Commands.literal("permitir")
					.executes(context -> {
						net.mcreator.quequeworld.world.QueQueWorldData data = net.mcreator.quequeworld.world.QueQueWorldData.get(context.getSource().getLevel());
						data.portalesRestringidos = false;
						data.setDirty();
						context.getSource().sendSuccess(() -> Component.literal("🔓 §aCreación/apertura de portales permitida para todos los jugadores."), true);
						return 1;
					})
				)
			)
			// 18.7. portalpasar (control individual de paso por portales)
			.then(Commands.literal("portalpasar")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.argument("jugador", EntityArgument.player())
					.then(Commands.literal("bloquear")
						.executes(context -> {
							ServerPlayer target = EntityArgument.getPlayer(context, "jugador");
							target.addTag("qqw_bloqueo_portal_individual");
							context.getSource().sendSuccess(() -> Component.literal("🔒 §cPaso por portales bloqueado individualmente para " + target.getScoreboardName()), true);
							return 1;
						})
					)
					.then(Commands.literal("permitir")
						.executes(context -> {
							ServerPlayer target = EntityArgument.getPlayer(context, "jugador");
							target.removeTag("qqw_bloqueo_portal_individual");
							context.getSource().sendSuccess(() -> Component.literal("🔓 §aPaso por portales permitido individualmente para " + target.getScoreboardName()), true);
							return 1;
						})
					)
				)
			)

// 18.5. meta
			.then(Commands.literal("meta")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.literal("setspawn")
					.then(Commands.argument("radius", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(0.0))
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							double radius = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(context, "radius");
							net.mcreator.quequeworld.minigame.GoalManager.setSpawn(
								player.getX(), player.getY(), player.getZ(),
								player.getYRot(), player.getXRot(),
								player.level().dimension().location().toString(),
								radius
							);
							context.getSource().sendSuccess(() -> Component.literal("§a[Meta] Spawn de llegada configurado en tu ubicación actual con radio " + radius + "m."), true);
							return 1;
						})
					)
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						net.mcreator.quequeworld.minigame.GoalManager.setSpawn(
							player.getX(), player.getY(), player.getZ(),
							player.getYRot(), player.getXRot(),
							player.level().dimension().location().toString(),
							3.0
						);
						context.getSource().sendSuccess(() -> Component.literal("§a[Meta] Spawn de llegada configurado en tu ubicación actual con radio 3.0m."), true);
						return 1;
					})
				)
				.then(Commands.literal("reset")
					.executes(context -> {
						net.mcreator.quequeworld.minigame.GoalManager.reset();
						context.getSource().sendSuccess(() -> Component.literal("§a[Meta] Puestos y registros de meta reiniciados correctamente."), true);
						return 1;
					})
				)
				.then(Commands.literal("status")
					.executes(context -> {
						context.getSource().sendSuccess(() -> Component.literal("§e=== Tabla de Llegadas a la Meta ==="), false);
						if (net.mcreator.quequeworld.minigame.GoalManager.leaderBoard.isEmpty()) {
							context.getSource().sendSuccess(() -> Component.literal("§7Nadie ha cruzado la meta aún."), false);
						} else {
							net.mcreator.quequeworld.minigame.GoalManager.leaderBoard.forEach((pos, name) -> {
								context.getSource().sendSuccess(() -> Component.literal("§a" + pos + "º Lugar: §b" + name), false);
							});
						}
						return 1;
					})
				)
			)
			
			// 19. info (ver estado)
			.then(Commands.literal("info")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("jugador", net.minecraft.commands.arguments.EntityArgument.players())
					.executes(context -> {
						java.util.Collection<ServerPlayer> targets = net.minecraft.commands.arguments.EntityArgument.getPlayers(context, "jugador");
						net.mcreator.quequeworld.world.QueQueWorldData worldData = net.mcreator.quequeworld.world.QueQueWorldData.get(context.getSource().getLevel());
						
						for (ServerPlayer target : targets) {
							CompoundTag tagData = target.getPersistentData();
							int banderines = tagData.getInt("qqw_banderines");
							int deuda = tagData.getInt("qqw_deuda_banderines");
							boolean isThreatened = target.getTags().contains("amenazado");
							boolean hasShield = target.getTags().contains("tiene_escudo");
							boolean isEliminated = target.getTags().contains("eliminado");
							double danger = net.mcreator.quequeworld.event.SoulShieldEventHandler.getDangerLevel(target) * 100.0D;
							
							context.getSource().sendSuccess(() -> Component.literal("§e=== Información de " + target.getScoreboardName() + " ==="), false);
							context.getSource().sendSuccess(() -> Component.literal("§fBanderines: §b" + banderines + " §7(Mínimo Ref: " + worldData.minBanderines + ")"), false);
							context.getSource().sendSuccess(() -> Component.literal("§fDeuda de Banderines: §c" + deuda + "/8"), false);
							context.getSource().sendSuccess(() -> Component.literal("§fNivel de Peligro: §e" + String.format("%.1f", danger) + "%"), false);
							context.getSource().sendSuccess(() -> Component.literal("§fEstado: " + 
								(isEliminated ? "§4ELIMINADO" : (isThreatened ? "§cAMENAZADO" : (hasShield ? "§aESCUDO ACTIVO" : "§7NORMAL")))), false);
						}
						return 1;
					})
				)
			)
			
			// 21. toplay
			.then(Commands.literal("toplay")
				.requires(source -> source.hasPermission(4))
				.then(Commands.argument("target", net.minecraft.commands.arguments.EntityArgument.entities())
					.then(Commands.argument("pos", net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos())
						.executes(context -> {
							CommandSourceStack source = context.getSource();
							java.util.Collection<ServerPlayer> players = net.minecraft.commands.arguments.EntityArgument.getPlayers(context, "target");
							BlockPos pos = net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos(context, "pos");
							
							ResourceKey<Level> destinationKey = ResourceKey.create(
								Registries.DIMENSION, 
								ResourceLocation.fromNamespaceAndPath("queque", "lab")
							);

							ServerLevel targetWorld = source.getServer().getLevel(destinationKey);

							if (targetWorld == null) {
								source.sendFailure(Component.literal("❌ Error: La dimensión destino no existe o no está cargada."));
								return 0;
							}

							for (ServerPlayer player : players) {
								player.teleportTo(
									targetWorld, 
									pos.getX() + 0.5,
									pos.getY(), 
									pos.getZ() + 0.5, 
									player.getYRot(), 
									player.getXRot()
								);
							}

							source.sendSuccess(() -> Component.literal("⚡ Jugadores teletransportados con éxito a la zona del evento."), true);
							return 1;
						})
					)
				)
			)
			.then(Commands.literal("guardar_cosas")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("jugadores", net.minecraft.commands.arguments.EntityArgument.players())
					.executes(context -> executeGuardarCosas(context.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayers(context, "jugadores"), true))
					.then(Commands.argument("limpiar", com.mojang.brigadier.arguments.BoolArgumentType.bool())
						.executes(context -> executeGuardarCosas(context.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayers(context, "jugadores"), com.mojang.brigadier.arguments.BoolArgumentType.getBool(context, "limpiar")))
					)
				)
			)
			.then(Commands.literal("recuperar_cosas")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("jugadores", net.minecraft.commands.arguments.EntityArgument.players())
					.executes(context -> executeRecuperarCosas(context.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayers(context, "jugadores")))
				)
			)
			// 22. banderines (<equipo> | ranking)
			.then(Commands.literal("banderines")
				.requires(source -> source.hasPermission(2))
				.executes(context -> listAllTeamsBanderines(context.getSource()))
				.then(Commands.argument("equipo", com.mojang.brigadier.arguments.StringArgumentType.string())
					.suggests(SUGGEST_FTB_TEAMS)
					.executes(context -> showTeamBanderines(context.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(context, "equipo")))
				)
			)
			// 23. bienvenida (img [nombre] | texto [texto])
			.then(Commands.literal("bienvenida")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.literal("img")
					.then(Commands.argument("banner", com.mojang.brigadier.arguments.StringArgumentType.string())
						.executes(context -> {
							String banner = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "banner");
							PacketDistributor.sendToAllPlayers(new net.mcreator.quequeworld.network.WelcomePacket("img", banner));
							context.getSource().sendSuccess(() -> Component.literal("§a[Bienvenida] Enviado banner 'img: " + banner + "' a todos los jugadores."), true);
							return 1;
						})
					)
				)
				.then(Commands.literal("texto")
					.then(Commands.argument("mensaje", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
						.executes(context -> {
							String msg = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "mensaje");
							PacketDistributor.sendToAllPlayers(new net.mcreator.quequeworld.network.WelcomePacket("texto", msg));
							context.getSource().sendSuccess(() -> Component.literal("§a[Bienvenida] Enviado texto '" + msg + "' a todos los jugadores."), true);
							return 1;
						})
					)
				)
			)
			// 24. random (<opciones> [duracion_segundos])
			.then(Commands.literal("random")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.argument("opciones", com.mojang.brigadier.arguments.StringArgumentType.string())
					.executes(context -> net.mcreator.quequeworld.roulette.RouletteManager.runRandom(context.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(context, "opciones"), 5))
					.then(Commands.argument("duracion", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 60))
						.executes(context -> net.mcreator.quequeworld.roulette.RouletteManager.runRandom(context.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(context, "opciones"), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "duracion")))
					)
				)
			)
			// 25. fakerandom (<resultado_forzado> <opciones> [duracion_segundos])
			.then(Commands.literal("fakerandom")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.then(Commands.argument("resultado_forzado", com.mojang.brigadier.arguments.StringArgumentType.string())
					.then(Commands.argument("opciones", com.mojang.brigadier.arguments.StringArgumentType.string())
						.executes(context -> net.mcreator.quequeworld.roulette.RouletteManager.runFakeRandom(context.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(context, "resultado_forzado"), com.mojang.brigadier.arguments.StringArgumentType.getString(context, "opciones"), 5))
						.then(Commands.argument("duracion", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 60))
							.executes(context -> net.mcreator.quequeworld.roulette.RouletteManager.runFakeRandom(context.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(context, "resultado_forzado"), com.mojang.brigadier.arguments.StringArgumentType.getString(context, "opciones"), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "duracion")))
						)
					)
				)
			)
			// 26. randomjugador ([duracion_segundos])
			.then(Commands.literal("randomjugador")
				.requires(source -> source.hasPermission(2) || (source.isPlayer() && source.getPlayer().getTags().contains("dios")))
				.executes(context -> net.mcreator.quequeworld.roulette.RouletteManager.runRandomPlayer(context.getSource(), 5))
				.then(Commands.argument("duracion", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 60))
					.executes(context -> net.mcreator.quequeworld.roulette.RouletteManager.runRandomPlayer(context.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "duracion")))
				)
			)
		);
	}

	private static int executeGuardarCosas(CommandSourceStack source, java.util.Collection<ServerPlayer> targets, boolean limpiar) {
		for (ServerPlayer target : targets) {
			CompoundTag data = target.getPersistentData();
			net.minecraft.nbt.ListTag invNbt = target.getInventory().save(new net.minecraft.nbt.ListTag());
			data.put("qqw_manual_saved_inv", invNbt);

			if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
				top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
					net.minecraft.nbt.ListTag curiosList = new net.minecraft.nbt.ListTag();
					handler.getCurios().forEach((identifier, slotHandler) -> {
						net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
						for (int i = 0; i < stacks.getSlots(); i++) {
							net.minecraft.world.item.ItemStack stack = stacks.getStackInSlot(i);
							if (!stack.isEmpty()) {
								CompoundTag itemTag = new CompoundTag();
								itemTag.putString("Identifier", identifier);
								itemTag.putInt("Slot", i);
								net.minecraft.nbt.Tag serializedStack = stack.save(target.level().registryAccess());
								itemTag.put("Item", serializedStack);
								curiosList.add(itemTag);
							}
						}
					});
					data.put("qqw_manual_saved_curios", curiosList);
				});
			}

			if (limpiar) {
				target.getInventory().clearContent();
				if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
					top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
						handler.getCurios().forEach((identifier, slotHandler) -> {
							net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
							for (int i = 0; i < stacks.getSlots(); i++) {
								stacks.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
							}
						});
					});
				}
			}
		}
		source.sendSuccess(() -> Component.literal("§a[QueQueWorld] Inventario guardado" + (limpiar ? " y limpiado" : "") + " para " + targets.size() + " jugador(es)."), true);
		return 1;
	}

	private static int executeRecuperarCosas(CommandSourceStack source, java.util.Collection<ServerPlayer> targets) {
		int restoredCount = 0;
		for (ServerPlayer target : targets) {
			CompoundTag data = target.getPersistentData();
			boolean restoredStandard = false;
			boolean restoredCurios = false;

			if (data.contains("qqw_manual_saved_inv")) {
				net.minecraft.nbt.ListTag invNbt = data.getList("qqw_manual_saved_inv", net.minecraft.nbt.Tag.TAG_COMPOUND);
				target.getInventory().load(invNbt);
				data.remove("qqw_manual_saved_inv");
				restoredStandard = true;
			}

			if (net.neoforged.fml.ModList.get().isLoaded("curios") && data.contains("qqw_manual_saved_curios")) {
				final net.minecraft.nbt.ListTag curiosList = data.getList("qqw_manual_saved_curios", net.minecraft.nbt.Tag.TAG_COMPOUND);
				top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
					handler.getCurios().forEach((identifier, slotHandler) -> {
						net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
						for (int i = 0; i < stacks.getSlots(); i++) {
							stacks.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
						}
					});
					for (int i = 0; i < curiosList.size(); i++) {
						CompoundTag itemTag = curiosList.getCompound(i);
						String identifier = itemTag.getString("Identifier");
						int slot = itemTag.getInt("Slot");
						CompoundTag stackTag = itemTag.getCompound("Item");
						java.util.Optional<net.minecraft.world.item.ItemStack> parsedOpt = 
							net.minecraft.world.item.ItemStack.parse(target.level().registryAccess(), stackTag);
						if (parsedOpt.isPresent()) {
							net.minecraft.world.item.ItemStack stack = parsedOpt.get();
							var slotHandler = handler.getCurios().get(identifier);
							if (slotHandler != null) {
								net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
								if (slot >= 0 && slot < stacks.getSlots()) {
									stacks.setStackInSlot(slot, stack);
								}
							}
						}
					}
				});
				data.remove("qqw_manual_saved_curios");
				restoredCurios = true;
			}

			if (restoredStandard || restoredCurios) {
				restoredCount++;
			}
		}
		final int finalRestored = restoredCount;
		source.sendSuccess(() -> Component.literal("§a[QueQueWorld] Inventario recuperado para " + finalRestored + " de " + targets.size() + " jugador(es)."), true);
		return 1;
	}

	public static void unpauseGame(net.minecraft.server.MinecraftServer server) {
		gamePaused = false;
		server.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_RANDOMTICKING).set(3, server);
		server.tickRateManager().setFrozen(false);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			player.getPersistentData().remove("paused_x");
			player.getPersistentData().remove("paused_y");
			player.getPersistentData().remove("paused_z");
			player.getPersistentData().remove("paused_yaw");
			player.getPersistentData().remove("paused_pitch");
		}
	}

	public static void playPuffEffect(ServerLevel level, double x, double y, double z) {
		level.sendParticles(ParticleTypes.POOF, x, y + 1.0, z, 50, 0.3, 0.5, 0.3, 0.05);
		level.playSound(null, x, y, z, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.5F, 1.2F);
	}

	private static void saveNormalPlayerPosition(ServerPlayer player) {
		CompoundTag pData = player.getPersistentData();
		if (!pData.contains("marked_x")) {
			pData.putDouble("marked_x", player.getX());
			pData.putDouble("marked_y", player.getY());
			pData.putDouble("marked_z", player.getZ());
			pData.putFloat("marked_yaw", player.getYRot());
			pData.putFloat("marked_pitch", player.getXRot());
			pData.putString("marked_dim", player.level().dimension().location().toString());
		}
	}

	private static void saveDesafioPositionAndInventory(ServerPlayer player) {
		CompoundTag pData = player.getPersistentData();
		if (!pData.contains("qqw_desafio_marked_x")) {
			// Si ya tenía datos normales guardados, los promovemos a desafío para preservar su verdadero origen
			if (pData.contains("marked_x")) {
				pData.putDouble("qqw_desafio_marked_x", pData.getDouble("marked_x"));
				pData.putDouble("qqw_desafio_marked_y", pData.getDouble("marked_y"));
				pData.putDouble("qqw_desafio_marked_z", pData.getDouble("marked_z"));
				pData.putFloat("qqw_desafio_marked_yaw", pData.getFloat("marked_yaw"));
				pData.putFloat("qqw_desafio_marked_pitch", pData.getFloat("marked_pitch"));
				pData.putString("qqw_desafio_marked_dim", pData.getString("marked_dim"));
			} else {
				pData.putDouble("qqw_desafio_marked_x", player.getX());
				pData.putDouble("qqw_desafio_marked_y", player.getY());
				pData.putDouble("qqw_desafio_marked_z", player.getZ());
				pData.putFloat("qqw_desafio_marked_yaw", player.getYRot());
				pData.putFloat("qqw_desafio_marked_pitch", player.getXRot());
				pData.putString("qqw_desafio_marked_dim", player.level().dimension().location().toString());
			}
		}
		savePlayerInventoryAndCurios(player);
	}

	private static void savePlayerInventoryAndCurios(ServerPlayer target) {
		CompoundTag tData = target.getPersistentData();
		net.minecraft.nbt.ListTag invNbt = target.getInventory().save(new net.minecraft.nbt.ListTag());
		tData.put("qqw_desafio_saved_inv", invNbt);

		if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
			top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
				net.minecraft.nbt.ListTag curiosList = new net.minecraft.nbt.ListTag();
				handler.getCurios().forEach((identifier, slotHandler) -> {
					net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
					for (int i = 0; i < stacks.getSlots(); i++) {
						net.minecraft.world.item.ItemStack stack = stacks.getStackInSlot(i);
						if (!stack.isEmpty()) {
							CompoundTag itemTag = new CompoundTag();
							itemTag.putString("Identifier", identifier);
							itemTag.putInt("Slot", i);
							net.minecraft.nbt.Tag serializedStack = stack.save(target.level().registryAccess());
							itemTag.put("Item", serializedStack);
							curiosList.add(itemTag);
						}
					}
				});
				tData.put("qqw_desafio_saved_curios", curiosList);
			});
		}

		target.getInventory().clearContent();
		if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
			top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
				handler.getCurios().forEach((identifier, slotHandler) -> {
					net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
					for (int i = 0; i < stacks.getSlots(); i++) {
						stacks.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
					}
				});
			});
		}
	}

	private static void restorePlayerInventoryAndCurios(ServerPlayer player) {
		CompoundTag pData = player.getPersistentData();
		if (pData.contains("qqw_desafio_saved_inv")) {
			net.minecraft.nbt.ListTag invNbt = pData.getList("qqw_desafio_saved_inv", net.minecraft.nbt.Tag.TAG_COMPOUND);
			player.getInventory().load(invNbt);
			pData.remove("qqw_desafio_saved_inv");
		}

		if (net.neoforged.fml.ModList.get().isLoaded("curios") && pData.contains("qqw_desafio_saved_curios")) {
			final net.minecraft.nbt.ListTag curiosList = pData.getList("qqw_desafio_saved_curios", net.minecraft.nbt.Tag.TAG_COMPOUND);
			top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
				handler.getCurios().forEach((identifier, slotHandler) -> {
					net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
					for (int idx = 0; idx < stacks.getSlots(); idx++) {
						stacks.setStackInSlot(idx, net.minecraft.world.item.ItemStack.EMPTY);
					}
				});
				for (int j = 0; j < curiosList.size(); j++) {
					CompoundTag itemTag = curiosList.getCompound(j);
					String identifier = itemTag.getString("Identifier");
					int slot = itemTag.getInt("Slot");
					CompoundTag stackTag = itemTag.getCompound("Item");
					java.util.Optional<net.minecraft.world.item.ItemStack> parsedOpt = 
						net.minecraft.world.item.ItemStack.parse(player.level().registryAccess(), stackTag);
					if (parsedOpt.isPresent()) {
						net.minecraft.world.item.ItemStack stack = parsedOpt.get();
						var slotHandler = handler.getCurios().get(identifier);
						if (slotHandler != null) {
							net.neoforged.neoforge.items.IItemHandlerModifiable stacks = slotHandler.getStacks();
							if (slot >= 0 && slot < stacks.getSlots()) {
								stacks.setStackInSlot(slot, stack);
							}
						}
					}
				}
			});
			pData.remove("qqw_desafio_saved_curios");
		}
	}

	private static boolean restoreNormalPlayerPosition(ServerPlayer player, MinecraftServer server) {
		CompoundTag pData = player.getPersistentData();
		if (!pData.contains("marked_x")) {
			return false;
		}

		double x = pData.getDouble("marked_x");
		double y = pData.getDouble("marked_y");
		double z = pData.getDouble("marked_z");
		float yaw = pData.getFloat("marked_yaw");
		float pitch = pData.getFloat("marked_pitch");
		String dimStr = pData.getString("marked_dim");

		try {
			ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
			ServerLevel targetLevel = server.getLevel(targetDim);
			if (targetLevel != null) {
				player.teleportTo(targetLevel, x, y, z, yaw, pitch);
				playPuffEffect(targetLevel, x, y, z);
			}
		} catch (Exception ignored) {}

		pData.remove("marked_x");
		pData.remove("marked_y");
		pData.remove("marked_z");
		pData.remove("marked_yaw");
		pData.remove("marked_pitch");
		pData.remove("marked_dim");

		return true;
	}

	private static boolean restoreDesafioPositionAndInventory(ServerPlayer player, MinecraftServer server) {
		CompoundTag pData = player.getPersistentData();
		boolean hadDesafioData = pData.contains("qqw_desafio_marked_x") || pData.contains("qqw_desafio_saved_inv");
		if (!hadDesafioData) {
			return false;
		}

		if (pData.contains("qqw_desafio_marked_x")) {
			double x = pData.getDouble("qqw_desafio_marked_x");
			double y = pData.getDouble("qqw_desafio_marked_y");
			double z = pData.getDouble("qqw_desafio_marked_z");
			float yaw = pData.getFloat("qqw_desafio_marked_yaw");
			float pitch = pData.getFloat("qqw_desafio_marked_pitch");
			String dimStr = pData.getString("qqw_desafio_marked_dim");

			try {
				ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
				ServerLevel targetLevel = server.getLevel(targetDim);
				if (targetLevel != null) {
					player.teleportTo(targetLevel, x, y, z, yaw, pitch);
					playPuffEffect(targetLevel, x, y, z);
				}
			} catch (Exception ignored) {}
		}

		restorePlayerInventoryAndCurios(player);

		// Limpiar datos de desafío
		pData.remove("qqw_desafio_marked_x");
		pData.remove("qqw_desafio_marked_y");
		pData.remove("qqw_desafio_marked_z");
		pData.remove("qqw_desafio_marked_yaw");
		pData.remove("qqw_desafio_marked_pitch");
		pData.remove("qqw_desafio_marked_dim");

		// Limpiar TAMBIÉN datos normales acumulados para evitar retornos pendientes
		pData.remove("marked_x");
		pData.remove("marked_y");
		pData.remove("marked_z");
		pData.remove("marked_yaw");
		pData.remove("marked_pitch");
		pData.remove("marked_dim");

		return true;
	}

	private static int performRegresarJugador(CommandSourceStack source, ServerPlayer targetPlayer) {
		boolean success;
		if (targetPlayer.getTags().contains("qqw_en_desafio") || targetPlayer.getPersistentData().contains("qqw_desafio_marked_x") || targetPlayer.getPersistentData().contains("qqw_desafio_saved_inv")) {
			success = restoreDesafioPositionAndInventory(targetPlayer, source.getServer());
		} else {
			success = restoreNormalPlayerPosition(targetPlayer, source.getServer());
		}

		targetPlayer.removeTag("qqw_en_desafio");
		targetPlayer.removeTag("qqw_mecanica_morir_disabled");

		if (!success) {
			source.sendFailure(Component.literal("§cEl jugador " + targetPlayer.getScoreboardName() + " no tiene datos de retorno guardados."));
			return 0;
		}

		boolean hasMoreInDesafio = false;
		for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) {
			if (p.getPersistentData().contains("marked_x") || p.getPersistentData().contains("qqw_desafio_marked_x") || p.getPersistentData().contains("qqw_desafio_saved_inv")) {
				hasMoreInDesafio = true;
				break;
			}
		}
		if (!hasMoreInDesafio && net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused) {
			net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused = false;
			net.mcreator.quequeworld.timer.TimerManager.syncToAll(source.getServer());
		}

		source.sendSuccess(() -> Component.literal("§aJugador " + targetPlayer.getScoreboardName() + " regresado a su ubicación original."), true);
		return 1;
	}

	private static int performRegresarlosTodos(CommandSourceStack source) {
		MinecraftServer server = source.getServer();
		net.mcreator.quequeworld.minigame.GoalManager.reset();
		net.mcreator.quequeworld.minigame.CheckpointManager.clearAll();
		int count = 0;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.getTags().contains("dios")) continue;
			player.removeTag("qqw_en_desafio");
			player.removeTag("qqw_mecanica_morir_disabled");
			if (restoreDesafioPositionAndInventory(player, server) || restoreNormalPlayerPosition(player, server)) {
				count++;
			}
		}

		if (net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused) {
			net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused = false;
			net.mcreator.quequeworld.timer.TimerManager.syncToAll(server);
		}

		final int finalCount = count;
		source.sendSuccess(() -> Component.literal("§aRegresados " + finalCount + " jugadores a sus posiciones originales."), true);
		return 1;
	}

	private static int performTraerlosJugador(CommandSourceStack source, ServerPlayer targetPlayer, double x, double y, double z, ServerLevel targetLevel, boolean isDesafio) {
		if (isDesafio) {
			saveDesafioPositionAndInventory(targetPlayer);
			net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused = true;
			net.mcreator.quequeworld.timer.TimerManager.syncToAll(source.getServer());
			
			targetPlayer.addTag("qqw_en_desafio");
			targetPlayer.addTag("qqw_mecanica_morir_disabled");
			
			net.mcreator.quequeworld.world.QueQueWorldData data = net.mcreator.quequeworld.world.QueQueWorldData.get(targetLevel);
			data.desafioSpawnX = x;
			data.desafioSpawnY = y;
			data.desafioSpawnZ = z;
			data.desafioSpawnYaw = source.getPlayer() != null ? source.getPlayer().getYRot() : 0f;
			data.desafioSpawnPitch = source.getPlayer() != null ? source.getPlayer().getXRot() : 0f;
			data.desafioSpawnDim = targetLevel.dimension().location().toString();
			data.desafioSpawnSet = true;
			data.setDirty();
		} else {
			saveNormalPlayerPosition(targetPlayer);
		}

		net.minecraft.world.phys.Vec3 safePos = findSafeGround(targetLevel, x, y, z, 5.0);
		targetPlayer.stopRiding();
		targetPlayer.stopSleeping();
		targetPlayer.teleportTo(targetLevel, safePos.x, safePos.y, safePos.z, targetPlayer.getYRot(), targetPlayer.getXRot());
		playPuffEffect(targetLevel, safePos.x, safePos.y, safePos.z);

		if (isDesafio) {
			source.sendSuccess(() -> Component.literal("§aJugador " + targetPlayer.getScoreboardName() + " traído al desafío (inventario guardado y reloj pausado)."), true);
			targetPlayer.displayClientMessage(Component.literal("§e[Desafío] ¡Has sido llevado al desafío! Tu inventario ha sido guardado y el reloj del día pausado."), false);
			sendDelayedChallengeTitle(targetPlayer, "Desafío QueQueWorld", 60);
		} else {
			source.sendSuccess(() -> Component.literal("§aJugador " + targetPlayer.getScoreboardName() + " traído (posición guardada)."), true);
		}
		return 1;
	}

	private static int performTraerlosDesafio(CommandSourceStack source, double x, double y, double z, ServerLevel targetLevel, double radius, ServerPlayer singleTarget) {
		if (singleTarget != null) {
			return performTraerlosJugador(source, singleTarget, x, y, z, targetLevel, true);
		}

		MinecraftServer server = source.getServer();
		net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused = true;
		net.mcreator.quequeworld.timer.TimerManager.syncToAll(server);

		int count = 0;
		if (server.getPlayerList().getPlayers().size() > 0) {
			net.mcreator.quequeworld.world.QueQueWorldData data = net.mcreator.quequeworld.world.QueQueWorldData.get(targetLevel);
			data.desafioSpawnX = x;
			data.desafioSpawnY = y;
			data.desafioSpawnZ = z;
			data.desafioSpawnYaw = source.getPlayer() != null ? source.getPlayer().getYRot() : 0f;
			data.desafioSpawnPitch = source.getPlayer() != null ? source.getPlayer().getXRot() : 0f;
			data.desafioSpawnDim = targetLevel.dimension().location().toString();
			data.desafioSpawnSet = true;
			data.setDirty();
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.getTags().contains("dios")) continue;
			saveDesafioPositionAndInventory(player);
			player.addTag("qqw_en_desafio");
			player.addTag("qqw_mecanica_morir_disabled");

			net.minecraft.world.phys.Vec3 safePos = findSafeGround(targetLevel, x, y, z, radius);
			player.stopRiding();
			player.stopSleeping();
			player.teleportTo(targetLevel, safePos.x, safePos.y, safePos.z, player.getYRot(), player.getXRot());
			playPuffEffect(targetLevel, safePos.x, safePos.y, safePos.z);
			player.displayClientMessage(Component.literal("§e[Desafío] ¡Has sido llevado al desafío! Tu inventario ha sido guardado y el reloj del día pausado."), false);
			sendDelayedChallengeTitle(player, "Desafío QueQueWorld", 60);
			count++;
		}

		final int finalCount = count;
		source.sendSuccess(() -> Component.literal("§a[Desafío] Traídos " + finalCount + " jugadores al desafío. Inventarios guardados y reloj del día pausado."), true);
		return 1;
	}

	private static int performTraerlos(CommandSourceStack source, double x, double y, double z, ServerLevel targetLevel, double radius) {
		MinecraftServer server = source.getServer();
		int teleportCount = 0;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.getTags().contains("dios")) continue;
			saveNormalPlayerPosition(player);
			net.minecraft.world.phys.Vec3 safePos = findSafeGround(targetLevel, x, y, z, radius);
			player.stopRiding();
			player.stopSleeping();
			player.teleportTo(targetLevel, safePos.x, safePos.y, safePos.z, player.getYRot(), player.getXRot());
			playPuffEffect(targetLevel, safePos.x, safePos.y, safePos.z);
			teleportCount++;
		}
		final int count = teleportCount;
		source.sendSuccess(() -> Component.literal("§aTraídos " + count + " jugadores a tu ubicación."), true);
		return 1;
	}

	public static net.minecraft.world.phys.Vec3 findSafeGround(ServerLevel level, double centerX, double centerY, double centerZ, double radius) {
		java.util.Random random = new java.util.Random();
		double angle = random.nextDouble() * 2 * Math.PI;
		double r = random.nextDouble() * radius;
		double targetX = centerX + r * Math.cos(angle);
		double targetZ = centerZ + r * Math.sin(angle);
		
		BlockPos centerPos = BlockPos.containing(targetX, centerY, targetZ);
		
		// Search vertically for a safe block.
		for (int offset : new int[]{0, 1, -1, 2, -2, 3, -3, 4, -4, 5, -5, 6, -6, 7, -7, 8, -8, 9, -9, 10, -10, -11, -12, -13, -14, -15}) {
			BlockPos checkPos = centerPos.above(offset);
			BlockState state = level.getBlockState(checkPos);
			BlockState belowState = level.getBlockState(checkPos.below());
			BlockState aboveState = level.getBlockState(checkPos.above());
			
			if (isPassable(level, checkPos, state) && isPassable(level, checkPos.above(), aboveState) && isSolidGround(level, checkPos.below(), belowState)) {
				return new net.minecraft.world.phys.Vec3(checkPos.getX() + 0.5, checkPos.getY(), checkPos.getZ() + 0.5);
			}
		}
		
		return new net.minecraft.world.phys.Vec3(targetX, centerY, targetZ);
	}

	private static boolean isPassable(Level level, BlockPos pos, BlockState state) {
		return state.getCollisionShape(level, pos, net.minecraft.world.phys.shapes.CollisionContext.empty()).isEmpty() && state.getFluidState().isEmpty();
	}

	private static boolean isSolidGround(Level level, BlockPos pos, BlockState state) {
		return !state.isAir() && !state.getCollisionShape(level, pos, net.minecraft.world.phys.shapes.CollisionContext.empty()).isEmpty() && state.getFluidState().isEmpty();
	}

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		QueQueDifficultyConfig.load();
	}

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		net.mcreator.quequeworld.world.QueQueWorldData worldData = net.mcreator.quequeworld.world.QueQueWorldData.get(event.getServer().overworld());
		gamePaused = worldData.gamePaused;
		net.mcreator.quequeworld.timer.TimerManager.dayTimerActive = worldData.dayTimerActive;
		net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused = worldData.dayTimerPaused;
		net.mcreator.quequeworld.timer.TimerManager.dayTimerTicks = worldData.dayTimerTicks;
		net.mcreator.quequeworld.timer.TimerManager.countdownActive = worldData.countdownActive;
		net.mcreator.quequeworld.timer.TimerManager.countdownTicks = worldData.countdownTicks;

		if (gamePaused) {
			event.getServer().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_RANDOMTICKING).set(0, event.getServer());
			event.getServer().tickRateManager().setFrozen(true);
		} else {
			event.getServer().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_RANDOMTICKING).set(3, event.getServer());
			event.getServer().tickRateManager().setFrozen(false);
		}
	}

	@SubscribeEvent
	public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		Level level = event.getLevel();

		if (level.isClientSide()) {
			return;
		}

		if (entity instanceof net.minecraft.world.entity.monster.Enemy || entity instanceof net.minecraft.world.entity.monster.Monster) {
			if (entity.getTags().contains("quequeworld:stats_modified")) {
				return;
			}
			entity.addTag("quequeworld:stats_modified");

			if (entity instanceof LivingEntity living) {
				QueQueDifficultyConfig.Multipliers mult = QueQueDifficultyConfig.instance.multipliers;
				if (mult != null) {
					if (mult.monster_health != 1.0) {
						var maxHealthAttr = living.getAttribute(Attributes.MAX_HEALTH);
						if (maxHealthAttr != null) {
							double baseHealth = maxHealthAttr.getBaseValue();
							maxHealthAttr.setBaseValue(baseHealth * mult.monster_health);
							living.setHealth((float) (baseHealth * mult.monster_health));
						}
					}

					if (mult.monster_damage != 1.0) {
						var attackDamageAttr = living.getAttribute(Attributes.ATTACK_DAMAGE);
						if (attackDamageAttr != null) {
							double baseDamage = attackDamageAttr.getBaseValue();
							attackDamageAttr.setBaseValue(baseDamage * mult.monster_damage);
						}
					}

					if (mult.monster_speed != 1.0) {
						var speedAttr = living.getAttribute(Attributes.MOVEMENT_SPEED);
						if (speedAttr != null) {
							double baseSpeed = speedAttr.getBaseValue();
							speedAttr.setBaseValue(baseSpeed * mult.monster_speed);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		Entity target = event.getTarget();
		ItemStack weapon = player.getMainHandItem();

		if (weapon.is(ModItems.CACHAPORRA.get())) {
			event.setCanceled(true); // Previene daño físico y validaciones de PvP/equipo

			if (target instanceof LivingEntity livingTarget) {
				// Leer el KnockbackPower del NBT
				double knockbackPower = 1.5; // Valor por defecto
				CustomData customData = weapon.get(DataComponents.CUSTOM_DATA);
				if (customData != null) {
					CompoundTag tag = customData.copyTag();
					if (tag.contains("KnockbackPower")) {
						knockbackPower = tag.getDouble("KnockbackPower");
					}
				}

				// Aplicar el empuje (knockback)
				double yawRad = player.getYRot() * Math.PI / 180.0;
				double ratioX = Math.sin(yawRad);
				double ratioZ = -Math.cos(yawRad);
				livingTarget.knockback(knockbackPower, ratioX, ratioZ);

				// Sincronizar velocidad y reproducir animación de daño
				livingTarget.hurtMarked = true;
				if (livingTarget.level() instanceof ServerLevel serverLevel) {
					serverLevel.broadcastEntityEvent(livingTarget, (byte) 2);
				}
			}
		}
	}

	private static BlockPos findSafePosition(ServerLevel level, BlockPos basePos) {
		// 1. Probar Y offsets directos (de 0 a +3 y -3)
		for (int yOffset : new int[]{0, 1, 2, 3, -1, -2, -3}) {
			BlockPos testPos = basePos.offset(0, yOffset, 0);
			if (isSafePosition(level, testPos)) {
				return testPos;
			}
		}
		// 2. Probar offsets horizontales en radio 1 (N, S, E, W, etc.)
		for (int xOffset = -1; xOffset <= 1; xOffset++) {
			for (int zOffset = -1; zOffset <= 1; zOffset++) {
				if (xOffset == 0 && zOffset == 0) continue;
				for (int yOffset : new int[]{0, 1, 2, 3, -1, -2, -3}) {
					BlockPos testPos = basePos.offset(xOffset, yOffset, zOffset);
					if (isSafePosition(level, testPos)) {
						return testPos;
					}
				}
			}
		}
		return null;
	}

	private static boolean isSafePosition(ServerLevel level, BlockPos pos) {
		BlockState feetState = level.getBlockState(pos);
		BlockState headState = level.getBlockState(pos.above());

		// Los bloques de los pies y cabeza deben estar libres de colisiones sólidas
		boolean feetPassable = feetState.getCollisionShape(level, pos).isEmpty();
		boolean headPassable = headState.getCollisionShape(level, pos.above()).isEmpty();

		// Evitar aparecer dentro de lava letal
		boolean notLava = feetState.getFluidState().isEmpty() || !feetState.getFluidState().isSource();

		return feetPassable && headPassable && notLava;
	}

	private static ItemStack getEquippedCurio(LivingEntity entity, Class<? extends net.minecraft.world.item.Item> itemClass) {
		if (!(entity instanceof Player player)) return ItemStack.EMPTY;
		var opt = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);
		if (opt.isPresent()) {
			var handler = opt.get();
			for (var entry : handler.getCurios().entrySet()) {
				var stacks = entry.getValue().getStacks();
				for (int i = 0; i < stacks.getSlots(); i++) {
					ItemStack stack = stacks.getStackInSlot(i);
					if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
						return stack;
					}
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@SubscribeEvent
	public static void onLivingDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ItemStack diamondRing = getEquippedCurio(player, DiamondRingItem.class);
			if (!diamondRing.isEmpty()) {
				int tier = ((DiamondRingItem) diamondRing.getItem()).getTier();
				double chance = (tier == 1 || tier == 2) ? 0.25D : (tier == 3 || tier == 4) ? 0.50D : 0.75D;
				boolean repairedAny = false;
				for (ItemStack armor : player.getArmorSlots()) {
					if (!armor.isEmpty() && armor.isDamageableItem() && armor.getDamageValue() > 0) {
						if (player.getRandom().nextDouble() < chance) {
							armor.setDamageValue(Math.max(0, armor.getDamageValue() - 1));
							repairedAny = true;
						}
					}
				}
				if (repairedAny) {
					diamondRing.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
		if (event.getPlayer() instanceof ServerPlayer player) {
			// Desgaste de Amatista
			ItemStack amethystRing = getEquippedCurio(player, AmethystRingItem.class);
			if (!amethystRing.isEmpty()) {
				amethystRing.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
			}

			// Desgaste de Diamante por uso de herramientas
			ItemStack diamondRing = getEquippedCurio(player, DiamondRingItem.class);
			if (!diamondRing.isEmpty()) {
				ItemStack tool = player.getMainHandItem();
				if (!tool.isEmpty() && tool.isDamageableItem()) {
					diamondRing.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
					int tier = ((DiamondRingItem) diamondRing.getItem()).getTier();
					double chance = (tier == 1 || tier == 2) ? 0.25D : (tier == 3 || tier == 4) ? 0.50D : 0.75D;
					if (player.getRandom().nextDouble() < chance && tool.getDamageValue() > 0) {
						tool.setDamageValue(Math.max(0, tool.getDamageValue() - 1));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onBlockPlace(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ItemStack amethystRing = getEquippedCurio(player, AmethystRingItem.class);
			if (!amethystRing.isEmpty()) {
				amethystRing.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerAttack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ItemStack diamondRing = getEquippedCurio(player, DiamondRingItem.class);
			if (!diamondRing.isEmpty()) {
				ItemStack tool = player.getMainHandItem();
				if (!tool.isEmpty() && tool.isDamageableItem()) {
					diamondRing.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
					int tier = ((DiamondRingItem) diamondRing.getItem()).getTier();
					double chance = (tier == 1 || tier == 2) ? 0.25D : (tier == 3 || tier == 4) ? 0.50D : 0.75D;
					if (player.getRandom().nextDouble() < chance && tool.getDamageValue() > 0) {
						tool.setDamageValue(Math.max(0, tool.getDamageValue() - 1));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerXpChange(net.neoforged.neoforge.event.entity.player.PlayerXpEvent.XpChange event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ItemStack lapisRing = getEquippedCurio(player, LapisRingItem.class);
			if (!lapisRing.isEmpty()) {
				int tier = ((LapisRingItem) lapisRing.getItem()).getTier();
				int mult = (tier == 5) ? 3 : 2;
				event.setAmount(event.getAmount() * mult);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingDrops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
		if (event.getEntity() instanceof net.minecraft.world.entity.monster.Monster && event.getSource().getEntity() instanceof ServerPlayer player) {
			ItemStack emeraldRing = getEquippedCurio(player, EmeraldRingItem.class);
			if (!emeraldRing.isEmpty()) {
				int tier = ((EmeraldRingItem) emeraldRing.getItem()).getTier();
				int chance = (tier == 1 || tier == 2) ? 10 : (tier == 3 || tier == 4) ? 15 : 20;
				if (player.getRandom().nextInt(100) < chance) {
					int coinsCount = 0;
					if (tier == 1) coinsCount = player.getRandom().nextInt(2) + 1;
					else if (tier == 2 || tier == 3) coinsCount = player.getRandom().nextInt(2) + 2;
					else coinsCount = player.getRandom().nextInt(2) + 3;

					ItemStack coinsStack = new ItemStack(net.mcreator.quequeworld.init.QuequeworldModItems.Q_COIN.get(), coinsCount);
					net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
						player.level(),
						event.getEntity().getX(),
						event.getEntity().getY(),
						event.getEntity().getZ(),
						coinsStack
					);
					event.getDrops().add(itemEntity);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onDesafioPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player instanceof ServerPlayer serverPlayer && serverPlayer.getTags().contains("qqw_en_desafio")) {
			serverPlayer.getFoodData().setFoodLevel(20);
			serverPlayer.getFoodData().setSaturation(20.0f);

			// Rescate de caída al vacío en Desafío
			if (serverPlayer.getY() < serverPlayer.level().getMinBuildHeight() - 10) {
				net.mcreator.quequeworld.minigame.CheckpointManager.PlayerCheckpoint cp = 
					net.mcreator.quequeworld.minigame.CheckpointManager.getCheckpoint(serverPlayer.getUUID());

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
					net.mcreator.quequeworld.world.QueQueWorldData data = net.mcreator.quequeworld.world.QueQueWorldData.get(serverPlayer.serverLevel());
					if (data.desafioSpawnSet) {
						for (ServerLevel level : serverPlayer.getServer().getAllLevels()) {
							if (level.dimension().location().toString().equals(data.desafioSpawnDim)) {
								targetLevel = level;
								break;
							}
						}
						if (targetLevel == null) targetLevel = serverPlayer.serverLevel();
						net.minecraft.world.phys.Vec3 safe = findSafeGround(targetLevel, data.desafioSpawnX, data.desafioSpawnY, data.desafioSpawnZ, 5.0);
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
				serverPlayer.teleportTo(targetLevel, targetX, targetY, targetZ, targetYaw, targetPitch);
				playPuffEffect(targetLevel, targetX, targetY, targetZ);
				serverPlayer.displayClientMessage(Component.literal("⚠️ §cCaíste al vacío. Rescatado al Checkpoint/Spawn de desafío."), true);
			}
		}
	}


	public static void sendDelayedChallengeTitle(ServerPlayer player, String titleText, int delayTicks) {
		if (player == null || titleText == null || titleText.trim().isEmpty()) return;
		final String finalTitle = titleText.trim();
		net.mcreator.quequeworld.QuequeworldMod.queueServerWork(delayTicks, () -> {
			if (player.isAlive()) {
				player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 70, 20));
				player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("§e§l" + finalTitle)));
				player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal("§f¡Prepárate para el Desafío!")));
			}
		});
	}




	private static int performTraerlosEquipo(CommandSourceStack source, String teamArg, double radius) {
		Entity sender = source.getEntity();
		ServerLevel level = sender != null ? (ServerLevel) sender.level() : source.getLevel();
		double x = sender != null ? sender.getX() : 0;
		double y = sender != null ? sender.getY() : 0;
		double z = sender != null ? sender.getZ() : 0;

		MinecraftServer server = source.getServer();
		List<ServerPlayer> teamPlayers = new ArrayList<>();
		try {
			if (dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().isManagerLoaded()) {
				var teamManager = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager();
				var optTeam = teamManager.getTeamByName(teamArg);
				if (optTeam.isEmpty()) {
					for (dev.ftb.mods.ftbteams.api.Team t : teamManager.getTeams()) {
						if (t.getShortName().equalsIgnoreCase(teamArg)) {
							optTeam = Optional.of(t);
							break;
						}
					}
				}
				if (optTeam.isPresent()) {
					for (UUID memberUUID : optTeam.get().getMembers()) {
						ServerPlayer mp = server.getPlayerList().getPlayer(memberUUID);
						if (mp != null && !mp.getTags().contains("dios")) {
							teamPlayers.add(mp);
						}
					}
				}
			}
		} catch (Throwable ignored) {}

		if (teamPlayers.isEmpty()) {
			source.sendFailure(Component.literal("⚠️ No se encontraron jugadores conectados para el equipo FTB '" + teamArg + "'."));
			return 0;
		}

		for (ServerPlayer player : teamPlayers) {
			saveNormalPlayerPosition(player);
			net.minecraft.world.phys.Vec3 safePos = findSafeGround(level, x, y, z, radius);
			player.teleportTo(level, safePos.x, safePos.y, safePos.z, player.getYRot(), player.getXRot());
			playPuffEffect(level, safePos.x, safePos.y, safePos.z);
			player.displayClientMessage(Component.literal("§e¡Has sido traído por tu equipo (" + teamArg + ")!"), false);
		}

		source.sendSuccess(() -> Component.literal("§aTraídos " + teamPlayers.size() + " integrantes del equipo '" + teamArg + "'."), true);
		return 1;
	}


	@SubscribeEvent
	public static void onRightClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
		if (event.getLevel().isClientSide()) return;
		Player player = event.getEntity();
		if (player.getTags().contains("dios") || player.hasPermissions(2)) return;

		net.mcreator.quequeworld.world.QueQueWorldData data = net.mcreator.quequeworld.world.QueQueWorldData.get((ServerLevel) event.getLevel());
		if (data.portalesRestringidos) {
			ItemStack held = event.getItemStack();
			if (held.is(net.minecraft.world.item.Items.FLINT_AND_STEEL) || held.is(net.minecraft.world.item.Items.FIRE_CHARGE)) {
				event.setCanceled(true);
				player.displayClientMessage(Component.literal("⚠️ §cSolo los Dioses pueden abrir/crear portales en este momento."), false);
			}
		}
	}

	@SubscribeEvent
	public static void onEntityTravelToDimension(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			if (player.getTags().contains("qqw_bloqueo_portal_individual")) {
				event.setCanceled(true);
				player.displayClientMessage(Component.literal("⚠️ §cSe te ha restringido el paso por portales individualmente."), false);
			}
		}
	}

	private static int startDay(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context, int minutos) {
		net.mcreator.quequeworld.timer.TimerManager.dayTimerTicks = minutos * 60 * 20;
		net.mcreator.quequeworld.timer.TimerManager.dayTimerActive = true;
		net.mcreator.quequeworld.timer.TimerManager.dayTimerPaused = false;
		context.getSource().sendSuccess(() -> Component.literal("§aReloj del día iniciado (" + minutos + " minutos)."), true);
		net.mcreator.quequeworld.timer.TimerManager.syncToAll(context.getSource().getServer());
		return 1;
	}

	private static int showTeamBanderines(CommandSourceStack source, String teamArg) {
		try {
			if (dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().isManagerLoaded()) {
				var teamManager = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager();
				java.util.Optional<dev.ftb.mods.ftbteams.api.Team> optTeam = teamManager.getTeamByName(teamArg);
				if (optTeam.isEmpty()) {
					for (dev.ftb.mods.ftbteams.api.Team t : teamManager.getTeams()) {
						if (t.getShortName().equalsIgnoreCase(teamArg)) {
							optTeam = java.util.Optional.of(t);
							break;
						}
					}
				}
				if (optTeam.isPresent()) {
					dev.ftb.mods.ftbteams.api.Team team = optTeam.get();
					int total = 0;
					java.util.List<String> breakdown = new java.util.ArrayList<>();
					for (java.util.UUID memberId : team.getMembers()) {
						ServerPlayer mp = source.getServer().getPlayerList().getPlayer(memberId);
						if (mp != null) {
							int b = mp.getPersistentData().getInt("qqw_banderines");
							total += b;
							breakdown.add("§f" + mp.getScoreboardName() + ": §b" + b);
						}
					}
					final int finalTotal = total;
					source.sendSuccess(() -> Component.literal("§e=== Banderines del Equipo: " + team.getName().getString() + " ==="), false);
					source.sendSuccess(() -> Component.literal("§6Total del Equipo: §a" + finalTotal + " banderines"), false);
					for (String line : breakdown) {
						source.sendSuccess(() -> Component.literal(line), false);
					}
					return 1;
				} else {
					source.sendFailure(Component.literal("§cNo se encontró el equipo FTB '" + teamArg + "'."));
				}
			}
		} catch (Throwable t) {
			source.sendFailure(Component.literal("§cError al consultar FTB Teams: " + t.getMessage()));
		}
		return 0;
	}

	private static int listAllTeamsBanderines(CommandSourceStack source) {
		try {
			if (dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().isManagerLoaded()) {
				var teamManager = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager();
				record TeamScore(String name, int total, int membersOnline) {}
				java.util.List<TeamScore> scores = new java.util.ArrayList<>();
				for (dev.ftb.mods.ftbteams.api.Team team : teamManager.getTeams()) {
					int total = 0;
					int online = 0;
					for (java.util.UUID memberId : team.getMembers()) {
						ServerPlayer mp = source.getServer().getPlayerList().getPlayer(memberId);
						if (mp != null) {
							total += mp.getPersistentData().getInt("qqw_banderines");
							online++;
						}
					}
					if (online > 0) {
						scores.add(new TeamScore(team.getName().getString(), total, online));
					}
				}
				scores.sort((a, b) -> Integer.compare(b.total(), a.total()));
				source.sendSuccess(() -> Component.literal("§e=== Ranking de Banderines por Equipo ==="), false);
				int rank = 1;
				for (TeamScore score : scores) {
					final int r = rank++;
					source.sendSuccess(() -> Component.literal("§a" + r + "º " + score.name() + " §7(" + score.membersOnline() + " jug.) §f- §b" + score.total() + " banderines"), false);
				}
				return 1;
			}
		} catch (Throwable t) {
			source.sendFailure(Component.literal("§cError al consultar FTB Teams: " + t.getMessage()));
		}
		return 0;
	}

}