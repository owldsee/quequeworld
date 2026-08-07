package net.mcreator.quequeworld.item;

import net.mcreator.quequeworld.QuequeworldMod;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;


import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.List;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Tiers;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModItems {

	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QuequeworldMod.MODID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QuequeworldMod.MODID);

	public static final DeferredItem<Item> BISMUTH = ITEMS.register("bismuth",
		() -> new Item(new Item.Properties())
		);

	public static final DeferredItem<Item> QUEQUE_TOKEN = ITEMS.register("queque_token",
		() -> new Item(new Item.Properties().stacksTo(64))
		);

	public static final DeferredItem<Item> VALE_BANDERIN = ITEMS.register("vale_banderin", ValeBanderinItem::new);

	public static final DeferredItem<Item> UNIVERSAL_KEY = ITEMS.register("universal_key",
		() -> new Item(new Item.Properties().stacksTo(1))
		);

	public static final DeferredItem<Item> QUEQUE_CAPSULA = ITEMS.register("queque_capsula",
		QueQueCapsulaItem::new
		);

	public static final DeferredItem<Item> ENDER_MIRROR = ITEMS.register("ender_mirror",
		() -> new EnderMirrorItem(new Item.Properties().stacksTo(1))
		);

	public static final DeferredItem<Item> QUEQUE_PEARL = ITEMS.register("queque_pearl",
		() -> new QueQuePearlItem(new Item.Properties().stacksTo(16))
		);

	public static final DeferredItem<Item> CACHAPORRA = ITEMS.register("cachaporra",
		() -> new CachaporraItem(new Item.Properties().stacksTo(1))
		);

	public static final DeferredItem<Item> MUFFIN_GLITTER = ITEMS.register("muffin_glitter",
		MuffinGlitterItem::new
		);

	public static final DeferredItem<Item> MUFFIN_GLOW = ITEMS.register("muffin_glow",
		MuffinGlowItem::new
		);

	public static final DeferredItem<Item> STICK_REDUCTOR = ITEMS.register("stick_reductor",
		StickReductorItem::new
		);

	public static final DeferredItem<Item> STICK_GIANT = ITEMS.register("stick_giant",
		StickGiantItem::new
		);



	public static final DeferredItem<Item> STICK_CAMERA = ITEMS.register("stick_camera",
		StickCameraItem::new
		);

	public static final DeferredItem<Item> MEGAPHONE = ITEMS.register("megaphone",
		() -> new Item(new Item.Properties().stacksTo(1))
		);

	public static final DeferredItem<Item> GLOBAL_ORB = ITEMS.register("global_orb",
		() -> new Item(new Item.Properties().stacksTo(1))
		);

	public static final DeferredItem<Item> AMENAZADO_VISUAL = ITEMS.register("amenazado_visual",
		() -> new Item(new Item.Properties())
		);

	public static final DeferredItem<Item> ESCUDO_VISUAL = ITEMS.register("escudo_visual",
		() -> new Item(new Item.Properties())
		);

	public static final DeferredItem<Item> ELIMINADO_VISUAL = ITEMS.register("eliminado_visual",
		() -> new Item(new Item.Properties())
		);

	public static final DeferredItem<Item> BANDERIN_VISUAL = ITEMS.register("banderin_visual",
		() -> new Item(new Item.Properties())
		);

	public static final DeferredItem<Item> RING = ITEMS.register("ring", BaseRingItem::new);
	public static final DeferredItem<Item> RING_TEMPLATE = ITEMS.register("ring_template", () -> new SmithingTemplateItem(
		Component.translatable("item.quequeworld.ring_template.applicable_to").withStyle(ChatFormatting.BLUE),
		Component.translatable("item.quequeworld.ring_template.ingredients").withStyle(ChatFormatting.BLUE),
		Component.translatable("item.quequeworld.ring_template.title").withStyle(ChatFormatting.GRAY),
		Component.translatable("item.quequeworld.ring_template.base_slot_description"),
		Component.translatable("item.quequeworld.ring_template.additions_slot_description"),
		List.of(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "item/empty_slot_ring"), ResourceLocation.fromNamespaceAndPath("curios", "slot/empty_ring_slot")),
		List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_ingot"), ResourceLocation.withDefaultNamespace("item/empty_slot_redstone_dust"))
	));

	public static final DeferredItem<Item> RING_DIAMOND_1 = ITEMS.register("ring_diamond_1", () -> new DiamondRingItem(1));
	public static final DeferredItem<Item> RING_DIAMOND_2 = ITEMS.register("ring_diamond_2", () -> new DiamondRingItem(2));
	public static final DeferredItem<Item> RING_DIAMOND_3 = ITEMS.register("ring_diamond_3", () -> new DiamondRingItem(3));
	public static final DeferredItem<Item> RING_DIAMOND_4 = ITEMS.register("ring_diamond_4", () -> new DiamondRingItem(4));
	public static final DeferredItem<Item> RING_DIAMOND_5 = ITEMS.register("ring_diamond_5", () -> new DiamondRingItem(5));

	public static final DeferredItem<Item> RING_GOLD_1 = ITEMS.register("ring_gold_1", () -> new GoldRingItem(1));
	public static final DeferredItem<Item> RING_GOLD_2 = ITEMS.register("ring_gold_2", () -> new GoldRingItem(2));
	public static final DeferredItem<Item> RING_GOLD_3 = ITEMS.register("ring_gold_3", () -> new GoldRingItem(3));
	public static final DeferredItem<Item> RING_GOLD_4 = ITEMS.register("ring_gold_4", () -> new GoldRingItem(4));
	public static final DeferredItem<Item> RING_GOLD_5 = ITEMS.register("ring_gold_5", () -> new GoldRingItem(5));

	public static final DeferredItem<Item> RING_EMERALD_1 = ITEMS.register("ring_emerald_1", () -> new EmeraldRingItem(1));
	public static final DeferredItem<Item> RING_EMERALD_2 = ITEMS.register("ring_emerald_2", () -> new EmeraldRingItem(2));
	public static final DeferredItem<Item> RING_EMERALD_3 = ITEMS.register("ring_emerald_3", () -> new EmeraldRingItem(3));
	public static final DeferredItem<Item> RING_EMERALD_4 = ITEMS.register("ring_emerald_4", () -> new EmeraldRingItem(4));
	public static final DeferredItem<Item> RING_EMERALD_5 = ITEMS.register("ring_emerald_5", () -> new EmeraldRingItem(5));

	public static final DeferredItem<Item> RING_COPPER_1 = ITEMS.register("ring_copper_1", () -> new CopperRingItem(1));
	public static final DeferredItem<Item> RING_COPPER_2 = ITEMS.register("ring_copper_2", () -> new CopperRingItem(2));
	public static final DeferredItem<Item> RING_COPPER_3 = ITEMS.register("ring_copper_3", () -> new CopperRingItem(3));
	public static final DeferredItem<Item> RING_COPPER_4 = ITEMS.register("ring_copper_4", () -> new CopperRingItem(4));
	public static final DeferredItem<Item> RING_COPPER_5 = ITEMS.register("ring_copper_5", () -> new CopperRingItem(5));

	public static final DeferredItem<Item> RING_LAZULI_1 = ITEMS.register("ring_lazuli_1", () -> new LapisRingItem(1, 15));
	public static final DeferredItem<Item> RING_LAZULI_2 = ITEMS.register("ring_lazuli_2", () -> new LapisRingItem(2, 20));
	public static final DeferredItem<Item> RING_LAZULI_3 = ITEMS.register("ring_lazuli_3", () -> new LapisRingItem(3, 25));
	public static final DeferredItem<Item> RING_LAZULI_4 = ITEMS.register("ring_lazuli_4", () -> new LapisRingItem(4, 30));
	public static final DeferredItem<Item> RING_LAZULI_5 = ITEMS.register("ring_lazuli_5", () -> new LapisRingItem(5, 30));

	public static final DeferredItem<Item> RING_AMETHYST_1 = ITEMS.register("ring_amethyst_1", () -> new AmethystRingItem(1));
	public static final DeferredItem<Item> RING_AMETHYST_2 = ITEMS.register("ring_amethyst_2", () -> new AmethystRingItem(2));
	public static final DeferredItem<Item> RING_AMETHYST_3 = ITEMS.register("ring_amethyst_3", () -> new AmethystRingItem(3));
	public static final DeferredItem<Item> RING_AMETHYST_4 = ITEMS.register("ring_amethyst_4", () -> new AmethystRingItem(4));
	public static final DeferredItem<Item> RING_AMETHYST_5 = ITEMS.register("ring_amethyst_5", () -> new AmethystRingItem(5));

	public static final DeferredItem<Item> RING_NETHERITE_1 = ITEMS.register("ring_netherite_1", () -> new NetheriteRingItem(1, 5));
	public static final DeferredItem<Item> RING_NETHERITE_2 = ITEMS.register("ring_netherite_2", () -> new NetheriteRingItem(2, 6));
	public static final DeferredItem<Item> RING_NETHERITE_3 = ITEMS.register("ring_netherite_3", () -> new NetheriteRingItem(3, 6));
	public static final DeferredItem<Item> RING_NETHERITE_4 = ITEMS.register("ring_netherite_4", () -> new NetheriteRingItem(4, 7));
	public static final DeferredItem<Item> RING_NETHERITE_5 = ITEMS.register("ring_netherite_5", () -> new NetheriteRingItem(5, 7));

	public static final DeferredItem<Item> DESECHO_ESPECTRAL = ITEMS.register("desecho_espectral", () -> new Item(new Item.Properties().stacksTo(64)));

	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.INGREDIENTS) {
			tabData.accept(BISMUTH.get());
			tabData.accept(QUEQUE_TOKEN.get());
			tabData.accept(VALE_BANDERIN.get());
			tabData.accept(QUEQUE_CAPSULA.get());
			tabData.accept(RING_TEMPLATE.get());
			tabData.accept(DESECHO_ESPECTRAL.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			tabData.accept(UNIVERSAL_KEY.get());
			tabData.accept(ENDER_MIRROR.get());
			tabData.accept(QUEQUE_PEARL.get());
			tabData.accept(STICK_REDUCTOR.get());
			tabData.accept(STICK_GIANT.get());

			tabData.accept(STICK_CAMERA.get());
			tabData.accept(MEGAPHONE.get());
			tabData.accept(GLOBAL_ORB.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			tabData.accept(CACHAPORRA.get());

			tabData.accept(RING.get());
			tabData.accept(RING_DIAMOND_1.get());
			tabData.accept(RING_DIAMOND_2.get());
			tabData.accept(RING_DIAMOND_3.get());
			tabData.accept(RING_DIAMOND_4.get());
			tabData.accept(RING_DIAMOND_5.get());
			tabData.accept(RING_GOLD_1.get());
			tabData.accept(RING_GOLD_2.get());
			tabData.accept(RING_GOLD_3.get());
			tabData.accept(RING_GOLD_4.get());
			tabData.accept(RING_GOLD_5.get());
			tabData.accept(RING_EMERALD_1.get());
			tabData.accept(RING_EMERALD_2.get());
			tabData.accept(RING_EMERALD_3.get());
			tabData.accept(RING_EMERALD_4.get());
			tabData.accept(RING_EMERALD_5.get());
			tabData.accept(RING_COPPER_1.get());
			tabData.accept(RING_COPPER_2.get());
			tabData.accept(RING_COPPER_3.get());
			tabData.accept(RING_COPPER_4.get());
			tabData.accept(RING_COPPER_5.get());
			tabData.accept(RING_LAZULI_1.get());
			tabData.accept(RING_LAZULI_2.get());
			tabData.accept(RING_LAZULI_3.get());
			tabData.accept(RING_LAZULI_4.get());
			tabData.accept(RING_LAZULI_5.get());
			tabData.accept(RING_AMETHYST_1.get());
			tabData.accept(RING_AMETHYST_2.get());
			tabData.accept(RING_AMETHYST_3.get());
			tabData.accept(RING_AMETHYST_4.get());
			tabData.accept(RING_AMETHYST_5.get());
			tabData.accept(RING_NETHERITE_1.get());
			tabData.accept(RING_NETHERITE_2.get());
			tabData.accept(RING_NETHERITE_3.get());
			tabData.accept(RING_NETHERITE_4.get());
			tabData.accept(RING_NETHERITE_5.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
			tabData.accept(MUFFIN_GLITTER.get());
			tabData.accept(MUFFIN_GLOW.get());
		}
	}
}