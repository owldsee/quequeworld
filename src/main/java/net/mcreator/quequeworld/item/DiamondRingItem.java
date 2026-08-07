package net.mcreator.quequeworld.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import com.google.common.collect.Multimap;
import com.google.common.collect.HashMultimap;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import java.util.List;

public class DiamondRingItem extends Item implements ICurioItem {
	private final int tier;

	public DiamondRingItem(int tier) {
		super(new Item.Properties().durability(500)); // 500 usos
		this.tier = tier;
	}

	public int getTier() {
		return this.tier;
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
		double armor = (this.tier == 1) ? 2.0D : (this.tier == 2 || this.tier == 3) ? 3.0D : 4.0D;
		modifiers.put(Attributes.ARMOR, new AttributeModifier(id, armor, AttributeModifier.Operation.ADD_VALUE));
		return modifiers;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("§7Al estar equipado en slot de anillo:"));
		
		int armor = (this.tier == 1) ? 2 : (this.tier == 2 || this.tier == 3) ? 3 : 4;
		tooltip.add(Component.literal("§9+" + armor + " Armadura"));
		
		int percent = (this.tier == 1 || this.tier == 2) ? 25 : (this.tier == 3 || this.tier == 4) ? 50 : 75;
		tooltip.add(Component.literal("§9" + percent + "% de probabilidad de mitigar desgaste de herramientas y armaduras"));
		tooltip.add(Component.literal("§8Se desgasta al recibir daño y usar herramientas"));
	}
}
