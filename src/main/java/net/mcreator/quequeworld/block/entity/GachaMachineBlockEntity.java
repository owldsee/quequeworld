package net.mcreator.quequeworld.block.entity;

import net.mcreator.quequeworld.init.ModBlockEntities;
import net.mcreator.quequeworld.item.ModItems;
import net.mcreator.quequeworld.init.ModMenus;
import net.mcreator.quequeworld.world.inventory.GachaMachineMenu;
import net.mcreator.quequeworld.block.GachaMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GachaMachineBlockEntity extends BlockEntity implements GeoBlockEntity, MenuProvider {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	// 27 slots: 0-25 para premios, slot 26 para fichas buffer
	private final ItemStackHandler inventory = new ItemStackHandler(27) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
			updateBlockLight();
		}
	};

	private int usedTimer = 0;
	private UUID currentPlayerUUID = null;

	public GachaMachineBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.GACHA_MACHINE_BE.get(), pos, state);
	}

	public ItemStackHandler getInventory() {
		return this.inventory;
	}

	// ==========================================
	// LÓGICA DE JUEGO (Fichas, Premios, Sonidos)
	// ==========================================

	public void insertToken(Player player, ItemStack playerTokenStack) {
		if (this.level == null || this.level.isClientSide()) return;

		// 1. Verificar si está girando
		if (this.usedTimer > 0) {
			player.displayClientMessage(Component.literal("§cLa máquina está ocupada en este momento."), true);
			return;
		}

		// 2. Verificar si hay premios disponibles
		int remainingPrizes = getFilledPrizeSlotsCount();
		if (remainingPrizes == 0) {
			player.displayClientMessage(Component.literal("Maquina Vacia"), true);
			return;
		}

		// 3. Obtener el buffer de fichas (slot 26)
		ItemStack tokenBuffer = this.inventory.getStackInSlot(26);
		int currentTokens = tokenBuffer.isEmpty() ? 0 : tokenBuffer.getCount();

		if (currentTokens < 3) {
			// Añadir una ficha al buffer
			if (tokenBuffer.isEmpty()) {
				this.inventory.setStackInSlot(26, new ItemStack(ModItems.QUEQUE_TOKEN.get(), 1));
			} else {
				tokenBuffer.grow(1);
				this.inventory.setStackInSlot(26, tokenBuffer); // Forzar cambio
			}

			// Consumir del jugador
			playerTokenStack.shrink(1);
			currentTokens++;

			// Sonido corto de inserción (usando sobrecarga double para admitir Holder)
			this.level.playSound(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 
				SoundEvents.NOTE_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.4F);
			player.displayClientMessage(Component.literal("§eFichas colocadas: §a[" + currentTokens + "/3]"), true);

			// Cuando llega a 3, iniciar el giro
			if (currentTokens == 3) {
				// Vaciar el buffer
				this.inventory.setStackInSlot(26, ItemStack.EMPTY);

				// Guardar el jugador e iniciar timer
				this.currentPlayerUUID = player.getUUID();
				this.usedTimer = 25; // 25 ticks = 1.25 segundos (coincide con la duración de la animación "used")
				
				// Sincronizar con el cliente para reproducir la animación
				this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
			}
		}
	}

	public void refundTokens(Player player) {
		if (this.level == null || this.level.isClientSide()) return;

		ItemStack tokenBuffer = this.inventory.getStackInSlot(26);
		if (!tokenBuffer.isEmpty() && tokenBuffer.getCount() > 0) {
			int count = tokenBuffer.getCount();
			ItemStack refundStack = new ItemStack(ModItems.QUEQUE_TOKEN.get(), count);

			if (!player.getInventory().add(refundStack)) {
				player.drop(refundStack, false);
			}

			this.inventory.setStackInSlot(26, ItemStack.EMPTY);
			this.level.playSound(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 
				SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
			player.displayClientMessage(Component.literal("§eFichas devueltas: §a[0/3]"), true);
		}
	}

	private int getFilledPrizeSlotsCount() {
		int count = 0;
		for (int i = 0; i < 26; i++) {
			if (!this.inventory.getStackInSlot(i).isEmpty()) {
				count++;
			}
		}
		return count;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, GachaMachineBlockEntity blockEntity) {
		if (level.isClientSide()) return;

		if (blockEntity.usedTimer > 0) {
			blockEntity.usedTimer--;

			// Efecto de sonido girando (ruleta traga perras usando sobrecarga double para admitir Holder)
			if (blockEntity.usedTimer % 4 == 0) {
				float pitch = 0.8F + (25 - blockEntity.usedTimer) * 0.05F; // Pitch ascendente
				level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
					SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5F, pitch);
				level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
					SoundEvents.NOTE_BLOCK_BELL, SoundSource.BLOCKS, 0.4F, pitch + 0.2F);
			}

			// Entregar premio al finalizar la animación
			if (blockEntity.usedTimer == 0) {
				blockEntity.deliverPrize();
			}
			blockEntity.setChanged();
		}
	}

	private void deliverPrize() {
		if (this.level == null || this.level.isClientSide() || this.currentPlayerUUID == null) return;

		Player player = this.level.getPlayerByUUID(this.currentPlayerUUID);
		this.currentPlayerUUID = null; // Resetear para el siguiente giro

		// Obtener slots de premios no vacíos
		List<Integer> filledSlots = new ArrayList<>();
		for (int i = 0; i < 26; i++) {
			if (!this.inventory.getStackInSlot(i).isEmpty()) {
				filledSlots.add(i);
			}
		}

		if (filledSlots.isEmpty()) {
			if (player != null) {
				player.displayClientMessage(Component.literal("§cLa máquina se quedó sin premios."), true);
			}
			return;
		}

		// Selección aleatoria
		int randomIndex = filledSlots.get(this.level.random.nextInt(filledSlots.size()));
		ItemStack prizeStack = this.inventory.getStackInSlot(randomIndex).copy();
		
		// Vaciar el slot por completo (extraemos toda la pila)
		this.inventory.setStackInSlot(randomIndex, ItemStack.EMPTY);

		// Crear la QueQue Cápsula y guardar el premio en CUSTOM_DATA
		ItemStack capsule = new ItemStack(ModItems.QUEQUE_CAPSULA.get());
		CompoundTag surpriseTag = (CompoundTag) prizeStack.save(this.level.registryAccess());
		CompoundTag capsuleTag = new CompoundTag();
		capsuleTag.put("surprise_item", surpriseTag);
		capsule.set(DataComponents.CUSTOM_DATA, CustomData.of(capsuleTag));

		// Dar al jugador
		if (player != null) {
			if (!player.getInventory().add(capsule)) {
				player.drop(capsule, false);
			}

			// Mostrar cápsulas restantes
			int remaining = getFilledPrizeSlotsCount();
			if (remaining > 0) {
				player.displayClientMessage(Component.literal("Quedan " + remaining + " Capsulas"), true);
			} else {
				player.displayClientMessage(Component.literal("Maquina Vacia"), true);
			}

			// Sonido de victoria (campanada y subida de nivel, usando sobrecarga double para admitir Holder)
			this.level.playSound(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 
				SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 0.9F, 1.0F);
			this.level.playSound(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 
				SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.5F);
		} else {
			// En caso extremo de que el jugador se desconecte, tirar la cápsula en el bloque
			Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5, capsule);
		}

		// Forzar actualización al cliente
		this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
	}

	public void openGUI(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(this, this.worldPosition);
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

	// ==========================================
	// SERIALIZACIÓN (NBT y Sincronización)
	// ==========================================

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.put("Inventory", this.inventory.serializeNBT(provider));
		tag.putInt("UsedTimer", this.usedTimer);
		if (this.currentPlayerUUID != null) {
			tag.putUUID("CurrentPlayerUUID", this.currentPlayerUUID);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		if (tag.contains("Inventory")) {
			this.inventory.deserializeNBT(provider, tag.getCompound("Inventory"));
		}
		this.usedTimer = tag.getInt("UsedTimer");
		if (tag.hasUUID("CurrentPlayerUUID")) {
			this.currentPlayerUUID = tag.getUUID("CurrentPlayerUUID");
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		tag.putInt("UsedTimer", this.usedTimer);
		return tag;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	// ==========================================
	// INTERFAZ DE SOPORTE GECKOLIB (Animaciones)
	// ==========================================

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "machine_controller", 2, event -> {
			// Si el temporizador de giro está activo en el cliente, reproducimos "used"
			if (this.usedTimer > 0) {
				return event.setAndContinue(RawAnimation.begin().thenPlay("used"));
			}
			// Por defecto, animación de espera
			return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
		}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	// ==========================================
	// INTERFAZ MENU PROVIDER (GUI)
	// ==========================================

	@Override
	public Component getDisplayName() {
		return Component.literal("Lucky Machine");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new GachaMachineMenu(containerId, playerInventory, this);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		updateBlockLight();
	}

	public void updateBlockLight() {
		if (this.level == null || this.level.isClientSide()) return;
		BlockState state = this.getBlockState();
		if (state.is(net.mcreator.quequeworld.init.ModBlocks.GACHA_MACHINE.get())) {
			boolean shouldBeLit = getFilledPrizeSlotsCount() > 0;
			if (state.getValue(GachaMachineBlock.LIT) != shouldBeLit) {
				this.level.setBlock(this.worldPosition, state.setValue(GachaMachineBlock.LIT, shouldBeLit), 3);
				BlockPos abovePos = this.worldPosition.above();
				BlockState aboveState = this.level.getBlockState(abovePos);
				if (aboveState.is(state.getBlock())) {
					this.level.setBlock(abovePos, aboveState.setValue(GachaMachineBlock.LIT, shouldBeLit), 3);
				}
			}
		}
	}
}
