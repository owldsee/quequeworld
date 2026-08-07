package net.mcreator.quequeworld.world.inventory;

import net.mcreator.quequeworld.init.ModMenus;
import net.mcreator.quequeworld.init.ModBlocks;
import net.mcreator.quequeworld.block.entity.GachaMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GachaMachineMenu extends AbstractContainerMenu {
	private final GachaMachineBlockEntity blockEntity;
	private final ContainerLevelAccess access;

	// Constructor para el cliente (inicializado vía red)
	public GachaMachineMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
	}

	// Constructor principal usado por el servidor
	public GachaMachineMenu(int containerId, Inventory playerInventory, GachaMachineBlockEntity blockEntity) {
		super(ModMenus.GACHA_MACHINE_MENU.get(), containerId);
		this.blockEntity = blockEntity;
		this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

		// 1. Agregar las ranuras de premios de la máquina (Slots 0 a 25)
		// Las organizamos en una cuadrícula de 3 filas de 9 columnas, omitiendo la última ranura (26)
		int slotIndex = 0;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				if (slotIndex < 26) {
					this.addSlot(new SlotItemHandler(blockEntity.getInventory(), slotIndex, 8 + col * 18, 18 + row * 18));
					slotIndex++;
				}
			}
		}

		// 2. Agregar el inventario del jugador (Fila 0-2 de la mochila)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		// 3. Agregar la barra de acceso rápido del jugador (Hotbar)
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}
	}

	private static GachaMachineBlockEntity getBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		BlockPos pos = extraData.readBlockPos();
		BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
		if (be instanceof GachaMachineBlockEntity gachaBE) {
			return gachaBE;
		}
		throw new IllegalStateException("Block entity at " + pos + " is not a GachaMachineBlockEntity!");
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, ModBlocks.GACHA_MACHINE.get());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			
			// Si el clic es en las ranuras de la máquina (0 a 25)
			if (index < 26) {
				// Mover al inventario del jugador
				if (!this.moveItemStackTo(itemstack1, 26, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				// Clic en el inventario del jugador -> Mover a las ranuras de la máquina
				if (!this.moveItemStackTo(itemstack1, 0, 26, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return itemstack;
	}

	public GachaMachineBlockEntity getBlockEntity() {
		return this.blockEntity;
	}
}
