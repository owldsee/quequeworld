package net.mcreator.quequeworld.init;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.item.ModItems;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.bus.api.IEventBus;

import java.util.List;
import java.util.ArrayList;

public class GodBlocks {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(QuequeworldMod.MODID);
	public static final List<DeferredItem<BlockItem>> GOD_BLOCK_ITEMS = new ArrayList<>();

	public static final DeferredBlock<Block> CLAY = BLOCKS.register("clay",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> CLAY_ITEM = registerBlockItem("clay", CLAY);

	public static final DeferredBlock<Block> TERRACOTTA = BLOCKS.register("terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> TERRACOTTA_ITEM = registerBlockItem("terracotta", TERRACOTTA);

	public static final DeferredBlock<Block> WHITE_TERRACOTTA = BLOCKS.register("white_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> WHITE_TERRACOTTA_ITEM = registerBlockItem("white_terracotta", WHITE_TERRACOTTA);

	public static final DeferredBlock<Block> ORANGE_TERRACOTTA = BLOCKS.register("orange_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> ORANGE_TERRACOTTA_ITEM = registerBlockItem("orange_terracotta", ORANGE_TERRACOTTA);

	public static final DeferredBlock<Block> MAGENTA_TERRACOTTA = BLOCKS.register("magenta_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> MAGENTA_TERRACOTTA_ITEM = registerBlockItem("magenta_terracotta", MAGENTA_TERRACOTTA);

	public static final DeferredBlock<Block> LIGHT_BLUE_TERRACOTTA = BLOCKS.register("light_blue_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIGHT_BLUE_TERRACOTTA_ITEM = registerBlockItem("light_blue_terracotta", LIGHT_BLUE_TERRACOTTA);

	public static final DeferredBlock<Block> YELLOW_TERRACOTTA = BLOCKS.register("yellow_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> YELLOW_TERRACOTTA_ITEM = registerBlockItem("yellow_terracotta", YELLOW_TERRACOTTA);

	public static final DeferredBlock<Block> LIME_TERRACOTTA = BLOCKS.register("lime_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIME_TERRACOTTA_ITEM = registerBlockItem("lime_terracotta", LIME_TERRACOTTA);

	public static final DeferredBlock<Block> PINK_TERRACOTTA = BLOCKS.register("pink_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> PINK_TERRACOTTA_ITEM = registerBlockItem("pink_terracotta", PINK_TERRACOTTA);

	public static final DeferredBlock<Block> GRAY_TERRACOTTA = BLOCKS.register("gray_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> GRAY_TERRACOTTA_ITEM = registerBlockItem("gray_terracotta", GRAY_TERRACOTTA);

	public static final DeferredBlock<Block> LIGHT_GRAY_TERRACOTTA = BLOCKS.register("light_gray_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIGHT_GRAY_TERRACOTTA_ITEM = registerBlockItem("light_gray_terracotta", LIGHT_GRAY_TERRACOTTA);

	public static final DeferredBlock<Block> CYAN_TERRACOTTA = BLOCKS.register("cyan_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> CYAN_TERRACOTTA_ITEM = registerBlockItem("cyan_terracotta", CYAN_TERRACOTTA);

	public static final DeferredBlock<Block> PURPLE_TERRACOTTA = BLOCKS.register("purple_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> PURPLE_TERRACOTTA_ITEM = registerBlockItem("purple_terracotta", PURPLE_TERRACOTTA);

	public static final DeferredBlock<Block> BLUE_TERRACOTTA = BLOCKS.register("blue_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BLUE_TERRACOTTA_ITEM = registerBlockItem("blue_terracotta", BLUE_TERRACOTTA);

	public static final DeferredBlock<Block> BROWN_TERRACOTTA = BLOCKS.register("brown_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BROWN_TERRACOTTA_ITEM = registerBlockItem("brown_terracotta", BROWN_TERRACOTTA);

	public static final DeferredBlock<Block> GREEN_TERRACOTTA = BLOCKS.register("green_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> GREEN_TERRACOTTA_ITEM = registerBlockItem("green_terracotta", GREEN_TERRACOTTA);

	public static final DeferredBlock<Block> RED_TERRACOTTA = BLOCKS.register("red_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> RED_TERRACOTTA_ITEM = registerBlockItem("red_terracotta", RED_TERRACOTTA);

	public static final DeferredBlock<Block> BLACK_TERRACOTTA = BLOCKS.register("black_terracotta",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BLACK_TERRACOTTA_ITEM = registerBlockItem("black_terracotta", BLACK_TERRACOTTA);

	public static final DeferredBlock<Block> WHITE_GLAZED_TERRACOTTA = BLOCKS.register("white_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> WHITE_GLAZED_TERRACOTTA_ITEM = registerBlockItem("white_glazed_terracotta", WHITE_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> ORANGE_GLAZED_TERRACOTTA = BLOCKS.register("orange_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> ORANGE_GLAZED_TERRACOTTA_ITEM = registerBlockItem("orange_glazed_terracotta", ORANGE_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> MAGENTA_GLAZED_TERRACOTTA = BLOCKS.register("magenta_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> MAGENTA_GLAZED_TERRACOTTA_ITEM = registerBlockItem("magenta_glazed_terracotta", MAGENTA_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> LIGHT_BLUE_GLAZED_TERRACOTTA = BLOCKS.register("light_blue_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIGHT_BLUE_GLAZED_TERRACOTTA_ITEM = registerBlockItem("light_blue_glazed_terracotta", LIGHT_BLUE_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> YELLOW_GLAZED_TERRACOTTA = BLOCKS.register("yellow_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> YELLOW_GLAZED_TERRACOTTA_ITEM = registerBlockItem("yellow_glazed_terracotta", YELLOW_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> LIME_GLAZED_TERRACOTTA = BLOCKS.register("lime_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIME_GLAZED_TERRACOTTA_ITEM = registerBlockItem("lime_glazed_terracotta", LIME_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> PINK_GLAZED_TERRACOTTA = BLOCKS.register("pink_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> PINK_GLAZED_TERRACOTTA_ITEM = registerBlockItem("pink_glazed_terracotta", PINK_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> GRAY_GLAZED_TERRACOTTA = BLOCKS.register("gray_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> GRAY_GLAZED_TERRACOTTA_ITEM = registerBlockItem("gray_glazed_terracotta", GRAY_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> LIGHT_GRAY_GLAZED_TERRACOTTA = BLOCKS.register("light_gray_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIGHT_GRAY_GLAZED_TERRACOTTA_ITEM = registerBlockItem("light_gray_glazed_terracotta", LIGHT_GRAY_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> CYAN_GLAZED_TERRACOTTA = BLOCKS.register("cyan_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> CYAN_GLAZED_TERRACOTTA_ITEM = registerBlockItem("cyan_glazed_terracotta", CYAN_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> PURPLE_GLAZED_TERRACOTTA = BLOCKS.register("purple_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> PURPLE_GLAZED_TERRACOTTA_ITEM = registerBlockItem("purple_glazed_terracotta", PURPLE_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> BLUE_GLAZED_TERRACOTTA = BLOCKS.register("blue_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BLUE_GLAZED_TERRACOTTA_ITEM = registerBlockItem("blue_glazed_terracotta", BLUE_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> BROWN_GLAZED_TERRACOTTA = BLOCKS.register("brown_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BROWN_GLAZED_TERRACOTTA_ITEM = registerBlockItem("brown_glazed_terracotta", BROWN_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> GREEN_GLAZED_TERRACOTTA = BLOCKS.register("green_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> GREEN_GLAZED_TERRACOTTA_ITEM = registerBlockItem("green_glazed_terracotta", GREEN_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> RED_GLAZED_TERRACOTTA = BLOCKS.register("red_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> RED_GLAZED_TERRACOTTA_ITEM = registerBlockItem("red_glazed_terracotta", RED_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> BLACK_GLAZED_TERRACOTTA = BLOCKS.register("black_glazed_terracotta",
		() -> new GlazedTerracottaBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BLACK_GLAZED_TERRACOTTA_ITEM = registerBlockItem("black_glazed_terracotta", BLACK_GLAZED_TERRACOTTA);

	public static final DeferredBlock<Block> WHITE_CONCRETE = BLOCKS.register("white_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> WHITE_CONCRETE_ITEM = registerBlockItem("white_concrete", WHITE_CONCRETE);

	public static final DeferredBlock<Block> ORANGE_CONCRETE = BLOCKS.register("orange_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> ORANGE_CONCRETE_ITEM = registerBlockItem("orange_concrete", ORANGE_CONCRETE);

	public static final DeferredBlock<Block> MAGENTA_CONCRETE = BLOCKS.register("magenta_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> MAGENTA_CONCRETE_ITEM = registerBlockItem("magenta_concrete", MAGENTA_CONCRETE);

	public static final DeferredBlock<Block> LIGHT_BLUE_CONCRETE = BLOCKS.register("light_blue_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIGHT_BLUE_CONCRETE_ITEM = registerBlockItem("light_blue_concrete", LIGHT_BLUE_CONCRETE);

	public static final DeferredBlock<Block> YELLOW_CONCRETE = BLOCKS.register("yellow_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> YELLOW_CONCRETE_ITEM = registerBlockItem("yellow_concrete", YELLOW_CONCRETE);

	public static final DeferredBlock<Block> LIME_CONCRETE = BLOCKS.register("lime_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIME_CONCRETE_ITEM = registerBlockItem("lime_concrete", LIME_CONCRETE);

	public static final DeferredBlock<Block> PINK_CONCRETE = BLOCKS.register("pink_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> PINK_CONCRETE_ITEM = registerBlockItem("pink_concrete", PINK_CONCRETE);

	public static final DeferredBlock<Block> GRAY_CONCRETE = BLOCKS.register("gray_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> GRAY_CONCRETE_ITEM = registerBlockItem("gray_concrete", GRAY_CONCRETE);

	public static final DeferredBlock<Block> LIGHT_GRAY_CONCRETE = BLOCKS.register("light_gray_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIGHT_GRAY_CONCRETE_ITEM = registerBlockItem("light_gray_concrete", LIGHT_GRAY_CONCRETE);

	public static final DeferredBlock<Block> CYAN_CONCRETE = BLOCKS.register("cyan_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> CYAN_CONCRETE_ITEM = registerBlockItem("cyan_concrete", CYAN_CONCRETE);

	public static final DeferredBlock<Block> PURPLE_CONCRETE = BLOCKS.register("purple_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> PURPLE_CONCRETE_ITEM = registerBlockItem("purple_concrete", PURPLE_CONCRETE);

	public static final DeferredBlock<Block> BLUE_CONCRETE = BLOCKS.register("blue_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BLUE_CONCRETE_ITEM = registerBlockItem("blue_concrete", BLUE_CONCRETE);

	public static final DeferredBlock<Block> BROWN_CONCRETE = BLOCKS.register("brown_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BROWN_CONCRETE_ITEM = registerBlockItem("brown_concrete", BROWN_CONCRETE);

	public static final DeferredBlock<Block> GREEN_CONCRETE = BLOCKS.register("green_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> GREEN_CONCRETE_ITEM = registerBlockItem("green_concrete", GREEN_CONCRETE);

	public static final DeferredBlock<Block> RED_CONCRETE = BLOCKS.register("red_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> RED_CONCRETE_ITEM = registerBlockItem("red_concrete", RED_CONCRETE);

	public static final DeferredBlock<Block> BLACK_CONCRETE = BLOCKS.register("black_concrete",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BLACK_CONCRETE_ITEM = registerBlockItem("black_concrete", BLACK_CONCRETE);

	public static final DeferredBlock<Block> WHITE_CONCRETE_POWDER = BLOCKS.register("white_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> WHITE_CONCRETE_POWDER_ITEM = registerBlockItem("white_concrete_powder", WHITE_CONCRETE_POWDER);

	public static final DeferredBlock<Block> ORANGE_CONCRETE_POWDER = BLOCKS.register("orange_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> ORANGE_CONCRETE_POWDER_ITEM = registerBlockItem("orange_concrete_powder", ORANGE_CONCRETE_POWDER);

	public static final DeferredBlock<Block> MAGENTA_CONCRETE_POWDER = BLOCKS.register("magenta_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> MAGENTA_CONCRETE_POWDER_ITEM = registerBlockItem("magenta_concrete_powder", MAGENTA_CONCRETE_POWDER);

	public static final DeferredBlock<Block> LIGHT_BLUE_CONCRETE_POWDER = BLOCKS.register("light_blue_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIGHT_BLUE_CONCRETE_POWDER_ITEM = registerBlockItem("light_blue_concrete_powder", LIGHT_BLUE_CONCRETE_POWDER);

	public static final DeferredBlock<Block> YELLOW_CONCRETE_POWDER = BLOCKS.register("yellow_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> YELLOW_CONCRETE_POWDER_ITEM = registerBlockItem("yellow_concrete_powder", YELLOW_CONCRETE_POWDER);

	public static final DeferredBlock<Block> LIME_CONCRETE_POWDER = BLOCKS.register("lime_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIME_CONCRETE_POWDER_ITEM = registerBlockItem("lime_concrete_powder", LIME_CONCRETE_POWDER);

	public static final DeferredBlock<Block> PINK_CONCRETE_POWDER = BLOCKS.register("pink_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> PINK_CONCRETE_POWDER_ITEM = registerBlockItem("pink_concrete_powder", PINK_CONCRETE_POWDER);

	public static final DeferredBlock<Block> GRAY_CONCRETE_POWDER = BLOCKS.register("gray_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> GRAY_CONCRETE_POWDER_ITEM = registerBlockItem("gray_concrete_powder", GRAY_CONCRETE_POWDER);

	public static final DeferredBlock<Block> LIGHT_GRAY_CONCRETE_POWDER = BLOCKS.register("light_gray_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> LIGHT_GRAY_CONCRETE_POWDER_ITEM = registerBlockItem("light_gray_concrete_powder", LIGHT_GRAY_CONCRETE_POWDER);

	public static final DeferredBlock<Block> CYAN_CONCRETE_POWDER = BLOCKS.register("cyan_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> CYAN_CONCRETE_POWDER_ITEM = registerBlockItem("cyan_concrete_powder", CYAN_CONCRETE_POWDER);

	public static final DeferredBlock<Block> PURPLE_CONCRETE_POWDER = BLOCKS.register("purple_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> PURPLE_CONCRETE_POWDER_ITEM = registerBlockItem("purple_concrete_powder", PURPLE_CONCRETE_POWDER);

	public static final DeferredBlock<Block> BLUE_CONCRETE_POWDER = BLOCKS.register("blue_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BLUE_CONCRETE_POWDER_ITEM = registerBlockItem("blue_concrete_powder", BLUE_CONCRETE_POWDER);

	public static final DeferredBlock<Block> BROWN_CONCRETE_POWDER = BLOCKS.register("brown_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BROWN_CONCRETE_POWDER_ITEM = registerBlockItem("brown_concrete_powder", BROWN_CONCRETE_POWDER);

	public static final DeferredBlock<Block> GREEN_CONCRETE_POWDER = BLOCKS.register("green_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> GREEN_CONCRETE_POWDER_ITEM = registerBlockItem("green_concrete_powder", GREEN_CONCRETE_POWDER);

	public static final DeferredBlock<Block> RED_CONCRETE_POWDER = BLOCKS.register("red_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> RED_CONCRETE_POWDER_ITEM = registerBlockItem("red_concrete_powder", RED_CONCRETE_POWDER);

	public static final DeferredBlock<Block> BLACK_CONCRETE_POWDER = BLOCKS.register("black_concrete_powder",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F))
	);
	public static final DeferredItem<BlockItem> BLACK_CONCRETE_POWDER_ITEM = registerBlockItem("black_concrete_powder", BLACK_CONCRETE_POWDER);

	public static final DeferredBlock<Block> ICE = BLOCKS.register("ice",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).friction(0.98F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> ICE_ITEM = registerBlockItem("ice", ICE);

	public static final DeferredBlock<Block> PACKED_ICE = BLOCKS.register("packed_ice",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).friction(0.98F))
	);
	public static final DeferredItem<BlockItem> PACKED_ICE_ITEM = registerBlockItem("packed_ice", PACKED_ICE);

	public static final DeferredBlock<Block> BLUE_ICE = BLOCKS.register("blue_ice",
		() -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).friction(0.989F))
	);
	public static final DeferredItem<BlockItem> BLUE_ICE_ITEM = registerBlockItem("blue_ice", BLUE_ICE);

	public static final DeferredBlock<Block> GLASS = BLOCKS.register("glass",
		() -> new TransparentBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> GLASS_ITEM = registerBlockItem("glass", GLASS);

	public static final DeferredBlock<Block> TINTED_GLASS = BLOCKS.register("tinted_glass",
		() -> new TintedGlassBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> TINTED_GLASS_ITEM = registerBlockItem("tinted_glass", TINTED_GLASS);

	public static final DeferredBlock<Block> WHITE_STAINED_GLASS = BLOCKS.register("white_stained_glass",
		() -> new StainedGlassBlock(DyeColor.WHITE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> WHITE_STAINED_GLASS_ITEM = registerBlockItem("white_stained_glass", WHITE_STAINED_GLASS);

	public static final DeferredBlock<Block> ORANGE_STAINED_GLASS = BLOCKS.register("orange_stained_glass",
		() -> new StainedGlassBlock(DyeColor.ORANGE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> ORANGE_STAINED_GLASS_ITEM = registerBlockItem("orange_stained_glass", ORANGE_STAINED_GLASS);

	public static final DeferredBlock<Block> MAGENTA_STAINED_GLASS = BLOCKS.register("magenta_stained_glass",
		() -> new StainedGlassBlock(DyeColor.MAGENTA, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> MAGENTA_STAINED_GLASS_ITEM = registerBlockItem("magenta_stained_glass", MAGENTA_STAINED_GLASS);

	public static final DeferredBlock<Block> LIGHT_BLUE_STAINED_GLASS = BLOCKS.register("light_blue_stained_glass",
		() -> new StainedGlassBlock(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> LIGHT_BLUE_STAINED_GLASS_ITEM = registerBlockItem("light_blue_stained_glass", LIGHT_BLUE_STAINED_GLASS);

	public static final DeferredBlock<Block> YELLOW_STAINED_GLASS = BLOCKS.register("yellow_stained_glass",
		() -> new StainedGlassBlock(DyeColor.YELLOW, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> YELLOW_STAINED_GLASS_ITEM = registerBlockItem("yellow_stained_glass", YELLOW_STAINED_GLASS);

	public static final DeferredBlock<Block> LIME_STAINED_GLASS = BLOCKS.register("lime_stained_glass",
		() -> new StainedGlassBlock(DyeColor.LIME, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> LIME_STAINED_GLASS_ITEM = registerBlockItem("lime_stained_glass", LIME_STAINED_GLASS);

	public static final DeferredBlock<Block> PINK_STAINED_GLASS = BLOCKS.register("pink_stained_glass",
		() -> new StainedGlassBlock(DyeColor.PINK, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> PINK_STAINED_GLASS_ITEM = registerBlockItem("pink_stained_glass", PINK_STAINED_GLASS);

	public static final DeferredBlock<Block> GRAY_STAINED_GLASS = BLOCKS.register("gray_stained_glass",
		() -> new StainedGlassBlock(DyeColor.GRAY, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> GRAY_STAINED_GLASS_ITEM = registerBlockItem("gray_stained_glass", GRAY_STAINED_GLASS);

	public static final DeferredBlock<Block> LIGHT_GRAY_STAINED_GLASS = BLOCKS.register("light_gray_stained_glass",
		() -> new StainedGlassBlock(DyeColor.LIGHT_GRAY, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> LIGHT_GRAY_STAINED_GLASS_ITEM = registerBlockItem("light_gray_stained_glass", LIGHT_GRAY_STAINED_GLASS);

	public static final DeferredBlock<Block> CYAN_STAINED_GLASS = BLOCKS.register("cyan_stained_glass",
		() -> new StainedGlassBlock(DyeColor.CYAN, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> CYAN_STAINED_GLASS_ITEM = registerBlockItem("cyan_stained_glass", CYAN_STAINED_GLASS);

	public static final DeferredBlock<Block> PURPLE_STAINED_GLASS = BLOCKS.register("purple_stained_glass",
		() -> new StainedGlassBlock(DyeColor.PURPLE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> PURPLE_STAINED_GLASS_ITEM = registerBlockItem("purple_stained_glass", PURPLE_STAINED_GLASS);

	public static final DeferredBlock<Block> BLUE_STAINED_GLASS = BLOCKS.register("blue_stained_glass",
		() -> new StainedGlassBlock(DyeColor.BLUE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> BLUE_STAINED_GLASS_ITEM = registerBlockItem("blue_stained_glass", BLUE_STAINED_GLASS);

	public static final DeferredBlock<Block> BROWN_STAINED_GLASS = BLOCKS.register("brown_stained_glass",
		() -> new StainedGlassBlock(DyeColor.BROWN, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> BROWN_STAINED_GLASS_ITEM = registerBlockItem("brown_stained_glass", BROWN_STAINED_GLASS);

	public static final DeferredBlock<Block> GREEN_STAINED_GLASS = BLOCKS.register("green_stained_glass",
		() -> new StainedGlassBlock(DyeColor.GREEN, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> GREEN_STAINED_GLASS_ITEM = registerBlockItem("green_stained_glass", GREEN_STAINED_GLASS);

	public static final DeferredBlock<Block> RED_STAINED_GLASS = BLOCKS.register("red_stained_glass",
		() -> new StainedGlassBlock(DyeColor.RED, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> RED_STAINED_GLASS_ITEM = registerBlockItem("red_stained_glass", RED_STAINED_GLASS);

	public static final DeferredBlock<Block> BLACK_STAINED_GLASS = BLOCKS.register("black_stained_glass",
		() -> new StainedGlassBlock(DyeColor.BLACK, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> BLACK_STAINED_GLASS_ITEM = registerBlockItem("black_stained_glass", BLACK_STAINED_GLASS);

	public static final DeferredBlock<Block> GLASS_PANE = BLOCKS.register("glass_pane",
		() -> new IronBarsBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> GLASS_PANE_ITEM = registerBlockItem("glass_pane", GLASS_PANE);

	public static final DeferredBlock<Block> WHITE_STAINED_GLASS_PANE = BLOCKS.register("white_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.WHITE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> WHITE_STAINED_GLASS_PANE_ITEM = registerBlockItem("white_stained_glass_pane", WHITE_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> ORANGE_STAINED_GLASS_PANE = BLOCKS.register("orange_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.ORANGE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> ORANGE_STAINED_GLASS_PANE_ITEM = registerBlockItem("orange_stained_glass_pane", ORANGE_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> MAGENTA_STAINED_GLASS_PANE = BLOCKS.register("magenta_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.MAGENTA, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> MAGENTA_STAINED_GLASS_PANE_ITEM = registerBlockItem("magenta_stained_glass_pane", MAGENTA_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> LIGHT_BLUE_STAINED_GLASS_PANE = BLOCKS.register("light_blue_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> LIGHT_BLUE_STAINED_GLASS_PANE_ITEM = registerBlockItem("light_blue_stained_glass_pane", LIGHT_BLUE_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> YELLOW_STAINED_GLASS_PANE = BLOCKS.register("yellow_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.YELLOW, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> YELLOW_STAINED_GLASS_PANE_ITEM = registerBlockItem("yellow_stained_glass_pane", YELLOW_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> LIME_STAINED_GLASS_PANE = BLOCKS.register("lime_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.LIME, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> LIME_STAINED_GLASS_PANE_ITEM = registerBlockItem("lime_stained_glass_pane", LIME_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> PINK_STAINED_GLASS_PANE = BLOCKS.register("pink_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.PINK, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> PINK_STAINED_GLASS_PANE_ITEM = registerBlockItem("pink_stained_glass_pane", PINK_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> GRAY_STAINED_GLASS_PANE = BLOCKS.register("gray_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.GRAY, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> GRAY_STAINED_GLASS_PANE_ITEM = registerBlockItem("gray_stained_glass_pane", GRAY_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> LIGHT_GRAY_STAINED_GLASS_PANE = BLOCKS.register("light_gray_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.LIGHT_GRAY, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> LIGHT_GRAY_STAINED_GLASS_PANE_ITEM = registerBlockItem("light_gray_stained_glass_pane", LIGHT_GRAY_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> CYAN_STAINED_GLASS_PANE = BLOCKS.register("cyan_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.CYAN, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> CYAN_STAINED_GLASS_PANE_ITEM = registerBlockItem("cyan_stained_glass_pane", CYAN_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> PURPLE_STAINED_GLASS_PANE = BLOCKS.register("purple_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.PURPLE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> PURPLE_STAINED_GLASS_PANE_ITEM = registerBlockItem("purple_stained_glass_pane", PURPLE_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> BLUE_STAINED_GLASS_PANE = BLOCKS.register("blue_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.BLUE, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> BLUE_STAINED_GLASS_PANE_ITEM = registerBlockItem("blue_stained_glass_pane", BLUE_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> BROWN_STAINED_GLASS_PANE = BLOCKS.register("brown_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.BROWN, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> BROWN_STAINED_GLASS_PANE_ITEM = registerBlockItem("brown_stained_glass_pane", BROWN_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> GREEN_STAINED_GLASS_PANE = BLOCKS.register("green_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.GREEN, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> GREEN_STAINED_GLASS_PANE_ITEM = registerBlockItem("green_stained_glass_pane", GREEN_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> RED_STAINED_GLASS_PANE = BLOCKS.register("red_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.RED, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> RED_STAINED_GLASS_PANE_ITEM = registerBlockItem("red_stained_glass_pane", RED_STAINED_GLASS_PANE);

	public static final DeferredBlock<Block> BLACK_STAINED_GLASS_PANE = BLOCKS.register("black_stained_glass_pane",
		() -> new StainedGlassPaneBlock(DyeColor.BLACK, BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);
	public static final DeferredItem<BlockItem> BLACK_STAINED_GLASS_PANE_ITEM = registerBlockItem("black_stained_glass_pane", BLACK_STAINED_GLASS_PANE);

	private static DeferredItem<BlockItem> registerBlockItem(String id, DeferredBlock<Block> block) {
		DeferredItem<BlockItem> item = ModItems.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
		GOD_BLOCK_ITEMS.add(item);
		return item;
	}

	public static void register(IEventBus eventBus) {
		BLOCKS.register(eventBus);
	}
}
