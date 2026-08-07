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

public class EmeraldRingItem extends Item implements ICurioItem {
	private final int tier;

	public EmeraldRingItem(int tier) {
		super(new Item.Properties().durability(36000)); // 30 minutos de durabilidad activa
		this.tier = tier;
	}

	public int getTier() {
		return this.tier;
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
		modifiers.put(Attributes.ARMOR, new AttributeModifier(id, 1.0D, AttributeModifier.Operation.ADD_VALUE));
		modifiers.put(Attributes.LUCK, new AttributeModifier(id, 1.0D * this.tier, AttributeModifier.Operation.ADD_VALUE));
		return modifiers;
	}

	@Override
	public void curioTick(SlotContext slotContext, ItemStack stack) {
		if (!slotContext.entity().level().isClientSide() && slotContext.entity() instanceof net.minecraft.server.level.ServerPlayer player) {
			if (player.tickCount % 20 == 0) { // Consumir durabilidad sólo por segundo
				stack.hurtAndBreak(20, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("§7Al estar equipado en slot de anillo:"));
		tooltip.add(Component.literal("§9+1 Armadura"));
		tooltip.add(Component.literal("§9+" + this.tier + " Suerte"));
		
		int chance = (this.tier == 1 || this.tier == 2) ? 10 : (this.tier == 3 || this.tier == 4) ? 15 : 20;
		String coins = (this.tier == 1) ? "1 a 2" : (this.tier == 2 || this.tier == 3) ? "2 a 3" : "3 a 4";
		tooltip.add(Component.literal("§9" + chance + "% de obtener " + coins + " QCoins al matar monstruos"));
		tooltip.add(Component.literal("§8Duración: 30 minutos de uso activo"));
	}
}
