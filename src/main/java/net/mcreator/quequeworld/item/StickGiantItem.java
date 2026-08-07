package net.mcreator.quequeworld.item;

import net.mcreator.quequeworld.init.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StickGiantItem extends Item {
	public StickGiantItem() {
		super(new Item.Properties().stacksTo(1));
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		Level level = player.level();
		if (!level.isClientSide()) {
			if (target.hasEffect(ModEffects.GIGANTE)) {
				target.removeEffect(ModEffects.GIGANTE);
				player.displayClientMessage(Component.literal("§aEfecto gigante removido de " + target.getName().getString()), true);
			} else {
				// Remover reducido si lo tiene
				if (target.hasEffect(ModEffects.REDUCIDO)) {
					target.removeEffect(ModEffects.REDUCIDO);
				}
				target.addEffect(new MobEffectInstance(ModEffects.GIGANTE, MobEffectInstance.INFINITE_DURATION, 0, false, false, false));
				player.displayClientMessage(Component.literal("§eEfecto gigante aplicado a " + target.getName().getString()), true);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			if (player.hasEffect(ModEffects.GIGANTE)) {
				player.removeEffect(ModEffects.GIGANTE);
				player.displayClientMessage(Component.literal("§aTe has normalizado."), true);
			} else {
				// Remover reducido si lo tiene
				if (player.hasEffect(ModEffects.REDUCIDO)) {
					player.removeEffect(ModEffects.REDUCIDO);
				}
				player.addEffect(new MobEffectInstance(ModEffects.GIGANTE, MobEffectInstance.INFINITE_DURATION, 0, false, false, false));
				player.displayClientMessage(Component.literal("§eTe has agrandado."), true);
			}
		}
		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
	}
}
