package net.mcreator.quequeworld.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.mcreator.quequeworld.vote.VoteManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.*;

@EventBusSubscriber
public class VoteCommand {

    private static boolean isDios(CommandSourceStack source) {
        if (source.hasPermission(2)) return true;
        if (source.isPlayer()) {
            return source.getPlayer().getTags().contains("dios");
        }
        return false;
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        var votacionNode = Commands.literal("votacion")
                // /qqw votacion votar <opcion> (Cualquier jugador)
                .then(Commands.literal("votar")
                        .then(Commands.argument("opcion", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    if (!ctx.getSource().isPlayer()) {
                                        ctx.getSource().sendFailure(Component.literal("Solo jugadores pueden votar."));
                                        return 0;
                                    }
                                    String option = StringArgumentType.getString(ctx, "opcion");
                                    return VoteManager.castVote(ctx.getSource().getPlayer(), option) ? 1 : 0;
                                })))
                // /qqw votacion crear <votantes> <opciones> [titulo] (Solo Dioses)
                .then(Commands.literal("crear")
                        .requires(VoteCommand::isDios)
                        .then(Commands.argument("votantes", StringArgumentType.string())
                                .then(Commands.argument("opciones", StringArgumentType.string())
                                        .executes(ctx -> executeCrear(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "votantes"),
                                                StringArgumentType.getString(ctx, "opciones"),
                                                "Votación Oficial"))
                                        .then(Commands.argument("titulo", StringArgumentType.greedyString())
                                                .executes(ctx -> executeCrear(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "votantes"),
                                                        StringArgumentType.getString(ctx, "opciones"),
                                                        StringArgumentType.getString(ctx, "titulo")))))))
                // /qqw votacion resultados [segundos] (Solo Dioses)
                .then(Commands.literal("resultados")
                        .requires(VoteCommand::isDios)
                        .executes(ctx -> {
                            VoteManager.revealResults(ctx.getSource(), 5);
                            return 1;
                        })
                        .then(Commands.argument("segundos", IntegerArgumentType.integer(1, 30))
                                .executes(ctx -> {
                                    int seg = IntegerArgumentType.getInteger(ctx, "segundos");
                                    VoteManager.revealResults(ctx.getSource(), seg);
                                    return 1;
                                })))
                // /qqw votacion cancelar (Solo Dioses)
                .then(Commands.literal("cancelar")
                        .requires(VoteCommand::isDios)
                        .executes(ctx -> {
                            VoteManager.cancelSession();
                            ctx.getSource().sendSuccess(() -> Component.literal("§a✔ Votación cancelada correctamente."), true);
                            return 1;
                        }));

        // Registrar bajo /qqw votacion
        var qqwNode = Commands.literal("qqw").then(votacionNode);
        event.getDispatcher().register(qqwNode);

        // Registrar alias directo /vote
        var voteNode = Commands.literal("vote")
                .then(Commands.literal("votar")
                        .then(Commands.argument("opcion", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    if (!ctx.getSource().isPlayer()) return 0;
                                    return VoteManager.castVote(ctx.getSource().getPlayer(), StringArgumentType.getString(ctx, "opcion")) ? 1 : 0;
                                })))
                .then(Commands.literal("crear")
                        .requires(VoteCommand::isDios)
                        .then(Commands.argument("votantes", StringArgumentType.string())
                                .then(Commands.argument("opciones", StringArgumentType.string())
                                        .executes(ctx -> executeCrear(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "votantes"),
                                                StringArgumentType.getString(ctx, "opciones"),
                                                "Votación Oficial"))
                                        .then(Commands.argument("titulo", StringArgumentType.greedyString())
                                                .executes(ctx -> executeCrear(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "votantes"),
                                                        StringArgumentType.getString(ctx, "opciones"),
                                                        StringArgumentType.getString(ctx, "titulo")))))))
                .then(Commands.literal("resultados")
                        .requires(VoteCommand::isDios)
                        .executes(ctx -> {
                            VoteManager.revealResults(ctx.getSource(), 5);
                            return 1;
                        }))
                .then(Commands.literal("cancelar")
                        .requires(VoteCommand::isDios)
                        .executes(ctx -> {
                            VoteManager.cancelSession();
                            ctx.getSource().sendSuccess(() -> Component.literal("§a✔ Votación cancelada correctamente."), true);
                            return 1;
                        }));
        event.getDispatcher().register(voteNode);
    }

    private static int executeCrear(CommandSourceStack source, String votantesArg, String opcionesArg, String titulo) {
        // 1. Resolver Opciones
        List<String> options = new ArrayList<>();
        if (opcionesArg.equalsIgnoreCase("jugadores") || opcionesArg.equalsIgnoreCase("todos")) {
            for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) {
                if (!p.getTags().contains("dios")) {
                    options.add(p.getScoreboardName());
                }
            }
        } else {
            String[] split = opcionesArg.split(",");
            for (String opt : split) {
                String trimmed = opt.trim();
                if (!trimmed.isEmpty()) {
                    options.add(trimmed);
                }
            }
        }

        if (options.isEmpty()) {
            source.sendFailure(Component.literal("❌ No se generaron opciones válidas para la votación."));
            return 0;
        }

        // 2. Resolver Votantes
        Set<UUID> eligibleVoters = null;
        if (!votantesArg.equalsIgnoreCase("todos") && !votantesArg.equalsIgnoreCase("jugadores")) {
            // Check if it matches an FTB team name
            try {
                if (FTBTeamsAPI.api().isManagerLoaded()) {
                    var teamManager = FTBTeamsAPI.api().getManager();
                    Optional<Team> optTeam = teamManager.getTeamByName(votantesArg);
                    if (optTeam.isEmpty()) {
                        // try short name or ID
                        for (Team t : teamManager.getTeams()) {
                            if (t.getShortName().equalsIgnoreCase(votantesArg)) {
                                optTeam = Optional.of(t);
                                break;
                            }
                        }
                    }
                    if (optTeam.isPresent()) {
                        eligibleVoters = new HashSet<>(optTeam.get().getMembers());
                    } else {
                        source.sendFailure(Component.literal("⚠️ No se encontró el equipo FTB '" + votantesArg + "'. Votación abierta a todos."));
                    }
                }
            } catch (Throwable t) {
                source.sendFailure(Component.literal("⚠️ Error al consultar FTB Teams: " + t.getMessage()));
            }
        }

        // 3. Crear Sesión y Enviar Broadcast
        VoteManager.VoteSession session = VoteManager.createSession(titulo, options, eligibleVoters);
        VoteManager.sendVoteBroadcast(source.getServer(), session);

        source.sendSuccess(() -> Component.literal("§a✔ Votación '" + titulo + "' creada con éxito con " + options.size() + " opciones."), true);
        return 1;
    }
}
