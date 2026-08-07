package net.mcreator.quequeworld.block;

import net.mcreator.quequeworld.minigame.GoalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GoalTriggerBlock extends Block {

    public GoalTriggerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context.isHoldingItem(this.asItem()) ? Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D) : Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return player.isCreative() ? 1.0F : 0.0F;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) {
            Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 16.0D, false);
            if (player != null && (player.getMainHandItem().is(this.asItem()) || player.getOffhandItem().is(this.asItem()))) {
                for (int i = 0; i < 3; i++) {
                    double px = pos.getX() + 0.1D + random.nextDouble() * 0.8D;
                    double py = pos.getY() + 0.1D + random.nextDouble() * 0.8D;
                    double pz = pos.getZ() + 0.1D + random.nextDouble() * 0.8D;
                    level.addParticle(ParticleTypes.GLOW, px, py, pz, 0.0D, 0.02D, 0.0D);
                    level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            GoalManager.handleGoalReach(player, level, "Bloque de Meta");
        }
    }
}
