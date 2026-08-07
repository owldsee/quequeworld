package net.mcreator.quequeworld.init;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.block.GachaMachineBlock;
import net.mcreator.quequeworld.block.ReductorBlock;
import net.mcreator.quequeworld.block.GianterBlock;
import net.mcreator.quequeworld.block.NormalizerBlock;
import net.mcreator.quequeworld.item.ModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import net.mcreator.quequeworld.item.GachaMachineBlockItem;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModBlocks {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(QuequeworldMod.MODID);

	public static final DeferredBlock<Block> GACHA_MACHINE = BLOCKS.register("gacha_machine",
		() -> new GachaMachineBlock(Block.Properties.of().strength(3.0F, 3.0F).noOcclusion()
			.lightLevel(state -> state.getValue(GachaMachineBlock.LIT) ? 12 : 0))
	);

	public static final DeferredItem<BlockItem> GACHA_MACHINE_ITEM = ModItems.ITEMS.register("gacha_machine",
		() -> new GachaMachineBlockItem(GACHA_MACHINE.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> REDUCTOR_BLOCK = BLOCKS.register("reductor_block",
		() -> new ReductorBlock(Block.Properties.of().strength(1.5F, 6.0F))
	);

	public static final DeferredItem<BlockItem> REDUCTOR_BLOCK_ITEM = ModItems.ITEMS.register("reductor_block",
		() -> new BlockItem(REDUCTOR_BLOCK.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> GIANTER_BLOCK = BLOCKS.register("gianter_block",
		() -> new GianterBlock(Block.Properties.of().strength(1.5F, 6.0F))
	);

	public static final DeferredItem<BlockItem> GIANTER_BLOCK_ITEM = ModItems.ITEMS.register("gianter_block",
		() -> new BlockItem(GIANTER_BLOCK.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> NORMALIZER_BLOCK = BLOCKS.register("normalizer_block",
		() -> new NormalizerBlock(Block.Properties.of().strength(1.5F, 6.0F))
	);

	public static final DeferredItem<BlockItem> NORMALIZER_BLOCK_ITEM = ModItems.ITEMS.register("normalizer_block",
		() -> new BlockItem(NORMALIZER_BLOCK.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> MINIGAME_SPAWNER = BLOCKS.register("minigame_spawner",
		() -> new net.mcreator.quequeworld.block.MinigameSpawnerBlock(Block.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion().noCollission().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).isValidSpawn((s, l, p, e) -> false))
	);

	public static final DeferredItem<BlockItem> MINIGAME_SPAWNER_ITEM = ModItems.ITEMS.register("minigame_spawner",
		() -> new BlockItem(MINIGAME_SPAWNER.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> GOAL_BELL = BLOCKS.register("goal_bell",
		() -> new net.mcreator.quequeworld.block.GoalBellBlock(Block.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);

	public static final DeferredItem<BlockItem> GOAL_BELL_ITEM = ModItems.ITEMS.register("goal_bell",
		() -> new BlockItem(GOAL_BELL.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> GOAL_TRIGGER = BLOCKS.register("goal_trigger",
		() -> new net.mcreator.quequeworld.block.GoalTriggerBlock(Block.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);

	public static final DeferredItem<BlockItem> GOAL_TRIGGER_ITEM = ModItems.ITEMS.register("goal_trigger",
		() -> new BlockItem(GOAL_TRIGGER.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> CHECKPOINT = BLOCKS.register("checkpoint",
		() -> new net.mcreator.quequeworld.block.CheckpointBlock(Block.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);

	public static final DeferredItem<BlockItem> CHECKPOINT_ITEM = ModItems.ITEMS.register("checkpoint",
		() -> new BlockItem(CHECKPOINT.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> INSTANT_KILL = BLOCKS.register("instant_kill",
		() -> new net.mcreator.quequeworld.block.InstantKillBlock(Block.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion())
	);

	public static final DeferredItem<BlockItem> INSTANT_KILL_ITEM = ModItems.ITEMS.register("instant_kill",
		() -> new BlockItem(INSTANT_KILL.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> SIGNAL_RECEIVER = BLOCKS.register("signal_receiver",
		() -> new net.mcreator.quequeworld.block.SignalReceiverBlock(Block.Properties.of().strength(1.5F, 6.0F))
	);

	public static final DeferredItem<BlockItem> SIGNAL_RECEIVER_ITEM = ModItems.ITEMS.register("signal_receiver",
		() -> new BlockItem(SIGNAL_RECEIVER.get(), new Item.Properties())
	);

	public static final DeferredBlock<Block> TERMINAL_BLOCK = BLOCKS.register("terminal_block",
		() -> new net.mcreator.quequeworld.block.TerminalBlock(Block.Properties.of().strength(1.5F, 6.0F))
	);

	public static final DeferredItem<BlockItem> TERMINAL_BLOCK_ITEM = ModItems.ITEMS.register("terminal_block",
		() -> new BlockItem(TERMINAL_BLOCK.get(), new Item.Properties())
	);

	public static void register(IEventBus eventBus) {
		BLOCKS.register(eventBus);
	}

	@SubscribeEvent
	public static void buildTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			event.accept(GACHA_MACHINE_ITEM.get());
			event.accept(REDUCTOR_BLOCK_ITEM.get());
			event.accept(GIANTER_BLOCK_ITEM.get());
			event.accept(NORMALIZER_BLOCK_ITEM.get());
			event.accept(MINIGAME_SPAWNER_ITEM.get());
			event.accept(SIGNAL_RECEIVER_ITEM.get());
			event.accept(TERMINAL_BLOCK_ITEM.get());
		}
	}
}
