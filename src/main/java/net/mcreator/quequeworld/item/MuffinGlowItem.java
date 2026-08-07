package net.mcreator.quequeworld.item;

import net.mcreator.quequeworld.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class MuffinGlowItem extends Item {
	public MuffinGlowItem() {
		super(new Item.Properties().stacksTo(64).food(
			new FoodProperties.Builder()
				.nutrition(4)
				.saturationModifier(0.3F)
				.alwaysEdible()
				.effect(() -> new MobEffectInstance(ModEffects.GIGANTE, 8 * 60 * 20, 0, false, false, false), 1.0F)
				.build()
		));
	}
}
