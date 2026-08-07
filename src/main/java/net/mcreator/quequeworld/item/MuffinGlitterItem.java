package net.mcreator.quequeworld.item;

import net.mcreator.quequeworld.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class MuffinGlitterItem extends Item {
	public MuffinGlitterItem() {
		super(new Item.Properties().stacksTo(64).food(
			new FoodProperties.Builder()
				.nutrition(4)
				.saturationModifier(0.3F)
				.alwaysEdible()
				.effect(() -> new MobEffectInstance(ModEffects.REDUCIDO, 8 * 60 * 20, 0, false, false, false), 1.0F)
				.build()
		));
	}
}
