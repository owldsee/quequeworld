package net.mcreator.quequeworld.client.model;

import net.mcreator.quequeworld.QuequeworldMod;
import net.mcreator.quequeworld.item.GachaMachineBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GachaMachineItemModel extends GeoModel<GachaMachineBlockItem> {
	@Override
	public ResourceLocation getModelResource(GachaMachineBlockItem animatable) {
		return ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "geo/lucky_machine.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(GachaMachineBlockItem animatable) {
		return ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "textures/block/lucky_machine.png");
	}

	@Override
	public ResourceLocation getAnimationResource(GachaMachineBlockItem animatable) {
		return ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "animations/lucky_machine.animation.json");
	}
}
