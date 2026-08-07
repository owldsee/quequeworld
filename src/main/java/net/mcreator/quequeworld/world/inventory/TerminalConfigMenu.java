package net.mcreator.quequeworld.world.inventory;

import net.mcreator.quequeworld.init.ModMenus;
import net.mcreator.quequeworld.init.ModBlocks;
import net.mcreator.quequeworld.block.entity.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TerminalConfigMenu extends AbstractContainerMenu {
    private final TerminalBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public TerminalConfigMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public TerminalConfigMenu(int containerId, Inventory playerInventory, TerminalBlockEntity blockEntity) {
        super(ModMenus.TERMINAL_CONFIG_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    private static TerminalBlockEntity getBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof TerminalBlockEntity terminalBE) {
            return terminalBE;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not a TerminalBlockEntity!");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.TERMINAL_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public TerminalBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
