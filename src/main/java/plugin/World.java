package plugin;

import java.util.Random;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import net.kyori.adventure.util.TriState;

public class World {

	public static org.bukkit.World voidWorld(final NamespacedKey key) {
		final var wc = new WorldCreator(key);
		wc.keepSpawnLoaded(TriState.FALSE);
		wc.environment(org.bukkit.World.Environment.NORMAL);
		wc.seed(0);
		wc.generateStructures(false);
		wc.generator(voidGenerator);

		final var w = wc.createWorld();
		w.setAutoSave(false);
		return w;
	}

	public static void idle(final org.bukkit.World world) {
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		world.setPVP(false);
	}

	private static final ChunkGenerator voidGenerator = new ChunkGenerator() {
		@Override
		public void generateSurface(final WorldInfo worldInfo, final Random random, final int chunkX, final int chunkZ,
				final ChunkData chunkData) {
		}

		@Override
		public void generateCaves(final WorldInfo worldInfo, final Random random, final int chunkX, final int chunkZ,
				final ChunkData chunkData) {
		}

		@Override
		public void generateBedrock(final WorldInfo worldInfo, final Random random, final int chunkX, final int chunkZ,
				final ChunkData chunkData) {
		}
	};
}
