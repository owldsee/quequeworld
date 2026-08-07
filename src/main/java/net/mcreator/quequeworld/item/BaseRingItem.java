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

public class BaseRingItem extends Item implements ICurioItem {
	public BaseRingItem() {
		super(new Item.Properties().stacksTo(1));
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
		modifiers.put(Attributes.ARMOR, new AttributeModifier(id, 1.0D, AttributeModifier.Operation.ADD_VALUE));
		return modifiers;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("§7Al estar equipado en slot de anillo:"));
		tooltip.add(Component.literal("§9+1 Armadura"));
	}
}
