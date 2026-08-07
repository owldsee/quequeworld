package net.mcreator.quequeworld.client;

import net.mcreator.quequeworld.QuequeworldMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = QuequeworldMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
	private static boolean wasGuiHidden = false;
	private static boolean wasSpectatingLastTick = false;

	private static final ResourceLocation THREAT_ICON =
		ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/gui/icon_amenazado.png");
	private static final long THREAT_ANIM_DURATION_MS = 3000L;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			boolean isSpectating = mc.getCameraEntity() != null && mc.getCameraEntity() != mc.player;
			if (isSpectating && !wasSpectatingLastTick) {
				wasGuiHidden = mc.options.hideGui;
				mc.options.hideGui = true;
				wasSpectatingLastTick = true;
			} else if (!isSpectating && wasSpectatingLastTick) {
				mc.options.hideGui = wasGuiHidden;
				wasSpectatingLastTick = false;
			}

			// Decrement timers locally
			if (!mc.isPaused()) {
				if (ClientVariables.countdownActive) {
					if (ClientVariables.countdownTicks > 0) {
						ClientVariables.countdownTicks--;
					} else {
						ClientVariables.countdownActive = false;
					}
				}
				if (ClientVariables.dayTimerActive && !ClientVariables.dayTimerPaused) {
					if (ClientVariables.dayTimerTicks > 0) {
						ClientVariables.dayTimerTicks--;
					} else {
						ClientVariables.dayTimerActive = false;
					}
				}

				// Interpolación numérica para banderines, deudas y peligro
				double diffBanderines = ClientVariables.banderines - ClientVariables.displayedBanderines;
				if (Math.abs(diffBanderines) < 0.05D) {
					ClientVariables.displayedBanderines = ClientVariables.banderines;
				} else {
					ClientVariables.displayedBanderines += diffBanderines * 0.15D;
				}

				double diffDeuda = ClientVariables.deuda - ClientVariables.displayedDeuda;
				if (Math.abs(diffDeuda) < 0.05D) {
					ClientVariables.displayedDeuda = ClientVariables.deuda;
				} else {
					ClientVariables.displayedDeuda += diffDeuda * 0.15D;
				}

				double diffDanger = ClientVariables.dangerLevel - ClientVariables.displayedDangerLevel;
				if (Math.abs(diffDanger) < 0.001D) {
					ClientVariables.displayedDangerLevel = ClientVariables.dangerLevel;
				} else {
					ClientVariables.displayedDangerLevel += diffDanger * 0.15D;
				}
			}
		}
	}

}
