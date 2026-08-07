package net.mcreator.quequeworld.world.inventory;

import net.mcreator.quequeworld.init.ModMenus;
import net.mcreator.quequeworld.init.ModBlocks;
import net.mcreator.quequeworld.block.entity.MinigameSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class MinigameSpawnerMenu extends AbstractContainerMenu {
	private final MinigameSpawnerBlockEntity blockEntity;
	private final ContainerLevelAccess access;
	private final DataSlot frequencyDataSlot = new DataSlot() {
		@Override
		public int get() {
			return blockEntity.getFrequencyTicks();
		}

		@Override
		public void set(int value) {
			blockEntity.setFrequencyTicks(value);
		}
	};
	private final DataSlot infiniteModeDataSlot = new DataSlot() {
		@Override
		public int get() {
			return blockEntity.isInfiniteMode() ? 1 : 0;
		}

		@Override
		public void set(int value) {
			blockEntity.setInfiniteMode(value == 1);
		}
	};

	public MinigameSpawnerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
	}

	public MinigameSpawnerMenu(int containerId, Inventory playerInventory, MinigameSpawnerBlockEntity blockEntity) {
		super(ModMenus.MINIGAME_SPAWNER_MENU.get(), containerId);
		this.blockEntity = blockEntity;
		this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

		this.addDataSlot(this.frequencyDataSlot);
		this.addDataSlot(this.infiniteModeDataSlot);

		// 1. 54 ranuras del generador (6 filas de 9 columnas)
		int slotIndex = 0;
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new SlotItemHandler(blockEntity.getInventory(), slotIndex++, 8 + col * 18, 18 + row * 18));
			}
		}

		// 2. Inventario del jugador (Fila 0-2 de la mochila, Y = 140)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
			}
		}

		// 3. Hotbar del jugador (Y = 198)
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
		}
	}

	private static MinigameSpawnerBlockEntity getBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		BlockPos pos = extraData.readBlockPos();
		BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
		if (be instanceof MinigameSpawnerBlockEntity spawnerBE) {
			return spawnerBE;
		}
		throw new IllegalStateException("Block entity at " + pos + " is not a MinigameSpawnerBlockEntity!");
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, ModBlocks.MINIGAME_SPAWNER.get());
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == 0) this.blockEntity.adjustFrequency(-10);
		else if (id == 1) this.blockEntity.adjustFrequency(-1);
		else if (id == 2) this.blockEntity.adjustFrequency(1);
		else if (id == 3) this.blockEntity.adjustFrequency(10);
		else if (id == 4) this.blockEntity.setInfiniteMode(!this.blockEntity.isInfiniteMode());
		return true;
	}

	public int getFrequencyTicks() {
		return this.blockEntity.getFrequencyTicks();
	}

	public boolean isInfiniteMode() {
		return this.blockEntity.isInfiniteMode();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();

			// Si el clic es en las ranuras del generador (0 a 53)
			if (index < 54) {
				if (!this.moveItemStackTo(itemstack1, 54, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				// Clic en el inventario del jugador -> Mover a las ranuras del generador
				if (!this.moveItemStackTo(itemstack1, 0, 54, false)) {
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

	public MinigameSpawnerBlockEntity getBlockEntity() {
		return this.blockEntity;
	}
}
