package net.mcreator.quequeworld.client.model;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.block.entity.GachaMachineBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GachaMachineBlockModel extends GeoModel<GachaMachineBlockEntity> {
	@Override
	public ResourceLocation getModelResource(GachaMachineBlockEntity animatable) {
		return ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "geo/lucky_machine.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(GachaMachineBlockEntity animatable) {
		return ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/block/lucky_machine.png");
	}

	@Override
	public ResourceLocation getAnimationResource(GachaMachineBlockEntity animatable) {
		return ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "animations/lucky_machine.animation.json");
	}
}
