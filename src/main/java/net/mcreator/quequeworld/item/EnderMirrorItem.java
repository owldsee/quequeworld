package net.mcreator.quequeworld.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class EnderMirrorItem extends Item {
	public EnderMirrorItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}

		// Si ya está canalizando, no hacer nada
		if (player.getPersistentData().contains("warp_cast_timer")) {
			return InteractionResultHolder.fail(stack);
		}

		// Iniciar canalización
		player.getPersistentData().putInt("warp_cast_timer", 60); // 3 segundos = 60 ticks
		player.getPersistentData().putDouble("warp_cast_health", player.getHealth());

		// Aplicar lentitud extrema (Slowness IV = amplificador 3, oculto del HUD)
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, false, false, false));

		// Efecto visual y de sonido al comenzar
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
			SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.5F);

		return InteractionResultHolder.success(stack);
	}
}
