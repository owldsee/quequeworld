package net.mcreator.quequeworld.event;

import net.mcreator.quequeworld.QuequeworldMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import java.util.List;

@EventBusSubscriber(modid = QuequeworldMod.MODID)
public class SpecterEventHandler {

    public static boolean isSpecter(LivingEntity entity) {
        if (entity == null) return false;
        return entity.getTags().contains("fantasma");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. TICK DE JUGADOR: VUELO (>6 HAMBRE), REGENERACIÓN PASIVA Y MANO ÚNICA
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) return;

        if (isSpecter(player)) {
            // Neutralizar agotamiento pasivo natural
            player.getFoodData().setExhaustion(0.0f);

            int foodLevel = player.getFoodData().getFoodLevel();
            long gameTime = player.level().getGameTime();

            // A) REGENERACIÓN PASIVA LENTA DE HAMBRE (+1 de foodLevel [0.5 iconos] cada 30 segundos / 600 ticks)
            if (gameTime % 600 == 0 && foodLevel < 20 && !player.getAbilities().flying) {
                player.getFoodData().setFoodLevel(Math.min(20, foodLevel + 1));
                foodLevel = player.getFoodData().getFoodLevel();
            }

            // B) VUELO ESPECTRAL (REQUISITO: >6 DE HAMBRE, COSTO 0.05/s)
            if (foodLevel > 6) {
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
                // Volar consume 1 de hambre cada 400 ticks (20s) -> 0.05 de hambre por segundo
                if (player.getAbilities().flying && gameTime % 400 == 0) {
                    player.getFoodData().setFoodLevel(Math.max(0, foodLevel - 1));
                }
            } else {
                if (player.getAbilities().mayfly || player.getAbilities().flying) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                    player.displayClientMessage(
                        Component.literal("§c[Fantasma] Necesitas más de 6 de hambre para poder volar.").withStyle(ChatFormatting.RED),
                        true
                    );
                }
            }

            // C) MANO ÚNICA: MÁXIMO 1 ÍTEM EN MAINHAND Y DROPEAR TODO LO DEMÁS
            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getCount() > 1) {
                ItemStack excess = mainHand.split(mainHand.getCount() - 1);
                player.drop(excess, false);
            }

            int selectedSlot = player.getInventory().selected;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                if (slot != selectedSlot) {
                    ItemStack extraStack = player.getInventory().getItem(slot);
                    if (!extraStack.isEmpty()) {
                        player.getInventory().setItem(slot, ItemStack.EMPTY);
                        player.drop(extraStack, false);
                    }
                }
            }

            // Expulsar también la mano secundaria (offhand)
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty()) {
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                player.drop(offhandStack, false);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. CURACIÓN DE ALIADOS Y REVELAR
    // ─────────────────────────────────────────────────────────────────────────
    
    private static void performHeal(Player player, Player targetPlayer) {
        int foodLevel = player.getFoodData().getFoodLevel();
        if (foodLevel < 2) {
            player.displayClientMessage(
                Component.literal("§c[Fantasma] Necesitas al menos 2 de hambre para curar a un aliado.").withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        if (targetPlayer.getHealth() < targetPlayer.getMaxHealth()) {
            targetPlayer.heal(2.0f); // Curar 1 Corazón (2 puntos de vida)
            player.getFoodData().setFoodLevel(Math.max(0, foodLevel - 2));

            player.displayClientMessage(
                Component.literal("§a[Curación] Has restaurado 1 corazón a " + targetPlayer.getScoreboardName() + ".").withStyle(ChatFormatting.GREEN),
                true
            );
            targetPlayer.displayClientMessage(
                Component.literal("§a💚 ¡Un Fantasma Auxiliar te ha curado 1 corazón!").withStyle(ChatFormatting.GREEN),
                true
            );
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player != null && isSpecter(player)) {
            event.setCanceled(true); // El fantasma no puede infligir daño con ataques
            if (event.getTarget() instanceof Player targetPlayer && !isSpecter(targetPlayer)) {
                if (!player.level().isClientSide()) {
                    performHeal(player, targetPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player != null && isSpecter(player) && event.getHand() == InteractionHand.MAIN_HAND) {
            if (event.getTarget() instanceof Player targetPlayer && !isSpecter(targetPlayer)) {
                event.setCanceled(true);
                if (!player.level().isClientSide()) {
                    performHeal(player, targetPlayer);
                }
            }
        }
    }

    private static void performReveal(Player player) {
        int foodLevel = player.getFoodData().getFoodLevel();
        if (foodLevel < 8) { // 4 muslos = 8 de hambre
            player.displayClientMessage(
                Component.literal("§c[Fantasma] Necesitas 4 muslos (8 de hambre) para usar Revelar.").withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        player.getFoodData().setFoodLevel(Math.max(0, foodLevel - 8));
        
        AABB area = new AABB(player.blockPosition()).inflate(30);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, area, e -> !isSpecter(e));
        
        for (LivingEntity e : entities) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
        }

        player.displayClientMessage(
            Component.literal("§b[Fantasma] ¡Pulso revelador activado! Entidades cercanas marcadas por 10 segundos.").withStyle(ChatFormatting.AQUA),
            true
        );
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player != null && isSpecter(player) && event.getHand() == InteractionHand.MAIN_HAND) {
            if (player.isShiftKeyDown()) { // Solo al agacharse y dar click derecho a un bloque para no molestar siempre
                if (!player.level().isClientSide()) {
                    performReveal(player);
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player != null && isSpecter(player) && event.getHand() == InteractionHand.MAIN_HAND) {
            if (player.isShiftKeyDown()) { 
                if (!player.level().isClientSide()) {
                    performReveal(player);
                }
                event.setCanceled(true);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. INMORTALIDAD TOTAL DEL FANTASMA (NO PUEDE MORIR NI RECIBIR DAÑO)
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (isSpecter(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (isSpecter(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. REVERSIÓN SÍNCRONA DE ARMADURAS
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isSpecter(player)) {
            EquipmentSlot slot = event.getSlot();
            if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
                ItemStack newStack = event.getTo();
                if (!newStack.isEmpty()) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    player.drop(newStack, false);
                    player.displayClientMessage(
                        Component.literal("§c[Fantasma] Los fantasmas no pueden llevar armadura.").withStyle(ChatFormatting.RED),
                        true
                    );
                    player.containerMenu.sendAllDataToRemote();
                    player.inventoryMenu.sendAllDataToRemote();
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. IGNORADO NATURAL DE MOBS (ESTILO MODO CREATIVO)
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof Player player && isSpecter(player)) {
            event.setCanceled(true);
            event.setNewAboutToBeSetTarget(null);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. PICAR BLOQUES SOLO A MANO DESNUDA
    // ─────────────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player != null && isSpecter(player)) {
            if (!player.getMainHandItem().isEmpty()) {
                event.setNewSpeed(0.0f);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player != null && isSpecter(player)) {
            if (!player.getMainHandItem().isEmpty()) {
                event.setCanceled(true);
                player.displayClientMessage(
                    Component.literal("§c[Fantasma] Solo puedes picar bloques a mano desnuda.").withStyle(ChatFormatting.RED),
                    true
                );
            }
        }
    }
}
