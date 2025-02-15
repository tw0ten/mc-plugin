package plugin;

import java.util.List;
import java.util.Random;

import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.util.TriState;

public class World {
	public static void load() {
		Command.add(new Command("world") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				voidWorld(new NamespacedKey(Plugin.i(), args[0]));
			}
		});
	}

	public static org.bukkit.World voidWorld(final NamespacedKey key) {
		final var wc = new WorldCreator(key);
		wc.keepSpawnLoaded(TriState.FALSE);
		wc.environment(Environment.NORMAL);
		wc.seed(0);
		wc.generator(new ChunkGenerator() {
			@Override
			public void generateSurface(final WorldInfo worldInfo, final Random random, final int chunkX,
					final int chunkZ,
					final ChunkData chunkData) {
			}

			@Override
			public void generateCaves(final WorldInfo worldInfo, final Random random, final int chunkX,
					final int chunkZ,
					final ChunkData chunkData) {
			}

			@Override
			public void generateBedrock(final WorldInfo worldInfo, final Random random, final int chunkX,
					final int chunkZ, final ChunkData chunkData) {
				if (chunkX == 0 && chunkZ == 0)
					chunkData.setBlock(0, 0, 0, Material.BEDROCK);
			}

			@Override
			public boolean shouldGenerateStructures() {
				return false;
			};

			@Override
			public boolean shouldGenerateMobs() {
				return false;
			};
		});
		wc.biomeProvider(new BiomeProvider() {
			@Override
			public @NotNull Biome getBiome(@NotNull final WorldInfo arg0, final int arg1, final int arg2, final int arg3) {
				return Biome.PLAINS;
			}

			@Override
			public @NotNull List<Biome> getBiomes(@NotNull final WorldInfo arg0) {
				return List.of();
			}
		});

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
}
