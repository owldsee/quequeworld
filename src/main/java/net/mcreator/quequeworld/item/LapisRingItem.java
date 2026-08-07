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

public class LapisRingItem extends Item implements ICurioItem {
	private final int tier;

	public LapisRingItem(int tier, int maxDurabilityMinutes) {
		super(new Item.Properties().durability(maxDurabilityMinutes * 60 * 20)); // Durabilidad en ticks según minutos
		this.tier = tier;
	}

	public int getTier() {
		return this.tier;
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
		modifiers.put(Attributes.ARMOR, new AttributeModifier(id, 1.0D, AttributeModifier.Operation.ADD_VALUE));
		return modifiers;
	}

	@Override
	public void curioTick(SlotContext slotContext, ItemStack stack) {
		if (!slotContext.entity().level().isClientSide() && slotContext.entity() instanceof net.minecraft.server.level.ServerPlayer player) {
			if (player.tickCount % 20 == 0) { // Consumir por segundo para rendimiento
				stack.hurtAndBreak(20, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("§7Al estar equipado en slot de anillo:"));
		tooltip.add(Component.literal("§9+1 Armadura"));
		
		String xpMult = (this.tier == 5) ? "x3" : "x2";
		tooltip.add(Component.literal("§9" + xpMult + " Experiencia ganada"));
		
		int mins = (this.tier == 1) ? 15 : (this.tier == 2) ? 20 : (this.tier == 3) ? 25 : 30;
		tooltip.add(Component.literal("§8Duración: " + mins + " minutos de uso activo"));
	}
}
