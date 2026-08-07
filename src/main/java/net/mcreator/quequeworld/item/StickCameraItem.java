package net.mcreator.quequeworld.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.nbt.CompoundTag;

public class StickCameraItem extends Item {
	public StickCameraItem() {
		super(new Item.Properties().stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}

		// Seguridad: Solo Dios o creador con permisos
		if (!player.getTags().contains("dios") && !player.hasPermissions(2)) {
			player.displayClientMessage(Component.literal("§cNo tienes permiso para usar esta herramienta divina."), true);
			return InteractionResultHolder.fail(stack);
		}

		if (player instanceof ServerPlayer serverPlayer) {
			ServerLevel serverLevel = serverPlayer.serverLevel();
			CompoundTag diosData = serverPlayer.getPersistentData();
			boolean active = diosData.getBoolean("qqw_camera_cinematic_active");

			if (active) {
				// 1. DESACTIVAR CINEMÁTICA
				diosData.putBoolean("qqw_camera_cinematic_active", false);

				for (ServerPlayer other : serverLevel.getServer().getPlayerList().getPlayers()) {
					if (other.getTags().contains("qqw_spectating_dios")) {
						other.removeTag("qqw_spectating_dios");
						other.connection.send(new ClientboundSetCameraPacket(other));
					}
				}
			} else {
				// 2. ACTIVAR CINEMÁTICA
				diosData.putBoolean("qqw_camera_cinematic_active", true);

				for (ServerPlayer other : serverLevel.getServer().getPlayerList().getPlayers()) {
					if (other != serverPlayer) {
						other.addTag("qqw_spectating_dios");
						other.getPersistentData().remove("cine_x");
						other.connection.send(new ClientboundSetCameraPacket(serverPlayer));
						other.displayClientMessage(Component.literal("§6[Cinemática] Viendo la perspectiva de Dios"), true);
					}
				}
				serverPlayer.displayClientMessage(Component.literal("§eCinemática iniciada. Transmitiendo tu perspectiva..."), true);
			}
		}

		return InteractionResultHolder.success(stack);
	}
}
