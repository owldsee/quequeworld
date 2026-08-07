package net.mcreator.quequeworld.client;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.init.ModBlockEntities;
import net.mcreator.quequeworld.init.ModMenus;
import net.mcreator.quequeworld.client.gui.GachaMachineScreen;
import net.mcreator.quequeworld.client.gui.MinigameSpawnerScreen;
import net.mcreator.quequeworld.client.renderer.GachaMachineBlockRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;

@EventBusSubscriber(modid = QuequeworldMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenus.GACHA_MACHINE_MENU.get(), GachaMachineScreen::new);
		event.register(ModMenus.MINIGAME_SPAWNER_MENU.get(), MinigameSpawnerScreen::new);
		event.register(ModMenus.SIGNAL_RECEIVER_MENU.get(), net.mcreator.quequeworld.client.gui.SignalReceiverConfigScreen::new);
		event.register(ModMenus.TERMINAL_CONFIG_MENU.get(), net.mcreator.quequeworld.client.gui.TerminalConfigScreen::new);
		event.register(ModMenus.TERMINAL_INTERACT_MENU.get(), net.mcreator.quequeworld.client.gui.TerminalInteractScreen::new);
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModBlockEntities.GACHA_MACHINE_BE.get(), GachaMachineBlockRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.GOAL_BELL_BE.get(), net.minecraft.client.renderer.blockentity.BellRenderer::new);
	}

	private static int prevTagType = 0; // 0 = none, 1 = shield, 2 = threatened
	private static int lastActiveTagType = 0;
	private static boolean isExiting = false;
	private static long animationStartTime = 0;

	private static float countdownProgress = 0.0f;
	private static float dayTimerProgress = 0.0f;
	private static long lastTime = 0L;

	private static float easeInOutQuad(float t) {
		return t < 0.5f ? 2.0f * t * t : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 2.0f) / 2.0f;
	}

	@SubscribeEvent
	public static void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAbove(
			net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH,
			ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "soul_shield_hud"),
			(guiGraphics, deltaTracker) -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player == null || mc.options.hideGui) {
					return;
				}
				
				if (!mc.player.isCreative() && !mc.player.isSpectator()) {
					boolean hasShield = ClientVariables.hasShield;
					boolean isThreatened = ClientVariables.isThreatened;
					
					int currentTagType = hasShield ? 1 : (isThreatened ? 2 : 0);
					
					// State transitions
					if (currentTagType != 0) {
						if (prevTagType != currentTagType) {
							// Transition: Inactive -> Active, or Active A -> Active B
							isExiting = false;
							prevTagType = currentTagType;
							lastActiveTagType = currentTagType;
							animationStartTime = System.currentTimeMillis();
						}
					} else {
						if (prevTagType != 0) {
							// Transition: Active -> Inactive (Start Exit Anim)
							isExiting = true;
							prevTagType = 0;
							animationStartTime = System.currentTimeMillis();
						}
					}
					
					// Rendering logic
					int renderType = 0;
					int currentY = 0;
					int screenWidth = mc.getWindow().getGuiScaledWidth();
					int screenHeight = mc.getWindow().getGuiScaledHeight();
					int targetX = screenWidth / 2 + 91 + 4; // Right side of hotbar (width 182, starts at center - 91)
					int targetY = screenHeight - 24 - 4;   // Bottom edge with 4px margin
					
					if (isExiting) {
						long elapsed = System.currentTimeMillis() - animationStartTime;
						float progress = Math.min(1.0f, elapsed / 300.0f);
						if (progress >= 1.0f) {
							isExiting = false;
							lastActiveTagType = 0;
						} else {
							renderType = lastActiveTagType;
							float ease = progress * progress * progress; // Cubic Ease-In
							currentY = (int) (targetY + (screenHeight + 10 - targetY) * ease);
						}
					} else if (currentTagType != 0) {
						renderType = lastActiveTagType;
						long elapsed = System.currentTimeMillis() - animationStartTime;
						float progress = Math.min(1.0f, elapsed / 300.0f);
						float ease = 1.0f - (1.0f - progress) * (1.0f - progress) * (1.0f - progress); // Cubic Ease-Out
						currentY = (int) (screenHeight + 10 - (screenHeight + 10 - targetY) * ease);
					}
					
					if (renderType != 0) {
						ResourceLocation texture = renderType == 1 ? 
							ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/gui/icon_escudo.png") :
							ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/gui/icon_amenazado.png");
						
						guiGraphics.blit(texture, targetX, currentY, 24, 24, 0.0F, 0.0F, 24, 24, 24, 24);
					}
				}
			}
		);

		event.registerAbove(
			net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH,
			ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "quequeworld_timers_hud"),
			(guiGraphics, deltaTracker) -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player == null || mc.options.hideGui) {
					return;
				}
				
				int screenWidth = mc.getWindow().getGuiScaledWidth();
				
				long now = System.currentTimeMillis();
				if (lastTime == 0L) {
					lastTime = now;
				}
				float dt = Math.min(0.1f, (now - lastTime) / 1000.0f);
				lastTime = now;

				// 1. General Countdown (Top Center)
				boolean countdownVisible = ClientVariables.countdownActive && ClientVariables.countdownTicks > 0;
				float countdownTarget = countdownVisible ? 1.0f : 0.0f;
				if (countdownProgress < countdownTarget) {
					countdownProgress = Math.min(countdownTarget, countdownProgress + dt / 0.5f);
				} else if (countdownProgress > countdownTarget) {
					countdownProgress = Math.max(countdownTarget, countdownProgress - dt / 0.5f);
				}
				float visualCountdownAlpha = easeInOutQuad(countdownProgress);

				if (visualCountdownAlpha > 0.0f) {
					int ticksLeft = ClientVariables.countdownTicks;
					String text = formatTime(Math.max(0, ticksLeft));
					int textWidth = mc.font.width(text);
					
					int frameX = (screenWidth - 48) / 2;
					int frameY = 6 - (int) ((1.0f - visualCountdownAlpha) * 10.0f);
					int textX = frameX + (48 - textWidth) / 2;
					int textY = frameY + 4;

					ResourceLocation timerFrame = ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/gui/timer_frame.png");
					guiGraphics.setColor(1.0F, 1.0F, 1.0F, visualCountdownAlpha);
					guiGraphics.blit(timerFrame, frameX, frameY, 48, 16, 0.0F, 0.0F, 48, 16, 48, 16);
					guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

					int textColor = (ticksLeft <= 200) ? 0xFF5555 : 0xFFFFFF;
					int alphaVal = Math.round(visualCountdownAlpha * 255.0f) & 0xFF;
					int colorARGB = (alphaVal << 24) | (textColor & 0xFFFFFF);
					guiGraphics.drawString(mc.font, text, textX, textY, colorARGB, true);
				}
				
				// 2. Render Global Day Timer (Top Right)
				boolean dayTimerVisible = ClientVariables.dayTimerActive && ClientVariables.dayTimerTicks > 0;
				float dayTimerTarget = dayTimerVisible ? 1.0f : 0.0f;
				if (dayTimerProgress < dayTimerTarget) {
					dayTimerProgress = Math.min(dayTimerTarget, dayTimerProgress + dt / 0.5f);
				} else if (dayTimerProgress > dayTimerTarget) {
					dayTimerProgress = Math.max(dayTimerTarget, dayTimerProgress - dt / 0.5f);
				}
				float visualDayTimerAlpha = easeInOutQuad(dayTimerProgress);

				if (visualDayTimerAlpha > 0.0f) {
					int ticksLeft = ClientVariables.dayTimerTicks;
					String text = formatTime(Math.max(0, ticksLeft));
					int textWidth = mc.font.width(text);
					
					int frameX = (screenWidth - 48 - 10) + (int) ((1.0f - visualDayTimerAlpha) * 15.0f);
					int frameY = 6;
					int textX = frameX + (48 - textWidth) / 2;
					int textY = frameY + 4;

					ResourceLocation dayFrame = ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/gui/day_timer_frame.png");
					guiGraphics.setColor(1.0F, 1.0F, 1.0F, visualDayTimerAlpha);
					guiGraphics.blit(dayFrame, frameX, frameY, 48, 16, 0.0F, 0.0F, 48, 16, 48, 16);
					guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

					int textColor = 0xFFFFFF;
					if (ticksLeft <= 6000) {
						boolean flash = (System.currentTimeMillis() / 500) % 2 == 0;
						textColor = flash ? 0xFF5555 : 0xFFFFFF;
					}
					int alphaVal = Math.round(visualDayTimerAlpha * 255.0f) & 0xFF;
					int colorARGB = (alphaVal << 24) | (textColor & 0xFFFFFF);
					guiGraphics.drawString(mc.font, text, textX, textY, colorARGB, true);
				}
			}
		);

		event.registerAbove(
			net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH,
			ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "quequeworld_indicators_hud"),
			(guiGraphics, deltaTracker) -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player == null || mc.options.hideGui) {
					return;
				}

				if (!mc.player.isCreative() && !mc.player.isSpectator() && !ClientVariables.isGhost && !mc.player.getTags().contains("fantasma")) {
					int bandCount = (int) Math.round(ClientVariables.displayedBanderines);
					int debtCount = (int) Math.round(ClientVariables.displayedDeuda);
					double dangerVal = ClientVariables.displayedDangerLevel;

					// 1. Render Banderín / Deuda en (16, 16)
					ResourceLocation icon = (debtCount > 0) ?
						ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/gui/deuda.png") :
						ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/gui/banderin.png");

					// Dibujar textura 16x16
					guiGraphics.blit(icon, 16, 16, 16, 16, 0.0F, 0.0F, 16, 16, 16, 16);

					// Formatear texto: "X (-Y)" o "X"
					String textBanderines = String.valueOf(bandCount);
					if (debtCount > 0) {
						textBanderines += " (-" + debtCount + ")";
					}
					guiGraphics.drawString(mc.font, textBanderines, 36, 20, 0xFFFFFF, true);

					// 2. Render Peligro en (16, 36)
					net.minecraft.world.item.ItemStack fireCharge = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIRE_CHARGE);
					guiGraphics.renderFakeItem(fireCharge, 16, 36);

					// Formatear texto de peligro (porcentaje con 1 decimal)
					String textPeligro = String.format("%.1f", dangerVal * 100.0D) + "%";
					
					double dangerPercentage = dangerVal * 100.0D;
					int dangerColor;
					if (dangerPercentage > 60.0D) {
						dangerColor = 0xFF5555; // Rojo
					} else if (dangerPercentage < 40.0D) {
						dangerColor = 0x55FF55; // Verde
					} else {
						dangerColor = 0xFFFFFF; // Blanco
					}
					guiGraphics.drawString(mc.font, textPeligro, 36, 40, dangerColor, true);

					// Tooltip al colocar el cursor sobre el área de peligro (16, 36) a (16+60, 36+16)
					double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
					double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();
					if (mouseX >= 16 && mouseX <= 85 && mouseY >= 36 && mouseY <= 54) {
						int minBand = ClientVariables.minBanderines;
						int diffBand = bandCount - minBand;
						double bandBonusPct = (diffBand * 2.0D);
						double deudaPenaltyPct = (debtCount * 5.0D);

						java.util.List<net.minecraft.network.chat.Component> tooltip = new java.util.ArrayList<>();
						tooltip.add(net.minecraft.network.chat.Component.literal("§c§lNivel de Peligro: §f" + String.format("%.1f", dangerVal * 100.0D) + "%"));
						tooltip.add(net.minecraft.network.chat.Component.literal("§7--------------------"));
						tooltip.add(net.minecraft.network.chat.Component.literal("§e• Peligro Base: §f50.0%"));
						
						if (diffBand >= 0) {
							tooltip.add(net.minecraft.network.chat.Component.literal("§a• Banderines (+ " + diffBand + " s/mín): §a+" + String.format("%.1f", bandBonusPct) + "%"));
						} else {
							tooltip.add(net.minecraft.network.chat.Component.literal("§c• Banderines (" + diffBand + " s/mín): §c" + String.format("%.1f", bandBonusPct) + "%"));
						}

						if (debtCount > 0) {
							tooltip.add(net.minecraft.network.chat.Component.literal("§c• Deudas (" + debtCount + " activas): §c-" + String.format("%.1f", deudaPenaltyPct) + "%"));
						} else {
							tooltip.add(net.minecraft.network.chat.Component.literal("§7• Deudas (0 activas): 0.0%"));
						}

						guiGraphics.renderComponentTooltip(mc.font, tooltip, (int) mouseX, (int) mouseY);
					}
				}
			}
		);

		event.registerAbove(
			net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH,
			ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "quequeworld_welcome_hud"),
			(guiGraphics, deltaTracker) -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player == null || mc.options.hideGui) {
					return;
				}
				long elapsed = System.currentTimeMillis() - ClientVariables.welcomeStartTime;
				if (elapsed >= 0 && elapsed < 3000L && !ClientVariables.welcomeType.isEmpty()) {
					float alpha = 1.0f;
					if (elapsed > 2000L) {
						alpha = 1.0f - (float) (elapsed - 2000L) / 1000.0f;
					}
					alpha = Math.max(0.0f, Math.min(1.0f, alpha));

					int screenWidth = mc.getWindow().getGuiScaledWidth();
					int screenHeight = mc.getWindow().getGuiScaledHeight();

					if ("img".equalsIgnoreCase(ClientVariables.welcomeType)) {
						ResourceLocation bannerLoc = ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/gui/banners/" + ClientVariables.welcomeContent + ".png");
						int texW = 1024;
						int texH = "dia_1".equalsIgnoreCase(ClientVariables.welcomeContent) ? 433 : 411;
						int displayW = 220;
						int displayH = (int) (displayW * ((double) texH / texW));
						int bannerX = (screenWidth - displayW) / 2;
						int bannerY = screenHeight / 4;

						guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
						guiGraphics.blit(bannerLoc, bannerX, bannerY, displayW, displayH, 0.0F, 0.0F, texW, texH, texW, texH);
						guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
					} else if ("texto".equalsIgnoreCase(ClientVariables.welcomeType)) {
						int alphaVal = Math.round(alpha * 255.0f) & 0xFF;
						int colorARGB = (alphaVal << 24) | 0xFFE55C; // Dorado/Amarillo
						guiGraphics.pose().pushPose();
						guiGraphics.pose().scale(2.0f, 2.0f, 1.0f);
						guiGraphics.drawCenteredString(mc.font, ClientVariables.welcomeContent, (screenWidth / 2) / 2, (screenHeight / 3) / 2, colorARGB);
						guiGraphics.pose().popPose();
					}
				}
			}
		);
	}


	private static String formatTime(int ticks) {
		if (ticks >= 1200) {
			int totalSecs = ticks / 20;
			int totalMins = totalSecs / 60;
			int hours = totalMins / 60;
			int minutes = totalMins % 60;
			return String.format("%02d:%02d", hours, minutes);
		} else {
			int seconds = ticks / 20;
			int remainingTicks = ticks % 20;
			return String.format("%02d:%02d", seconds, remainingTicks);
		}
	}
}


