/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.quequeworld.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredItem;

import net.mcreator.quequeworld.QuequeworldMod;

@EventBusSubscriber
public class QuequeworldModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QuequeworldMod.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GOD_CONSTRUCTION_TAB = REGISTRY.register("god_construction_tab",
		() -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.quequeworld.god_construction_tab"))
			.icon(() -> new ItemStack(GodBlocks.PACKED_ICE_ITEM.get()))
			.displayItems((parameters, output) -> {
				for (DeferredItem<BlockItem> item : GodBlocks.GOD_BLOCK_ITEMS) {
					output.accept(item.get());
				}
			})
			.build()
	);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MINIGAMES_TAB = REGISTRY.register("minigames_tab",
		() -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.quequeworld.minigames_tab"))
			.icon(() -> new ItemStack(ModBlocks.MINIGAME_SPAWNER_ITEM.get()))
			.displayItems((parameters, output) -> {
				output.accept(ModBlocks.MINIGAME_SPAWNER_ITEM.get());
				output.accept(ModBlocks.GOAL_BELL_ITEM.get());
				output.accept(ModBlocks.GOAL_TRIGGER_ITEM.get());
				output.accept(ModBlocks.CHECKPOINT_ITEM.get());
				output.accept(ModBlocks.INSTANT_KILL_ITEM.get());
				output.accept(ModBlocks.GACHA_MACHINE_ITEM.get());
				output.accept(ModBlocks.REDUCTOR_BLOCK_ITEM.get());
				output.accept(ModBlocks.GIANTER_BLOCK_ITEM.get());
				output.accept(ModBlocks.NORMALIZER_BLOCK_ITEM.get());
				output.accept(ModBlocks.SIGNAL_RECEIVER_ITEM.get());
				output.accept(ModBlocks.TERMINAL_BLOCK_ITEM.get());
			})
			.build()
	);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			tabData.accept(QuequeworldModItems.Q_COIN.get());
		}
	}
}