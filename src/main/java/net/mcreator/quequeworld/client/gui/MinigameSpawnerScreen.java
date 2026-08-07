package net.mcreator.quequeworld.client.gui;

import net.mcreator.quequeworld.world.inventory.MinigameSpawnerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MinigameSpawnerScreen extends AbstractContainerScreen<MinigameSpawnerMenu> {
	private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

	private Button modeButton;

	public MinigameSpawnerScreen(MinigameSpawnerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		this.imageWidth = 176;
		this.imageHeight = 222;
		this.inventoryLabelY = 128;
	}

	@Override
	protected void init() {
		super.init();
		int relX = (this.width - this.imageWidth) / 2;
		int relY = (this.height - this.imageHeight) / 2;

		// Botón para alternar modo Infinito (Tabla de Looteo) / Consumir ítem
		this.modeButton = this.addRenderableWidget(Button.builder(Component.literal(this.menu.isInfiniteMode() ? "Modo: Tabla (∞)" : "Modo: Consumir"), button -> {
			if (this.minecraft != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 4);
			}
		}).bounds(relX + 4, relY - 18, 92, 16).build());

		// Botones para ajustar frecuencia [-10, -1, +1, +10]
		this.addRenderableWidget(Button.builder(Component.literal("-10"), button -> {
			if (this.minecraft != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
			}
		}).bounds(relX + 100, relY - 18, 22, 16).build());

		this.addRenderableWidget(Button.builder(Component.literal("-1"), button -> {
			if (this.minecraft != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
			}
		}).bounds(relX + 124, relY - 18, 18, 16).build());

		this.addRenderableWidget(Button.builder(Component.literal("+1"), button -> {
			if (this.minecraft != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
			}
		}).bounds(relX + 144, relY - 18, 18, 16).build());

		this.addRenderableWidget(Button.builder(Component.literal("+10"), button -> {
			if (this.minecraft != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 3);
			}
		}).bounds(relX + 164, relY - 18, 22, 16).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
		if (this.modeButton != null) {
			this.modeButton.setMessage(Component.literal(this.menu.isInfiniteMode() ? "Modo: Tabla (∞)" : "Modo: Consumir"));
		}
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		int relX = (this.width - this.imageWidth) / 2;
		int relY = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(CONTAINER_BACKGROUND, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
		String frecText = String.format("Mochila | Frec: %dt (%.1fs)", this.menu.getFrequencyTicks(), this.menu.getFrequencyTicks() / 20.0f);
		guiGraphics.drawString(this.font, Component.literal(frecText), this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
	}
}
