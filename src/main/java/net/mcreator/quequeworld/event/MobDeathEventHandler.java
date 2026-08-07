package net.mcreator.quequeworld.event;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.config.QueQueDifficultyConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Mecánicas de muerte de mobs y jugadores:
 *   1. Zombie muere → 30% chance de spawnear un Esqueleto sin arco con +10% velocidad.
 *   2. Jugador muere (sin escudo) → Zombie con el nametag del jugador.
 *
 * Ambas mecánicas son configurables desde quequeworld_difficulty.json → mechanics
 * y recargables en caliente con /qqw reload.
 */
@EventBusSubscriber(modid = QuequeworldMod.MODID)
public class MobDeathEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        // 1. Zombie muere → Spawn Esqueleto (30%)
        if (entity instanceof Zombie zombie) {
            handleZombieDeath(zombie);
        }

        // 2. Jugador muere → Spawn Zombie con nametag
        if (entity instanceof ServerPlayer player) {
            handlePlayerDeath(player);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ZOMBIE MUERE → ESQUELETO SIN ARCO CON +10% VELOCIDAD
    // ─────────────────────────────────────────────────────────────────────────
    private static void handleZombieDeath(Zombie zombie) {
        QueQueDifficultyConfig.Mechanics mech = QueQueDifficultyConfig.instance.mechanics;
        if (mech == null || !mech.zombie_death_spawns_skeleton) return;

        if (zombie.getRandom().nextDouble() >= mech.zombie_death_skeleton_chance) return;

        ServerLevel level = (ServerLevel) zombie.level();

        Skeleton skeleton = EntityType.SKELETON.create(level);
        if (skeleton == null) return;

        // Posicionar en la ubicación del zombie muerto
        skeleton.moveTo(zombie.getX(), zombie.getY(), zombie.getZ(), zombie.getYRot(), 0.0F);

        // Quitar el arco: mano vacía y sin drop chance
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        skeleton.setDropChance(EquipmentSlot.MAINHAND, 0.0F);

        // +10% velocidad (o lo que diga la config)
        if (skeleton.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            skeleton.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("quequeworld", "zombie_death_speed_bonus"),
                    mech.skeleton_speed_bonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
            );
        }

        // No despawnear
        skeleton.setPersistenceRequired();

        // Agregar al mundo
        level.addFreshEntity(skeleton);

        // Efecto visual: partículas de humo en la posición del zombie
        level.sendParticles(ParticleTypes.SMOKE, zombie.getX(), zombie.getY() + 0.5, zombie.getZ(), 15, 0.3, 0.5, 0.3, 0.02);
        level.sendParticles(ParticleTypes.SOUL, zombie.getX(), zombie.getY() + 0.5, zombie.getZ(), 5, 0.2, 0.3, 0.2, 0.01);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JUGADOR MUERE → ZOMBIE CON NAMETAG DEL JUGADOR
    // ─────────────────────────────────────────────────────────────────────────
    private static void handlePlayerDeath(ServerPlayer player) {
        QueQueDifficultyConfig.Mechanics mech = QueQueDifficultyConfig.instance.mechanics;
        if (mech == null || !mech.player_death_spawns_zombie) return;

        // NUNCA aplica si la mecánica de morir está desactivada para este jugador
        if (player.getTags().contains("qqw_mecanica_morir_disabled")) return;

        // Por defecto, solo aplica si el jugador NO tiene escudo de alma
        boolean hasShield = player.getTags().contains("tiene_escudo");
        if (hasShield && !mech.player_death_zombie_on_all_deaths) return;

        ServerLevel level = player.serverLevel();

        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) return;

        // Posicionar en la ubicación de muerte del jugador
        zombie.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);

        // Nametag con el nombre del jugador, siempre visible
        zombie.setCustomName(Component.literal(player.getScoreboardName()));
        zombie.setCustomNameVisible(true);

        // No despawnear
        zombie.setPersistenceRequired();

        // Agregar al mundo
        level.addFreshEntity(zombie);

        // Efecto visual: partículas ominosas
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.5, player.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.5, player.getZ(), 8, 0.2, 0.4, 0.2, 0.01);
    }
}
