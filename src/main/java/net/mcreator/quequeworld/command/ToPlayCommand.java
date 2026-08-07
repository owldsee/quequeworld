package net.mcreator.quequeworld.command;

import org.checkerframework.checker.units.qual.s;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import org.openjdk.nashorn.internal.runtime.Context;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

// @EventBusSubscriber
public class ToPlayCommand {
	// @SubscribeEvent
	// public static void registerCommand(RegisterCommandsEvent event) {
	// 	event.getDispatcher().register(Commands.literal("toplay")
	// 		.requires(s -> s.hasPermission(4))
	// 		.then(Commands.argument("target", EntityArgument.entities())
	// 		.then(Commands.argument("pos", BlockPosArgument.blockPos())
	// 			.executes(context -> execute(
	//                     context.getSource(), 
	//                     EntityArgument.getPlayers(context, "target"), 
	//                     BlockPosArgument.getLoadedBlockPos(context, "pos")
	//                 ))
	// 		)));
	// }
	private static int execute(CommandSourceStack source, Collection<ServerPlayer> players, BlockPos pos) {
        // 1. Corregimos el genérico usando 'Level' en lugar de 'ServerLevel'
        ResourceKey<Level> destinationKey = ResourceKey.create(
            Registries.DIMENSION, 
            ResourceLocation.fromNamespaceAndPath("queque", "lab")
        );

        // 2. Ahora el servidor leerá la llave perfectamente y nos devolverá el ServerLevel de la dimensión
        ServerLevel targetWorld = source.getServer().getLevel(destinationKey);

        if (targetWorld == null) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("❌ Error: La dimensión destino no existe o no está cargada."));
            return 0;
        }

        // Bucle para mover a todos los jugadores seleccionados de forma masiva
        for (ServerPlayer player : players) {
            player.teleportTo(
                targetWorld, 
                pos.getX() + 0.5, // Centramos al jugador en el bloque
                pos.getY(), 
                pos.getZ() + 0.5, 
                player.getYRot(), 
                player.getXRot()
            );
        }

        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("⚡ Jugadores teletransportados con éxito a la zona del evento."), true);
        return 1;
    }
}