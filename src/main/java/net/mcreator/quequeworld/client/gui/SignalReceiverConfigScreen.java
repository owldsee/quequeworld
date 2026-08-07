package net.mcreator.quequeworld.client.gui;

import net.mcreator.quequeworld.network.UpdateSignalReceiverPacket;
import net.mcreator.quequeworld.world.inventory.SignalReceiverMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class SignalReceiverConfigScreen extends AbstractContainerScreen<SignalReceiverMenu> {
    private EditBox channelEdit;
    private EditBox signalEdit;

    public SignalReceiverConfigScreen(SignalReceiverMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 120;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos;
        int top = this.topPos;

        this.channelEdit = new EditBox(this.font, left + 15, top + 30, 146, 18, Component.literal("Canal"));
        this.channelEdit.setMaxLength(64);
        this.channelEdit.setValue(this.menu.getBlockEntity().getListenChannel());
        this.addRenderableWidget(this.channelEdit);

        this.signalEdit = new EditBox(this.font, left + 15, top + 65, 146, 18, Component.literal("Señal"));
        this.signalEdit.setMaxLength(64);
        this.signalEdit.setValue(this.menu.getBlockEntity().getListenSignal());
        this.addRenderableWidget(this.signalEdit);

        this.addRenderableWidget(
            Button.builder(Component.literal("Guardar"), b -> saveAndClose())
                .bounds(left + 50, top + 92, 76, 20)
                .build()
        );
    }

    private void saveAndClose() {
        String ch = this.channelEdit.getValue().trim();
        String sig = this.signalEdit.getValue().trim();
        PacketDistributor.sendToServer(new UpdateSignalReceiverPacket(this.menu.getBlockEntity().getBlockPos(), ch, sig));
        this.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF2C2C2C);
        guiGraphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + this.imageWidth - 2, this.topPos + this.imageHeight - 2, 0xFF1E1E1E);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, "§e§lReceptor de Señal", 35, 8, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "§7Canal:", 15, 20, 0xAAAAAA, false);
        guiGraphics.drawString(this.font, "§7Señal Esperada:", 15, 53, 0xAAAAAA, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
