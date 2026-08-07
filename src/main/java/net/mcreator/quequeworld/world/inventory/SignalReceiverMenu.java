package net.mcreator.quequeworld.world.inventory;

import net.mcreator.quequeworld.init.ModMenus;
import net.mcreator.quequeworld.init.ModBlocks;
import net.mcreator.quequeworld.block.entity.SignalReceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SignalReceiverMenu extends AbstractContainerMenu {
    private final SignalReceiverBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public SignalReceiverMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public SignalReceiverMenu(int containerId, Inventory playerInventory, SignalReceiverBlockEntity blockEntity) {
        super(ModMenus.SIGNAL_RECEIVER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    private static SignalReceiverBlockEntity getBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof SignalReceiverBlockEntity receiverBE) {
            return receiverBE;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not a SignalReceiverBlockEntity!");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.SIGNAL_RECEIVER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public SignalReceiverBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
