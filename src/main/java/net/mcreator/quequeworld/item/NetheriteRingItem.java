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

public class NetheriteRingItem extends Item implements ICurioItem {
	private final int tier;

	public NetheriteRingItem(int tier, int maxDurabilityMinutes) {
		super(new Item.Properties().durability(maxDurabilityMinutes * 60 * 20)); // Durabilidad en ticks
		this.tier = tier;
	}

	public int getTier() {
		return this.tier;
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
		modifiers.put(Attributes.ARMOR, new AttributeModifier(id, 1.0D, AttributeModifier.Operation.ADD_VALUE));
		modifiers.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, 0.05D, AttributeModifier.Operation.ADD_VALUE));
		return modifiers;
	}

	@Override
	public void curioTick(SlotContext slotContext, ItemStack stack) {
		if (!slotContext.entity().level().isClientSide() && slotContext.entity() instanceof net.minecraft.server.level.ServerPlayer player) {
			// Si está en fuego o en lava, consume 1 de durabilidad por tick y previene el fuego
			if (player.isOnFire() || player.isInLava() || player.isInWall()) {
				stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
				player.clearFire(); // Limpiar fuego del cliente/entidad
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("§7Al estar equipado en slot de anillo:"));
		tooltip.add(Component.literal("§9+1 Armadura"));
		tooltip.add(Component.literal("§9+5% Resistencia al retroceso"));
		
		int mins = (this.tier == 1) ? 5 : (this.tier == 2 || this.tier == 3) ? 6 : 7;
		tooltip.add(Component.literal("§9Inmunidad al fuego y lava §8(Dura " + mins + " min de uso activo)"));
		
		int explReduction = (this.tier == 1 || this.tier == 2) ? 4 : (this.tier == 3 || this.tier == 4) ? 8 : 16;
		tooltip.add(Component.literal("§9-" + explReduction + " Daño de explosiones §8(Consume 1s de duración por daño absorbido)"));
	}
}
