package net.mcreator.quequeworld.client.renderer;

import net.mcreator.quequeworld.block.entity.GachaMachineBlockEntity;
import net.mcreator.quequeworld.client.model.GachaMachineBlockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GachaMachineBlockRenderer extends GeoBlockRenderer<GachaMachineBlockEntity> {
	public GachaMachineBlockRenderer(BlockEntityRendererProvider.Context context) {
		super(new GachaMachineBlockModel());
	}
}
