package net.mcreator.quequeworld.init;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.world.inventory.GachaMachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;

public class ModMenus {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, QuequeworldMod.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<GachaMachineMenu>> GACHA_MACHINE_MENU = MENUS.register("gacha_machine",
		() -> IMenuTypeExtension.create((windowId, inv, data) -> new GachaMachineMenu(windowId, inv, data))
	);

	public static final DeferredHolder<MenuType<?>, MenuType<net.mcreator.quequeworld.world.inventory.MinigameSpawnerMenu>> MINIGAME_SPAWNER_MENU = MENUS.register("minigame_spawner",
		() -> IMenuTypeExtension.create((windowId, inv, data) -> new net.mcreator.quequeworld.world.inventory.MinigameSpawnerMenu(windowId, inv, data))
	);

	public static final DeferredHolder<MenuType<?>, MenuType<net.mcreator.quequeworld.world.inventory.SignalReceiverMenu>> SIGNAL_RECEIVER_MENU = MENUS.register("signal_receiver",
		() -> IMenuTypeExtension.create((windowId, inv, data) -> new net.mcreator.quequeworld.world.inventory.SignalReceiverMenu(windowId, inv, data))
	);

	public static final DeferredHolder<MenuType<?>, MenuType<net.mcreator.quequeworld.world.inventory.TerminalConfigMenu>> TERMINAL_CONFIG_MENU = MENUS.register("terminal_config",
		() -> IMenuTypeExtension.create((windowId, inv, data) -> new net.mcreator.quequeworld.world.inventory.TerminalConfigMenu(windowId, inv, data))
	);

	public static final DeferredHolder<MenuType<?>, MenuType<net.mcreator.quequeworld.world.inventory.TerminalInteractMenu>> TERMINAL_INTERACT_MENU = MENUS.register("terminal_interact",
		() -> IMenuTypeExtension.create((windowId, inv, data) -> new net.mcreator.quequeworld.world.inventory.TerminalInteractMenu(windowId, inv, data))
	);

	public static void register(IEventBus eventBus) {
		MENUS.register(eventBus);
	}
}
