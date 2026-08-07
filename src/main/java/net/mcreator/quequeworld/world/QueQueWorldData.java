package net.mcreator.quequeworld.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.HolderLookup;

public class QueQueWorldData extends SavedData {
	private static final String DATA_NAME = "quequeworld_data";

	public double spawnX = 383.0;
	public double spawnY = 104.0;
	public double spawnZ = -480.0;
	public float spawnYaw = 0.0f;
	public float spawnPitch = 0.0f;
	public String spawnDim = "minecraft:overworld";

	public double confesadoX = 0;
	public double confesadoY = 0;
	public double confesadoZ = 0;
	public float confesadoYaw = 0;
	public float confesadoPitch = 0;
	public String confesadoDim = "";
	public boolean confesadoSet = false;

	public double camaraX = 0;
	public double camaraY = 0;
	public double camaraZ = 0;
	public float camaraYaw = 0;
	public float camaraPitch = 0;
	public String camaraDim = "";
	public boolean camaraSet = false;

	public double desafioSpawnX = 0;
	public double desafioSpawnY = 0;
	public double desafioSpawnZ = 0;
	public float desafioSpawnYaw = 0;
	public float desafioSpawnPitch = 0;
	public String desafioSpawnDim = "";
	public boolean desafioSpawnSet = false;


	public int minBanderines = 0;

	public boolean gamePaused = false;
	public boolean dayTimerActive = false;
	public boolean dayTimerPaused = false;
	public int dayTimerTicks = 0;
	public boolean countdownActive = false;
	public int countdownTicks = 0;

	public boolean portalesRestringidos = false;

	public static QueQueWorldData get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(
			new SavedData.Factory<>(
				QueQueWorldData::new,
				(nbt, registries) -> load(nbt, registries),
				null
			),
			DATA_NAME
		);
	}

	public QueQueWorldData() {}

	public static QueQueWorldData load(CompoundTag nbt, HolderLookup.Provider registries) {
		QueQueWorldData data = new QueQueWorldData();
		if (nbt.contains("portalesRestringidos")) {
			data.portalesRestringidos = nbt.getBoolean("portalesRestringidos");
		}
		if (nbt.contains("spawnX")) {
			data.spawnX = nbt.getDouble("spawnX");
			data.spawnY = nbt.getDouble("spawnY");
			data.spawnZ = nbt.getDouble("spawnZ");
			data.spawnYaw = nbt.getFloat("spawnYaw");
			data.spawnPitch = nbt.getFloat("spawnPitch");
			data.spawnDim = nbt.getString("spawnDim");
		}
		if (nbt.contains("confesadoX")) {
			data.confesadoX = nbt.getDouble("confesadoX");
			data.confesadoY = nbt.getDouble("confesadoY");
			data.confesadoZ = nbt.getDouble("confesadoZ");
			data.confesadoYaw = nbt.getFloat("confesadoYaw");
			data.confesadoPitch = nbt.getFloat("confesadoPitch");
			data.confesadoDim = nbt.getString("confesadoDim");
			data.confesadoSet = nbt.getBoolean("confesadoSet");
		}
		if (nbt.contains("camaraX")) {
			data.camaraX = nbt.getDouble("camaraX");
			data.camaraY = nbt.getDouble("camaraY");
			data.camaraZ = nbt.getDouble("camaraZ");
			data.camaraYaw = nbt.getFloat("camaraYaw");
			data.camaraPitch = nbt.getFloat("camaraPitch");
			data.camaraDim = nbt.getString("camaraDim");
			data.camaraSet = nbt.getBoolean("camaraSet");
		}
		if (nbt.contains("desafioSpawnX")) {
			data.desafioSpawnX = nbt.getDouble("desafioSpawnX");
			data.desafioSpawnY = nbt.getDouble("desafioSpawnY");
			data.desafioSpawnZ = nbt.getDouble("desafioSpawnZ");
			data.desafioSpawnYaw = nbt.getFloat("desafioSpawnYaw");
			data.desafioSpawnPitch = nbt.getFloat("desafioSpawnPitch");
			data.desafioSpawnDim = nbt.getString("desafioSpawnDim");
			data.desafioSpawnSet = nbt.getBoolean("desafioSpawnSet");
		}
		if (nbt.contains("minBanderines")) {
			data.minBanderines = nbt.getInt("minBanderines");
		}
		if (nbt.contains("gamePaused")) {
			data.gamePaused = nbt.getBoolean("gamePaused");
			data.dayTimerActive = nbt.getBoolean("dayTimerActive");
			data.dayTimerPaused = nbt.getBoolean("dayTimerPaused");
			data.dayTimerTicks = nbt.getInt("dayTimerTicks");
			data.countdownActive = nbt.getBoolean("countdownActive");
			data.countdownTicks = nbt.getInt("countdownTicks");
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
		nbt.putDouble("spawnX", this.spawnX);
		nbt.putDouble("spawnY", this.spawnY);
		nbt.putDouble("spawnZ", this.spawnZ);
		nbt.putFloat("spawnYaw", this.spawnYaw);
		nbt.putFloat("spawnPitch", this.spawnPitch);
		nbt.putString("spawnDim", this.spawnDim);

		nbt.putDouble("confesadoX", this.confesadoX);
		nbt.putDouble("confesadoY", this.confesadoY);
		nbt.putDouble("confesadoZ", this.confesadoZ);
		nbt.putFloat("confesadoYaw", this.confesadoYaw);
		nbt.putFloat("confesadoPitch", this.confesadoPitch);
		nbt.putString("confesadoDim", this.confesadoDim);
		nbt.putBoolean("confesadoSet", this.confesadoSet);

		nbt.putDouble("camaraX", this.camaraX);
		nbt.putDouble("camaraY", this.camaraY);
		nbt.putDouble("camaraZ", this.camaraZ);
		nbt.putFloat("camaraYaw", this.camaraYaw);
		nbt.putFloat("camaraPitch", this.camaraPitch);
		nbt.putString("camaraDim", this.camaraDim);
		nbt.putBoolean("camaraSet", this.camaraSet);

		nbt.putDouble("desafioSpawnX", this.desafioSpawnX);
		nbt.putDouble("desafioSpawnY", this.desafioSpawnY);
		nbt.putDouble("desafioSpawnZ", this.desafioSpawnZ);
		nbt.putFloat("desafioSpawnYaw", this.desafioSpawnYaw);
		nbt.putFloat("desafioSpawnPitch", this.desafioSpawnPitch);
		nbt.putString("desafioSpawnDim", this.desafioSpawnDim);
		nbt.putBoolean("desafioSpawnSet", this.desafioSpawnSet);
		nbt.putInt("minBanderines", this.minBanderines);

		nbt.putBoolean("gamePaused", this.gamePaused);
		nbt.putBoolean("dayTimerActive", this.dayTimerActive);
		nbt.putBoolean("dayTimerPaused", this.dayTimerPaused);
		nbt.putInt("dayTimerTicks", this.dayTimerTicks);
		nbt.putBoolean("countdownActive", this.countdownActive);
		nbt.putInt("countdownTicks", this.countdownTicks);
		nbt.putBoolean("portalesRestringidos", this.portalesRestringidos);
		return nbt;
	}
}
