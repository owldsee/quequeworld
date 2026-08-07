package net.mcreator.quequeworld;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.server.TickTask;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;

import net.mcreator.quequeworld.item.ModItems;
import net.mcreator.quequeworld.init.QuequeworldModTabs;
import net.mcreator.quequeworld.init.QuequeworldModItems;
import net.mcreator.quequeworld.init.ModBlocks;
import net.mcreator.quequeworld.init.ModBlockEntities;
import net.mcreator.quequeworld.init.ModMenus;
import net.mcreator.quequeworld.init.ModEffects;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;

@Mod("quequeworld")
public class QuequeworldMod {
	public static final Logger LOGGER = LogManager.getLogger(QuequeworldMod.class);
	public static final String MODID = "quequeworld";

	public QuequeworldMod(IEventBus modEventBus) {
		// Start of user code block mod constructor
		ModItems.ITEMS.register(modEventBus);
		addNetworkMessage(net.mcreator.quequeworld.network.SoulShieldSyncPacket.TYPE, net.mcreator.quequeworld.network.SoulShieldSyncPacket.STREAM_CODEC, net.mcreator.quequeworld.network.SoulShieldSyncPacket::handle);
		addNetworkMessage(net.mcreator.quequeworld.network.TimerSyncPacket.TYPE, net.mcreator.quequeworld.network.TimerSyncPacket.STREAM_CODEC, net.mcreator.quequeworld.network.TimerSyncPacket::handle);
		addNetworkMessage(net.mcreator.quequeworld.network.ThreatAnimationPacket.TYPE, net.mcreator.quequeworld.network.ThreatAnimationPacket.STREAM_CODEC, net.mcreator.quequeworld.network.ThreatAnimationPacket::handle);
		addNetworkMessage(net.mcreator.quequeworld.network.MusicCommandPacket.TYPE, net.mcreator.quequeworld.network.MusicCommandPacket.STREAM_CODEC, net.mcreator.quequeworld.network.MusicCommandPacket::handle);
		addNetworkMessage(net.mcreator.quequeworld.network.WelcomePacket.TYPE, net.mcreator.quequeworld.network.WelcomePacket.STREAM_CODEC, net.mcreator.quequeworld.network.WelcomePacket::handle);
		addNetworkMessage(net.mcreator.quequeworld.network.UpdateSignalReceiverPacket.TYPE, net.mcreator.quequeworld.network.UpdateSignalReceiverPacket.STREAM_CODEC, net.mcreator.quequeworld.network.UpdateSignalReceiverPacket::handle);
		addNetworkMessage(net.mcreator.quequeworld.network.UpdateTerminalConfigPacket.TYPE, net.mcreator.quequeworld.network.UpdateTerminalConfigPacket.STREAM_CODEC, net.mcreator.quequeworld.network.UpdateTerminalConfigPacket::handle);
		addNetworkMessage(net.mcreator.quequeworld.network.SubmitTerminalInputPacket.TYPE, net.mcreator.quequeworld.network.SubmitTerminalInputPacket.STREAM_CODEC, net.mcreator.quequeworld.network.SubmitTerminalInputPacket::handle);
		// End of user code block mod constructor

		NeoForge.EVENT_BUS.register(this);
		modEventBus.addListener(this::registerNetworking);
		QuequeworldModItems.REGISTRY.register(modEventBus);
		QuequeworldModTabs.REGISTRY.register(modEventBus);
		net.mcreator.quequeworld.init.GodBlocks.register(modEventBus);
		ModBlocks.register(modEventBus);
		ModBlockEntities.register(modEventBus);
		ModMenus.register(modEventBus);
		ModEffects.register(modEventBus);
		net.mcreator.quequeworld.init.ModSounds.register(modEventBus);
		// Start of user code block mod init
		//modEventBus.addListener(this::addCreative);
		// End of user code block mod init
	}

	// Start of user code block mod methods
	//public void addCreative(BuildCreativeModeTabContentsEvent event) {
	//	if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
	//		event.accept(ModItems.BISMUTH);
	//	}
	//}
	// End of user code block mod methods
	private static boolean networkingRegistered = false;
	private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

	private record NetworkMessage<T extends CustomPacketPayload>(StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
	}

	public static <T extends CustomPacketPayload> void addNetworkMessage(CustomPacketPayload.Type<T> id, StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
		if (networkingRegistered)
			throw new IllegalStateException("Cannot register new network messages after networking has been registered");
		MESSAGES.put(id, new NetworkMessage<>(reader, handler));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void registerNetworking(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(MODID);
		MESSAGES.forEach((id, networkMessage) -> registrar.playBidirectional(id, ((NetworkMessage) networkMessage).reader(), ((NetworkMessage) networkMessage).handler()));
		networkingRegistered = true;
	}

	private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();
	private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));

	public static void queueServerWork(int delay, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workToBeScheduled.add(new IntObjectImmutablePair<>(delay, action));
	}

	@SubscribeEvent
	public void tick(ServerTickEvent.Post event) {
		int currentTick = event.getServer().getTickCount();
		IntObjectPair<Runnable> work;
		while ((work = workToBeScheduled.poll()) != null) {
			workQueue.add(new TickTask(currentTick + work.leftInt(), work.right()));
		}
		while (!workQueue.isEmpty() && currentTick >= workQueue.peek().getTick()) {
			workQueue.poll().run();
		}
		net.mcreator.quequeworld.timer.TimerManager.tick(event.getServer());
	}
}