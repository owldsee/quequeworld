package net.mcreator.quequeworld.block.entity;

import net.mcreator.quequeworld.init.ModBlockEntities;
import net.mcreator.quequeworld.world.inventory.MinigameSpawnerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MinigameSpawnerBlockEntity extends BlockEntity implements MenuProvider {
	private final ItemStackHandler inventory = new ItemStackHandler(54) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};

	private int frequencyTicks = 20; // 20 ticks por defecto (1 segundo)
	private int timerTicks = 0;
	private boolean infiniteMode = true; // true = tabla de looteo infinita, false = consumir ítems

	public MinigameSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MINIGAME_SPAWNER_BE.get(), pos, state);
	}

	public ItemStackHandler getInventory() {
		return this.inventory;
	}

	public int getFrequencyTicks() {
		return this.frequencyTicks;
	}

	public void setFrequencyTicks(int val) {
		this.frequencyTicks = Math.max(1, Math.min(1200, val));
		setChanged();
	}

	public boolean isInfiniteMode() {
		return this.infiniteMode;
	}

	public void setInfiniteMode(boolean val) {
		this.infiniteMode = val;
		setChanged();
		if (this.level != null && !this.level.isClientSide()) {
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		}
	}

	public void adjustFrequency(int delta) {
		this.setFrequencyTicks(this.frequencyTicks + delta);
		if (this.level != null && !this.level.isClientSide()) {
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		}
	}

	public static void tick(Level level, BlockPos pos, BlockState state, MinigameSpawnerBlockEntity blockEntity) {
		if (level.isClientSide()) return;

		// Solo suelta objetos al recibir energía redstone
		if (!level.hasNeighborSignal(pos)) {
			return;
		}

		blockEntity.timerTicks++;
		if (blockEntity.timerTicks >= blockEntity.frequencyTicks) {
			blockEntity.timerTicks = 0;
			blockEntity.spawnRandomItem();
		}
	}

	private void spawnRandomItem() {
		if (this.level == null || this.level.isClientSide()) return;

		List<Integer> filledSlots = new ArrayList<>();
		for (int i = 0; i < 54; i++) {
			if (!this.inventory.getStackInSlot(i).isEmpty()) {
				filledSlots.add(i);
			}
		}

		if (filledSlots.isEmpty()) {
			return;
		}

		int chosenSlot = filledSlots.get(this.level.random.nextInt(filledSlots.size()));
		ItemStack stackInSlot = this.inventory.getStackInSlot(chosenSlot);
		ItemStack toSpawn = this.infiniteMode ? stackInSlot.copyWithCount(1) : stackInSlot.split(1);

		if (!toSpawn.isEmpty()) {
			ItemEntity itemEntity = new ItemEntity(this.level,
				this.worldPosition.getX() + 0.5D,
				this.worldPosition.getY() + 1.05D,
				this.worldPosition.getZ() + 0.5D,
				toSpawn);
			itemEntity.setDeltaMovement(
				(this.level.random.nextDouble() - 0.5D) * 0.15D,
				0.32D,
				(this.level.random.nextDouble() - 0.5D) * 0.15D
			);
			this.level.addFreshEntity(itemEntity);

			this.level.playSound(null, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D,
				SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.6F, 1.2F);

			this.setChanged();
		}
	}

	public void drops() {
		SimpleContainer container = new SimpleContainer(this.inventory.getSlots());
		for (int i = 0; i < this.inventory.getSlots(); i++) {
			container.setItem(i, this.inventory.getStackInSlot(i));
		}
		if (this.level != null) {
			Containers.dropContents(this.level, this.worldPosition, container);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.put("Inventory", this.inventory.serializeNBT(provider));
		tag.putInt("FrequencyTicks", this.frequencyTicks);
		tag.putInt("TimerTicks", this.timerTicks);
		tag.putBoolean("InfiniteMode", this.infiniteMode);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		if (tag.contains("Inventory")) {
			this.inventory.deserializeNBT(provider, tag.getCompound("Inventory"));
		}
		if (tag.contains("FrequencyTicks")) {
			this.frequencyTicks = Math.max(1, tag.getInt("FrequencyTicks"));
		}
		if (tag.contains("InfiniteMode")) {
			this.infiniteMode = tag.getBoolean("InfiniteMode");
		}
		this.timerTicks = tag.getInt("TimerTicks");
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		tag.putInt("FrequencyTicks", this.frequencyTicks);
		tag.putBoolean("InfiniteMode", this.infiniteMode);
		return tag;
	}

	@Override
	public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
		super.onDataPacket(net, pkt, provider);
		CompoundTag tag = pkt.getTag();
		if (tag != null) {
			if (tag.contains("FrequencyTicks")) {
				this.frequencyTicks = Math.max(1, tag.getInt("FrequencyTicks"));
			}
			if (tag.contains("InfiniteMode")) {
				this.infiniteMode = tag.getBoolean("InfiniteMode");
			}
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Generador de Minijuego");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new MinigameSpawnerMenu(containerId, playerInventory, this);
	}
}
