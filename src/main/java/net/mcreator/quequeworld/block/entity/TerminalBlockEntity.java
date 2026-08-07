package net.mcreator.quequeworld.block.entity;

import net.mcreator.quequeworld.init.ModBlockEntities;
import net.mcreator.quequeworld.signal.SignalChannelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TerminalBlockEntity extends BlockEntity {

    private String expectedWord = "";
    private String emitChannelSignal = "";

    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TERMINAL_BE.get(), pos, state);
    }

    public void setConfig(String expectedWord, String emitChannelSignal) {
        this.expectedWord = expectedWord != null ? expectedWord.trim() : "";
        this.emitChannelSignal = emitChannelSignal != null ? emitChannelSignal.trim() : "";
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public String getExpectedWord() {
        return expectedWord;
    }

    public String getEmitChannelSignal() {
        return emitChannelSignal;
    }

    public boolean submitWord(Player player, String word) {
        if (level == null || level.isClientSide()) return false;
        if (!expectedWord.isEmpty() && expectedWord.equalsIgnoreCase(word.trim())) {
            if (!emitChannelSignal.isEmpty()) {
                SignalChannelManager.emitSignal(level, emitChannelSignal);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("ExpectedWord", expectedWord);
        tag.putString("EmitChannelSignal", emitChannelSignal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.expectedWord = tag.getString("ExpectedWord");
        this.emitChannelSignal = tag.getString("EmitChannelSignal");
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
