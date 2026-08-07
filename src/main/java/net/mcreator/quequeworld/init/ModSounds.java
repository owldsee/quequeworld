package net.mcreator.quequeworld.init;

import net.mcreator.quequeworld.QuequeworldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, QuequeworldMod.MODID);

	public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_SUSPENSO_1 = SOUND_EVENTS.register("music.suspenso_1",
		() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "music.suspenso_1")));

	public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BATALLA_1 = SOUND_EVENTS.register("music.batalla_1",
		() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, "music.batalla_1")));

	public static void register(IEventBus eventBus) {
		SOUND_EVENTS.register(eventBus);
	}
}
