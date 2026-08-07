package net.mcreator.quequeworld.block;

import net.mcreator.quequeworld.block.entity.GoalBellBlockEntity;
import net.mcreator.quequeworld.minigame.GoalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class GoalBellBlock extends BellBlock implements EntityBlock {

    public GoalBellBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoalBellBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(this.asItem()) || player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                GoalManager.isTeamMode = !GoalManager.isTeamMode;
                String modeName = GoalManager.isTeamMode ? "EQUIPO (FTB Teams)" : "INDIVIDUAL";
                player.displayClientMessage(Component.literal("🔔 §b[META] Modo cambiado a: §e" + modeName), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        return handleBellInteraction(state, level, pos, player, hitResult) ? ItemInteractionResult.sidedSuccess(level.isClientSide()) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return handleBellInteraction(state, level, pos, player, hitResult) ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
    }

    private boolean handleBellInteraction(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        this.attemptToRing(player, level, pos, hitResult.getDirection());
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            GoalManager.handleGoalReach(sp, level, "Campana de Meta");
        }
        return true;
    }
}
