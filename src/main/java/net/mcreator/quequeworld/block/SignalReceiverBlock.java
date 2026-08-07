package net.mcreator.quequeworld.block;

import net.mcreator.quequeworld.block.entity.SignalReceiverBlockEntity;
import net.mcreator.quequeworld.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SignalReceiverBlock extends BaseEntityBlock {
    public static final com.mojang.serialization.MapCodec<SignalReceiverBlock> CODEC = simpleCodec(SignalReceiverBlock::new);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public SignalReceiverBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (player.hasPermissions(2) || player.getTags().contains("dios") || player.isCreative()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SignalReceiverBlockEntity receiver) {
                    player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (containerId, playerInventory, p) -> new net.mcreator.quequeworld.world.inventory.SignalReceiverMenu(containerId, playerInventory, receiver),
                        Component.literal("Configuración de Receptor")
                    ), pos);
                }
            } else {
                player.displayClientMessage(Component.literal("§c[Receptor] Solo los administradores pueden configurar este bloque."), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SignalReceiverBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.SIGNAL_RECEIVER_BE.get(), SignalReceiverBlockEntity::serverTick);
    }
}
