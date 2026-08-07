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

public class AmethystRingItem extends Item implements ICurioItem {
	private final int tier;

	public AmethystRingItem(int tier) {
		super(new Item.Properties().durability(250)); // 250 usos
		this.tier = tier;
	}

	public int getTier() {
		return this.tier;
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
		modifiers.put(Attributes.ARMOR, new AttributeModifier(id, 1.0D, AttributeModifier.Operation.ADD_VALUE));
		
		double reach = (this.tier == 1 || this.tier == 2) ? 1.0D : (this.tier == 3 || this.tier == 4) ? 2.0D : 3.0D;
		modifiers.put(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(id, reach, AttributeModifier.Operation.ADD_VALUE));
		modifiers.put(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(id, reach, AttributeModifier.Operation.ADD_VALUE));
		
		return modifiers;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("§7Al estar equipado en slot de anillo:"));
		tooltip.add(Component.literal("§9+1 Armadura"));
		
		int reach = (this.tier == 1 || this.tier == 2) ? 1 : (this.tier == 3 || this.tier == 4) ? 2 : 3;
		tooltip.add(Component.literal("§9+" + reach + " Rango de picar y colocar bloques"));
		
		if (this.tier >= 2) {
			int projProt = (this.tier == 2 || this.tier == 3) ? 2 : 4;
			tooltip.add(Component.literal("§9+" + projProt + " Protección contra daño de proyectiles"));
		}
		tooltip.add(Component.literal("§8Se desgasta al picar y colocar bloques"));
	}
}
