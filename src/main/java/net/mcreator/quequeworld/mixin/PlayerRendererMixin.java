package net.mcreator.quequeworld.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mcreator.quequeworld.client.ClientVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    // Color ARGB: Alpha=0x73 (~45%), R=0x33, G=0x99, B=0xFF (Azul Cian Espectral)
    private static final int GHOST_COLOR = (0x73 << 24) | (0x33 << 16) | (0x99 << 8) | 0xFF;

    private static boolean isGhostPlayer(AbstractClientPlayer player) {
        if (player == null) return false;
        if (player.getTags().contains("fantasma")) return true;
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getUUID().equals(player.getUUID()) && ClientVariables.isGhost;
    }

    @Redirect(
        method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/PlayerModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
        ),
        require = 0
    )
    private void redirectPlayerRenderToBuffer(
        PlayerModel<AbstractClientPlayer> model,
        PoseStack poseStack,
        com.mojang.blaze3d.vertex.VertexConsumer buffer,
        int packedLight,
        int packedOverlay,
        int color,
        AbstractClientPlayer entity,
        float entityYaw,
        float partialTicks,
        PoseStack matrixStack,
        MultiBufferSource bufferSource,
        int light
    ) {
        if (isGhostPlayer(entity)) {
            ResourceLocation texture = entity.getSkin().texture();
            com.mojang.blaze3d.vertex.VertexConsumer translucentBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
            model.renderToBuffer(poseStack, translucentBuffer, packedLight, packedOverlay, GHOST_COLOR);
        } else {
            model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
