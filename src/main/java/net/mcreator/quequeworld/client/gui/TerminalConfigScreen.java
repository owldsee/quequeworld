package net.mcreator.quequeworld.client.gui;

import net.mcreator.quequeworld.network.UpdateTerminalConfigPacket;
import net.mcreator.quequeworld.world.inventory.TerminalConfigMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class TerminalConfigScreen extends AbstractContainerScreen<TerminalConfigMenu> {
    private EditBox signalEdit;
    private EditBox wordEdit;

    public TerminalConfigScreen(TerminalConfigMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 180;
        this.imageHeight = 125;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos;
        int top = this.topPos;

        this.signalEdit = new EditBox(this.font, left + 15, top + 30, 150, 18, Component.literal("Canal:Señal"));
        this.signalEdit.setMaxLength(128);
        this.signalEdit.setValue(this.menu.getBlockEntity().getEmitChannelSignal());
        this.addRenderableWidget(this.signalEdit);

        this.wordEdit = new EditBox(this.font, left + 15, top + 65, 150, 18, Component.literal("Palabra"));
        this.wordEdit.setMaxLength(128);
        this.wordEdit.setValue(this.menu.getBlockEntity().getExpectedWord());
        this.addRenderableWidget(this.wordEdit);

        this.addRenderableWidget(
            Button.builder(Component.literal("Guardar"), b -> saveAndClose())
                .bounds(left + 52, top + 95, 76, 20)
                .build()
        );
    }

    private void saveAndClose() {
        String sig = this.signalEdit.getValue().trim();
        String word = this.wordEdit.getValue().trim();
        PacketDistributor.sendToServer(new UpdateTerminalConfigPacket(this.menu.getBlockEntity().getBlockPos(), sig, word));
        this.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF2C2C2C);
        guiGraphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + this.imageWidth - 2, this.topPos + this.imageHeight - 2, 0xFF1E1E1E);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, "§b§lConfiguración Terminal", 25, 8, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "§7Señal a Emitir (canal:señal):", 15, 20, 0xAAAAAA, false);
        guiGraphics.drawString(this.font, "§7Palabra Clave Esperada:", 15, 53, 0xAAAAAA, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
