package net.mcreator.quequeworld.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class QueQueDifficultyConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "quequeworld_difficulty.json");
	
	public static QueQueDifficultyConfig instance = new QueQueDifficultyConfig();

	// Config structure
	public SafeZone safe_zone = new SafeZone();
	public Multipliers multipliers = new Multipliers();
	public Mechanics mechanics = new Mechanics();
	public DangerSettings danger = new DangerSettings();

	public static class DangerSettings {
		public double base_danger = 0.50;
		public double danger_per_banderin = 0.02;
		public double danger_penalty_per_deuda = 0.05;

		public int banderin_loss_ratio = 5;
		public int banderin_loss_base = 0;

		public int deuda_gain_ratio = 10;
		public int deuda_gain_per_ratio = 2;
		public int deuda_gain_base = 1;
	}

	public static class SafeZone {
		public boolean enabled = true;
		public double x = 385.35;
		public double y = 106.0;
		public double z = -486.54;
		public double radius_blocks = 64.0;
	}

	public static class Multipliers {
		public double monster_health = 1.0;
		public double monster_damage = 1.0;
		public double monster_speed = 1.0;
	}

	public static class Mechanics {
		public boolean creepers_instant_explode = false;
		public boolean mobs_immune_to_fire = false;
		public boolean zombie_death_spawns_skeleton = true;
		public double zombie_death_skeleton_chance = 0.30;
		public double skeleton_speed_bonus = 0.10;
		public boolean player_death_spawns_zombie = true;
		public boolean player_death_zombie_on_all_deaths = false;
	}

	public static void load() {
		if (!CONFIG_FILE.exists()) {
			saveDefault();
			return;
		}

		try (FileReader reader = new FileReader(CONFIG_FILE)) {
			instance = GSON.fromJson(reader, QueQueDifficultyConfig.class);
			if (instance == null) {
				instance = new QueQueDifficultyConfig();
				saveDefault();
			}
		} catch (Exception e) {
			System.err.println("[QueQueWorld] Error al cargar la configuración de dificultad: " + e.getMessage());
			instance = new QueQueDifficultyConfig();
		}
	}

	public static void saveDefault() {
		try {
			if (!CONFIG_FILE.getParentFile().exists()) {
				CONFIG_FILE.getParentFile().mkdirs();
			}
			try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			System.err.println("[QueQueWorld] Error al guardar la configuración de dificultad: " + e.getMessage());
		}
	}
}
