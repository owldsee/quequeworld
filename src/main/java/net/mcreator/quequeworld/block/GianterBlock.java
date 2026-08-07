package net.mcreator.quequeworld.block;

import net.mcreator.quequeworld.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GianterBlock extends Block {
	public GianterBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		super.stepOn(level, pos, state, entity);
		if (!level.isClientSide() && entity instanceof LivingEntity living) {
			if (!living.hasEffect(ModEffects.GIGANTE)) {
				if (living.hasEffect(ModEffects.REDUCIDO)) {
					living.removeEffect(ModEffects.REDUCIDO);
				}
				living.addEffect(new MobEffectInstance(ModEffects.GIGANTE, MobEffectInstance.INFINITE_DURATION, 0, false, false, false));
			}
		}
	}
}
