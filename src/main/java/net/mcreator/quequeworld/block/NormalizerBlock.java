package net.mcreator.quequeworld.block;

import net.mcreator.quequeworld.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class NormalizerBlock extends Block {
	public NormalizerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		super.stepOn(level, pos, state, entity);
		if (!level.isClientSide() && entity instanceof LivingEntity living) {
			boolean removed = false;
			if (living.hasEffect(ModEffects.REDUCIDO)) {
				living.removeEffect(ModEffects.REDUCIDO);
				removed = true;
			}
			if (living.hasEffect(ModEffects.GIGANTE)) {
				living.removeEffect(ModEffects.GIGANTE);
				removed = true;
			}
		}
	}
}
