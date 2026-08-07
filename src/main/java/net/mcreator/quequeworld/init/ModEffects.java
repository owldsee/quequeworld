package net.mcreator.quequeworld.init;

import net.mcreator.quequeworld.QuequeworldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, QuequeworldMod.MODID);

	public static final DeferredHolder<MobEffect, MobEffect> REDUCIDO = REGISTRY.register("reducido",
		() -> new MobEffect(MobEffectCategory.NEUTRAL, 0x8A2BE2) {}
			.addAttributeModifier(Attributes.SCALE, 
				ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "effect.reducido"), 
				-0.75, 
				AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
	);

	public static final DeferredHolder<MobEffect, MobEffect> GIGANTE = REGISTRY.register("gigante",
		() -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFF0000) {}
			.addAttributeModifier(Attributes.SCALE, 
				ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "effect.gigante"), 
				0.65, 
				AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
	);

	public static void register(IEventBus eventBus) {
		REGISTRY.register(eventBus);
	}
}
