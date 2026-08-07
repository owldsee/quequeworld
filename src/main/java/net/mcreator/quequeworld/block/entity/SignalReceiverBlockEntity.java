package net.mcreator.quequeworld.block.entity;

import net.mcreator.quequeworld.init.ModBlockEntities;
import net.mcreator.quequeworld.signal.SignalChannelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SignalReceiverBlockEntity extends BlockEntity implements SignalChannelManager.ISignalListener {

    private String listenChannel = "";
    private String listenSignal = "";
    private int activeTicks = 0;

    public SignalReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SIGNAL_RECEIVER_BE.get(), pos, state);
    }

    public void setChannelAndSignal(String channel, String signal) {
        this.listenChannel = channel != null ? channel.trim() : "";
        this.listenSignal = signal != null ? signal.trim() : "";
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public String getListenChannel() {
        return listenChannel;
    }

    public String getListenSignal() {
        return listenSignal;
    }

    public String getFullChannelSignal() {
        if (listenChannel.isEmpty()) return "";
        return listenChannel + ":" + listenSignal;
    }

    public boolean isPowered() {
        return activeTicks > 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SignalReceiverBlockEntity entity) {
        if (entity.activeTicks > 0) {
            entity.activeTicks--;
            if (entity.activeTicks == 0) {
                level.setBlock(pos, state.setValue(net.mcreator.quequeworld.block.SignalReceiverBlock.POWERED, false), 3);
                level.updateNeighborsAt(pos, state.getBlock());
            }
        }
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public BlockPos getListenerPos() {
        return worldPosition;
    }

    @Override
    public void onSignalReceived(String channel, String signal) {
        if (level == null || level.isClientSide()) return;
        if (!listenChannel.isEmpty() && listenChannel.equalsIgnoreCase(channel) && listenSignal.equalsIgnoreCase(signal)) {
            activeTicks = 20; // 1 segundo (20 ticks) de señal de redstone
            BlockState state = getBlockState();
            if (!state.getValue(net.mcreator.quequeworld.block.SignalReceiverBlock.POWERED)) {
                level.setBlock(worldPosition, state.setValue(net.mcreator.quequeworld.block.SignalReceiverBlock.POWERED, true), 3);
                level.updateNeighborsAt(worldPosition, state.getBlock());
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            SignalChannelManager.registerListener(this);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {
            SignalChannelManager.unregisterListener(this);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("ListenChannel", listenChannel);
        tag.putString("ListenSignal", listenSignal);
        tag.putInt("ActiveTicks", activeTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.listenChannel = tag.getString("ListenChannel");
        this.listenSignal = tag.getString("ListenSignal");
        this.activeTicks = tag.getInt("ActiveTicks");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
