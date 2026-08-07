package net.mcreator.quequeworld.client.gui;

import net.mcreator.quequeworld.network.SubmitTerminalInputPacket;
import net.mcreator.quequeworld.world.inventory.TerminalInteractMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class TerminalInteractScreen extends AbstractContainerScreen<TerminalInteractMenu> {
    private EditBox inputEdit;

    public TerminalInteractScreen(TerminalInteractMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 95;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos;
        int top = this.topPos;

        this.inputEdit = new EditBox(this.font, left + 15, top + 35, 146, 18, Component.literal("Respuesta"));
        this.inputEdit.setMaxLength(128);
        this.addRenderableWidget(this.inputEdit);

        this.addRenderableWidget(
            Button.builder(Component.literal("Enviar"), b -> submitAndClose())
                .bounds(left + 50, top + 62, 76, 20)
                .build()
        );
    }

    private void submitAndClose() {
        String word = this.inputEdit.getValue().trim();
        if (!word.isEmpty()) {
            PacketDistributor.sendToServer(new SubmitTerminalInputPacket(this.menu.getBlockEntity().getBlockPos(), word));
        }
        this.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF2C2C2C);
        guiGraphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + this.imageWidth - 2, this.topPos + this.imageHeight - 2, 0xFF1E1E1E);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, "§b§lTerminal de Entrada", 30, 8, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "§7Ingresa la palabra requerida:", 15, 23, 0xAAAAAA, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
