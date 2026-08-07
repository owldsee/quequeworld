package net.mcreator.quequeworld.event;

import net.mcreator.quequeworld.QuequeworldMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingVisibilityEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * Maneja la dificultad dinámica por Nivel de Peligro (dangerLevel) y el consumo extra de crafteo en herrería.
 */
@EventBusSubscriber(modid = QuequeworldMod.MODID)
public class DifficultyScalingEventHandler {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. ESCALADO DE DAÑO RECIBIDO POR MOBS
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        Entity directAttacker = source.getDirectEntity();

        boolean isMobDamage = (attacker instanceof Mob) || (directAttacker instanceof Mob)
                || (attacker instanceof Enemy) || (directAttacker instanceof Enemy)
                || (attacker instanceof Monster) || (directAttacker instanceof Monster);

        if (attacker instanceof Player || directAttacker instanceof Player) {
            isMobDamage = false;
        }

        if (!isMobDamage) {
            return;
        }

        double dangerLevel = SoulShieldEventHandler.getDangerLevel(player);
        float baseAmount = event.getAmount();

        float multiplier = (float) (1.0D + (dangerLevel - 0.50D) * 1.0D);
        multiplier = Math.max(0.5f, Math.min(2.0f, multiplier));

        if (dangerLevel > 0.75D) {
            double prob = (dangerLevel - 0.75D) * 0.8D + 0.15D;
            if (player.getRandom().nextDouble() < prob) {
                if (player.getRandom().nextBoolean()) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, true));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true));
                }
            }
        }

        event.setAmount(baseAmount * multiplier);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. PERCEPCIÓN Y VISIBILIDAD DE MOBS (AGRO DISTANCE)
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onLivingVisibility(LivingVisibilityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        double dangerLevel = SoulShieldEventHandler.getDangerLevel(player);

        double visibilityMult = 1.0D + (dangerLevel - 0.50D) * 1.0D;
        visibilityMult = Math.max(0.5D, Math.min(2.0D, visibilityMult));

        event.modifyVisibility(visibilityMult);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. PRIORIDAD DE SELECCIÓN DE OBJETIVOS (MOB TARGET PRIORITY)
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob.level().isClientSide()) return;

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (!(newTarget instanceof ServerPlayer currentTargetPlayer)) return;

        double currentDanger = SoulShieldEventHandler.getDangerLevel(currentTargetPlayer);

        double searchRadius = 16.0D;
        AABB area = mob.getBoundingBox().inflate(searchRadius);
        List<ServerPlayer> nearbyPlayers = mob.level().getEntitiesOfClass(ServerPlayer.class, area,
                p -> p.isAlive() && !p.isSpectator() && mob.hasLineOfSight(p));

        if (nearbyPlayers.size() <= 1) return;

        ServerPlayer highestDangerPlayer = currentTargetPlayer;
        double highestDanger = currentDanger;

        for (ServerPlayer p : nearbyPlayers) {
            double pDanger = SoulShieldEventHandler.getDangerLevel(p);
            if (pDanger > highestDanger) {
                highestDanger = pDanger;
                highestDangerPlayer = p;
            }
        }

        if (highestDangerPlayer != currentTargetPlayer && highestDanger > currentDanger + 0.05D) {
            double dangerDiff = highestDanger - currentDanger;
            double redirectProb = Math.min(0.75D, 0.25D + dangerDiff * 1.0D);

            if (mob.getRandom().nextDouble() < redirectProb) {
                event.setNewAboutToBeSetTarget(highestDangerPlayer);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. TICK HANDLER (AGOTAMIENTO, MOBS VELOCES, BENDICIÓN DE GRACIA)
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        if (player.tickCount % 10 != 0) return;

        double dangerLevel = SoulShieldEventHandler.getDangerLevel(player);

        if (dangerLevel > 0.50D) {
            float extraExhaustion = (float) ((dangerLevel - 0.50D) * 0.015D);
            player.getFoodData().addExhaustion(extraExhaustion);
        } else if (dangerLevel < 0.50D) {
            if (dangerLevel < 0.30D && player.getFoodData().getFoodLevel() >= 18 && player.getHealth() < player.getMaxHealth()) {
                player.heal(0.5f);
            }
        }

        if (dangerLevel < 0.30D && player.getHealth() <= 6.0f) {
            long lastGrace = player.getPersistentData().getLong("qqw_grace_cooldown");
            long now = player.level().getGameTime();

            if (now - lastGrace >= 1800L || lastGrace == 0L) {
                player.getPersistentData().putLong("qqw_grace_cooldown", now);

                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0, false, true));

                if (player.level() instanceof ServerLevel sl) {
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8f, 1.2f);
                    sl.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.3, 0.5, 0.3, 0.05);
                }
            }
        }

        if (dangerLevel > 0.60D) {
            AABB mobArea = player.getBoundingBox().inflate(12.0D);
            List<Mob> chasingMobs = player.level().getEntitiesOfClass(Mob.class, mobArea,
                    m -> m.getTarget() == player && m.isAlive());

            for (Mob mob : chasingMobs) {
                if (!mob.hasEffect(MobEffects.MOVEMENT_SPEED)) {
                    int speedAmp = dangerLevel > 0.85D ? 1 : 0;
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, speedAmp, false, false));
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. MOBS ÉLITE AL APARECER (> 80% PELIGRO)
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob.level().isClientSide()) return;

        // Interceptación y prevención de apariciones naturales en la dimensión qqw:desafio
        String dimPath = mob.level().dimension().location().getPath();
        if (dimPath.equalsIgnoreCase("desafio")) {
            net.minecraft.world.entity.MobSpawnType spawnType = event.getSpawnType();
            if (spawnType == net.minecraft.world.entity.MobSpawnType.NATURAL ||
                spawnType == net.minecraft.world.entity.MobSpawnType.CHUNK_GENERATION ||
                spawnType == net.minecraft.world.entity.MobSpawnType.PATROL ||
                spawnType == net.minecraft.world.entity.MobSpawnType.STRUCTURE ||
                spawnType == net.minecraft.world.entity.MobSpawnType.REINFORCEMENT ||
                spawnType == net.minecraft.world.entity.MobSpawnType.EVENT) {
                event.setCanceled(true);
                return;
            }
        }

        if (!(mob instanceof Enemy || mob instanceof Monster)) return;

        AABB playerArea = mob.getBoundingBox().inflate(32.0D);
        List<ServerPlayer> nearbyDangerPlayers = mob.level().getEntitiesOfClass(ServerPlayer.class, playerArea,
                p -> p.isAlive() && SoulShieldEventHandler.getDangerLevel(p) > 0.80D);

        if (!nearbyDangerPlayers.isEmpty()) {
            if (mob.getRandom().nextDouble() < 0.30D) {
                mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999, 0, false, true));
                mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 0, false, true));

                if (mob.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, mob.getX(), mob.getY() + 1.0, mob.getZ(), 3, 0.2, 0.3, 0.2, 0.0);
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. CONSUMO ADICIONAL DE MATERIALES EN HERRERÍA PARA ANILLOS
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;

        int requiredTotal = 0;
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(crafted.getItem());
        if (id != null && id.getNamespace().equals("quequeworld")) {
            String path = id.getPath();
            if (path.endsWith("_2")) requiredTotal = 2;
            else if (path.endsWith("_3")) requiredTotal = 3;
            else if (path.endsWith("_4")) requiredTotal = 4;
            else if (path.endsWith("_5")) requiredTotal = 5;
        }

        if (requiredTotal > 1) {
            Container matrix = event.getInventory();
            if (matrix != null && matrix.getContainerSize() >= 3) {
                ItemStack addition = matrix.getItem(2);
                if (!addition.isEmpty()) {
                    int extraToShrink = requiredTotal - 1;
                    addition.shrink(extraToShrink);
                }
            }
        }
    }
}
