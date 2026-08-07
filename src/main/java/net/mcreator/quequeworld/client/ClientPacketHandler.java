package net.mcreator.quequeworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

	public static void handleThreatAnimation(String itemId) {
		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> {
			try {
				ResourceLocation resLoc = ResourceLocation.parse(itemId);
				Item item = BuiltInRegistries.ITEM.get(resLoc);
				if (item != null) {
					ItemStack itemStack = new ItemStack(item);
					mc.gameRenderer.displayItemActivation(itemStack);
					if (mc.player != null) {
						mc.player.handleEntityEvent((byte) 35);
					}
				}
			} catch (Exception e) {
				net.mcreator.quequeworld.QuequeworldMod.LOGGER.error("[QQW] Error displaying totem animation: ", e);
			}
		});
	}
}
