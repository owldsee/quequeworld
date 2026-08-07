package net.mcreator.quequeworld.block.entity;

import net.mcreator.quequeworld.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GoalBellBlockEntity extends BellBlockEntity {

    public GoalBellBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.GOAL_BELL_BE.get();
    }
}
