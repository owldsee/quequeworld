package net.mcreator.quequeworld.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FadeMusicSoundInstance extends AbstractTickableSoundInstance {
	private int fadeTicks = 0;
	private static final int MAX_FADE_TICKS = 10; // 0.5s en 20 ticks/seg
	private boolean fadingOut = false;
	private final float targetVolume;

	public FadeMusicSoundInstance(SoundEvent soundEvent, float volume, float pitch) {
		super(soundEvent, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
		this.targetVolume = volume;
		this.volume = 0.001F; // Empieza prácticamente en 0 para Fade In
		this.pitch = pitch;
		this.looping = true; // Reproducción continua en bucle hasta que se detenga por comando
		this.delay = 0;
		this.relative = true; // Sonido de fondo no posicional
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public void fadeOutAndStop() {
		if (!this.fadingOut) {
			this.fadingOut = true;
			this.fadeTicks = 0;
		}
	}

	public boolean isFadingOut() {
		return this.fadingOut;
	}

	@Override
	public void tick() {
		if (this.isStopped()) {
			return;
		}

		this.fadeTicks++;

		if (this.fadingOut) {
			// Fade out
			this.volume = Mth.lerp((float) this.fadeTicks / MAX_FADE_TICKS, this.targetVolume, 0.0F);
			if (this.fadeTicks >= MAX_FADE_TICKS || this.volume <= 0.005F) {
				this.stop();
			}
		} else {
			// Fade in
			if (this.fadeTicks <= MAX_FADE_TICKS) {
				this.volume = Mth.lerp((float) this.fadeTicks / MAX_FADE_TICKS, 0.0F, this.targetVolume);
			} else {
				this.volume = this.targetVolume;
			}
		}
	}
}
