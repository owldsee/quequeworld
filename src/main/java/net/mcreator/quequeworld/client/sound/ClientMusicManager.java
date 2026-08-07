package net.mcreator.quequeworld.client.sound;

import net.mcreator.quequeworld.QuequeworldMod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientMusicManager {
	private static FadeMusicSoundInstance currentMusic;

	public static void playMusic(String songName) {
		if (currentMusic != null && !currentMusic.isStopped()) {
			currentMusic.fadeOutAndStop();
		}

		ResourceLocation resLoc = songName.contains(":") ? ResourceLocation.parse(songName) :
			ResourceLocation.fromNamespaceAndPath(QuequeworldMod.MODID, songName.startsWith("music.") ? songName : "music." + songName);

		SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.containsKey(resLoc) ? 
			BuiltInRegistries.SOUND_EVENT.get(resLoc) : 
			SoundEvent.createVariableRangeEvent(resLoc);

		currentMusic = new FadeMusicSoundInstance(soundEvent, 1.0F, 1.0F);
		Minecraft.getInstance().getSoundManager().play(currentMusic);
	}

	public static void stopMusic() {
		if (currentMusic != null && !currentMusic.isStopped()) {
			currentMusic.fadeOutAndStop();
		}
	}
}
