package net.mcreator.quequeworld.init;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.block.entity.GachaMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;

public class ModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, QuequeworldMod.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GachaMachineBlockEntity>> GACHA_MACHINE_BE = BLOCK_ENTITIES.register("gacha_machine",
		() -> BlockEntityType.Builder.of(GachaMachineBlockEntity::new, ModBlocks.GACHA_MACHINE.get()).build(null)
	);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<net.mcreator.quequeworld.block.entity.MinigameSpawnerBlockEntity>> MINIGAME_SPAWNER_BE = BLOCK_ENTITIES.register("minigame_spawner",
		() -> BlockEntityType.Builder.of(net.mcreator.quequeworld.block.entity.MinigameSpawnerBlockEntity::new, ModBlocks.MINIGAME_SPAWNER.get()).build(null)
	);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<net.mcreator.quequeworld.block.entity.GoalBellBlockEntity>> GOAL_BELL_BE = BLOCK_ENTITIES.register("goal_bell",
		() -> BlockEntityType.Builder.of(net.mcreator.quequeworld.block.entity.GoalBellBlockEntity::new, ModBlocks.GOAL_BELL.get()).build(null)
	);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<net.mcreator.quequeworld.block.entity.SignalReceiverBlockEntity>> SIGNAL_RECEIVER_BE = BLOCK_ENTITIES.register("signal_receiver",
		() -> BlockEntityType.Builder.of(net.mcreator.quequeworld.block.entity.SignalReceiverBlockEntity::new, ModBlocks.SIGNAL_RECEIVER.get()).build(null)
	);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<net.mcreator.quequeworld.block.entity.TerminalBlockEntity>> TERMINAL_BE = BLOCK_ENTITIES.register("terminal_block",
		() -> BlockEntityType.Builder.of(net.mcreator.quequeworld.block.entity.TerminalBlockEntity::new, ModBlocks.TERMINAL_BLOCK.get()).build(null)
	);

	public static void register(IEventBus eventBus) {
		BLOCK_ENTITIES.register(eventBus);
	}
}
