package net.mcreator.quequeworld.block;

import net.mcreator.quequeworld.block.entity.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TerminalBlock extends BaseEntityBlock {
    public static final com.mojang.serialization.MapCodec<TerminalBlock> CODEC = simpleCodec(TerminalBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public TerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(this.asItem())) {
            if (!level.isClientSide()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TerminalBlockEntity terminal) {
                    player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (containerId, playerInventory, p) -> new net.mcreator.quequeworld.world.inventory.TerminalConfigMenu(containerId, playerInventory, terminal),
                        Component.literal("Configuración de Terminal")
                    ), pos);
                }
            }
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            ItemStack heldItem = player.getMainHandItem();
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TerminalBlockEntity terminal) {
                if (heldItem.is(this.asItem())) {
                    player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (containerId, playerInventory, p) -> new net.mcreator.quequeworld.world.inventory.TerminalConfigMenu(containerId, playerInventory, terminal),
                        Component.literal("Configuración de Terminal")
                    ), pos);
                } else {
                    player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (containerId, playerInventory, p) -> new net.mcreator.quequeworld.world.inventory.TerminalInteractMenu(containerId, playerInventory, terminal),
                        Component.literal("Terminal de Entrada")
                    ), pos);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TerminalBlockEntity(pos, state);
    }
}
