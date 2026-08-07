package net.mcreator.quequeworld.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Optional;

public class QueQueCapsulaItem extends Item {
	public QueQueCapsulaItem() {
		super(new Item.Properties().stacksTo(64));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		
		// Solo procesamos la apertura en el servidor para evitar duplicaciones
		if (!level.isClientSide()) {
			CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
			if (customData != null) {
				CompoundTag tag = customData.copyTag();
				if (tag.contains("surprise_item")) {
					// Deserializar el item sorpresa usando el registro de la partida
					Optional<ItemStack> surpriseOpt = ItemStack.parse(level.registryAccess(), tag.getCompound("surprise_item"));
					if (surpriseOpt.isPresent() && !surpriseOpt.get().isEmpty()) {
						ItemStack surpriseStack = surpriseOpt.get();
						
						// Entregar el premio al jugador (o arrojarlo al suelo si no cabe)
						if (!player.getInventory().add(surpriseStack)) {
							player.drop(surpriseStack, false);
						}
						
						// Efectos visuales de partículas
						if (level instanceof ServerLevel serverLevel) {
							serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, 
								player.getX(), player.getY() + 1.0, player.getZ(), 
								15, 0.5, 0.5, 0.5, 0.15);
							serverLevel.sendParticles(ParticleTypes.INSTANT_EFFECT, 
								player.getX(), player.getY() + 1.0, player.getZ(), 
								10, 0.3, 0.3, 0.3, 0.1);
						}
						
						// Sonido de victoria/campanada
						level.playSound(null, player.getX(), player.getY(), player.getZ(), 
							SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.2F);
						level.playSound(null, player.getX(), player.getY(), player.getZ(), 
							SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 1.0F);
						
						// Consumir una cápsula
						if (!player.getAbilities().instabuild) {
							stack.shrink(1);
						}
						
						return InteractionResultHolder.consume(stack);
					}
				}
			}
			
			// Si no tiene nada dentro (caso de creativo/comandos)
			player.displayClientMessage(Component.literal("§cEsta cápsula está vacía."), true);
			return InteractionResultHolder.fail(stack);
		}
		
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.literal("§7Haz clic derecho para abrir y obtener tu premio."));
	}
}
