/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.quequeworld.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import net.minecraft.world.item.Item;

import net.mcreator.quequeworld.item.QCoinItem;
import net.mcreator.quequeworld.QuequeworldMod;

public class QuequeworldModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(QuequeworldMod.MODID);
	public static final DeferredItem<Item> Q_COIN;
	static {
		Q_COIN = REGISTRY.register("q_coin", QCoinItem::new);
	}
	// Start of user code block custom items
	// End of user code block custom items
}