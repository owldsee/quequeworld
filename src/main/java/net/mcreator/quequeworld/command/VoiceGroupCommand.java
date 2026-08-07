package net.mcreator.quequeworld.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import de.maxhenkel.voicechat.api.Group;
import net.mcreator.quequeworld.voice.QueQueVoicechatPlugin;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class VoiceGroupCommand {

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        var builder = Commands.literal("voicegroup")
                .requires(s -> s.hasPermission(2))
                
                // /voicegroup explain
                .then(Commands.literal("explain")
                        .executes(ctx -> executeExplainMode(ctx.getSource())))
                        
                // /voicegroup mode explain
                .then(Commands.literal("mode")
                        .then(Commands.literal("explain")
                                .executes(ctx -> executeExplainMode(ctx.getSource())))
                        .then(Commands.literal("teams")
                                .executes(ctx -> executeTeamsMode(ctx.getSource()))))
                                
                // /voicegroup teams
                .then(Commands.literal("teams")
                        .executes(ctx -> executeTeamsMode(ctx.getSource())))
                        
                // /voicegroup reset
                .then(Commands.literal("reset")
                        .executes(ctx -> executeResetMode(ctx.getSource())))
                        
                // /voicegroup clear
                .then(Commands.literal("clear")
                        .executes(ctx -> executeResetMode(ctx.getSource())))
                        
                // /voicegroup moveall <nombre_grupo>
                .then(Commands.literal("moveall")
                        .then(Commands.argument("group", StringArgumentType.greedyString())
                                .executes(ctx -> executeMoveAll(ctx.getSource(), StringArgumentType.getString(ctx, "group")))));

        event.getDispatcher().register(builder);
        
        // Alias /vg
        var aliasBuilder = Commands.literal("vg")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("explain").executes(ctx -> executeExplainMode(ctx.getSource())))
                .then(Commands.literal("teams").executes(ctx -> executeTeamsMode(ctx.getSource())))
                .then(Commands.literal("reset").executes(ctx -> executeResetMode(ctx.getSource())))
                .then(Commands.literal("moveall")
                        .then(Commands.argument("group", StringArgumentType.greedyString())
                                .executes(ctx -> executeMoveAll(ctx.getSource(), StringArgumentType.getString(ctx, "group")))));
                                
        event.getDispatcher().register(aliasBuilder);
    }

    private static int executeExplainMode(CommandSourceStack source) {
        QueQueVoicechatPlugin.clearAllGroups(source.getServer());
        Group group = QueQueVoicechatPlugin.createOrGetGroup("Explicación", Group.Type.OPEN);
        if (group == null) {
            source.sendFailure(Component.literal("❌ No se pudo crear o acceder al grupo de voz de Explicación. Verifica que Simple Voice Chat esté activo."));
            return 0;
        }

        int moved = QueQueVoicechatPlugin.moveAllToGroup(source.getServer(), group);
        source.sendSuccess(() -> Component.literal("📢 Modo Explicación Activo: " + moved + " jugador(es) han sido movidos al grupo de voz 'Explicación'."), true);
        return moved;
    }

    private static int executeTeamsMode(CommandSourceStack source) {
        QueQueVoicechatPlugin.clearAllGroups(source.getServer());
        int moved = QueQueVoicechatPlugin.setupFTBTeamGroups(source.getServer());
        if (moved > 0) {
            source.sendSuccess(() -> Component.literal("👥 Modo Equipos Activo: " + moved + " jugador(es) han sido asignados a los canales de voz de sus equipos de FTB Teams."), true);
        } else {
            source.sendFailure(Component.literal("⚠️ No se asignaron jugadores. Asegúrate de que FTB Teams esté cargado y los jugadores tengan equipo."));
        }
        return moved;
    }

    private static int executeResetMode(CommandSourceStack source) {
        int cleared = QueQueVoicechatPlugin.clearAllGroups(source.getServer());
        source.sendSuccess(() -> Component.literal("🔊 Se han cerrado los canales de voz. " + cleared + " jugador(es) han regresado al chat de proximidad estándar."), true);
        return cleared;
    }

    private static int executeMoveAll(CommandSourceStack source, String groupName) {
        Group group = QueQueVoicechatPlugin.createOrGetGroup(groupName, Group.Type.OPEN);
        if (group == null) {
            source.sendFailure(Component.literal("❌ No se pudo crear o acceder al grupo de voz: " + groupName));
            return 0;
        }

        int moved = QueQueVoicechatPlugin.moveAllToGroup(source.getServer(), group);
        source.sendSuccess(() -> Component.literal("📢 " + moved + " jugador(es) han sido movidos al grupo de voz '" + groupName + "'."), true);
        return moved;
    }
}
