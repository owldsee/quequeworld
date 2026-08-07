package net.mcreator.quequeworld.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

public class QueQuePearlItem extends Item {
	public QueQuePearlItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData != null) {
			CompoundTag tag = customData.copyTag();
			if (tag.contains("queque_pearl_data")) {
				return tag.getCompound("queque_pearl_data").getBoolean("is_marked");
			}
		}
		return super.isFoil(stack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}

		CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		CompoundTag rootTag = customData.copyTag();
		CompoundTag pearlData = rootTag.getCompound("queque_pearl_data");

		boolean isMarked = pearlData.getBoolean("is_marked");

		if (!isMarked) {
			if (!player.isCreative() && player.experienceLevel < 2) {
				level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
				player.displayClientMessage(Component.literal("§cSe requieren 2 niveles de experiencia para marcar la perla."), true);
				return InteractionResultHolder.fail(stack);
			}

			// ESTADO 1: Guardar la posición del jugador
			CompoundTag newPearlData = new CompoundTag();
			newPearlData.putDouble("x", player.getX());
			newPearlData.putDouble("y", player.getY());
			newPearlData.putDouble("z", player.getZ());
			newPearlData.putFloat("yaw", player.getYRot());
			newPearlData.putFloat("pitch", player.getXRot());
			newPearlData.putString("dimension", player.level().dimension().location().toString());
			newPearlData.putBoolean("is_marked", true);

			rootTag.put("queque_pearl_data", newPearlData);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(rootTag));

			if (!player.isCreative()) {
				player.giveExperienceLevels(-2);
			}

			// Reproducir sonido y partículas
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
			
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
			}

			player.displayClientMessage(Component.literal("§aUbicación guardada en la perla (-2 niveles de EXP)"), true);
			return InteractionResultHolder.success(stack);
		} else {
			// ESTADO 2: Teletransportar al jugador
			double x = pearlData.getDouble("x");
			double y = pearlData.getDouble("y");
			double z = pearlData.getDouble("z");
			float yaw = pearlData.getFloat("yaw");
			float pitch = pearlData.getFloat("pitch");
			String dimensionStr = pearlData.getString("dimension");

			// Verificar si se encuentra en la dimensión en donde se colocó la marca
			String currentDimStr = player.level().dimension().location().toString();
			if (!currentDimStr.equals(dimensionStr)) {
				level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
				player.displayClientMessage(Component.literal("§cLa perla pertenece a otra dimensión."), true);
				return InteractionResultHolder.fail(stack);
			}

			if (player instanceof ServerPlayer serverPlayer) {
				try {
					ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionStr));
					ServerLevel targetLevel = serverPlayer.server.getLevel(targetDim);
					if (targetLevel != null) {
						// Guardar coords y timer en NBT para teleportación retardada
						CompoundTag pData = player.getPersistentData();
						pData.putInt("pearl_teleport_timer", 8);
						pData.putDouble("pearl_teleport_x", x);
						pData.putDouble("pearl_teleport_y", y);
						pData.putDouble("pearl_teleport_z", z);
						pData.putFloat("pearl_teleport_yaw", yaw);
						pData.putFloat("pearl_teleport_pitch", pitch);
						pData.putString("pearl_teleport_dim", dimensionStr);

						// Sonido de carga de magia
						level.playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
						level.playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

						return InteractionResultHolder.success(stack);
					}
				} catch (Exception e) {
					player.displayClientMessage(Component.literal("§cError al leer las coordenadas de teletransporte."), true);
				}
			}
		}

		return InteractionResultHolder.pass(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData != null) {
			CompoundTag tag = customData.copyTag();
			if (tag.contains("queque_pearl_data")) {
				CompoundTag pearlData = tag.getCompound("queque_pearl_data");
				if (pearlData.getBoolean("is_marked")) {
					double x = pearlData.getDouble("x");
					double y = pearlData.getDouble("y");
					double z = pearlData.getDouble("z");
					String dimensionStr = pearlData.getString("dimension");
					// Formatear dimensión para que sea más legible (ej: minecraft:overworld -> Overworld)
					String dimName = dimensionStr.substring(dimensionStr.indexOf(":") + 1);
					dimName = dimName.substring(0, 1).toUpperCase() + dimName.substring(1);

					tooltip.add(Component.literal("§7Destino: §a" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ", " + String.format("%.1f", z)));
					tooltip.add(Component.literal("§7Dimensión: §b" + dimName));
					return;
				}
			}
		}
		tooltip.add(Component.literal("§8Sin vincular. Haz clic derecho para guardar tu ubicación."));
	}
}
