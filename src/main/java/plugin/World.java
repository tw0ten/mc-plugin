package plugin;

import java.util.List;

import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.util.TriState;
import plugin.etc.Random;

public class World {
	private static final List<Biome> biomes = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).stream()
			.toList();

	private static final Environment[] environments = {
			Environment.NORMAL,
			Environment.NETHER,
			Environment.THE_END
	};

	public static void load() {
		Command.add(new Command.Admin("world") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (sender instanceof final Player p)
					p.teleport(Plugin.s().getWorld(args[0]).getSpawnLocation());
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
			public void generateSurface(final WorldInfo worldInfo, final java.util.Random random, final int chunkX,
					final int chunkZ,
					final ChunkData chunkData) {
			}

			@Override
			public void generateCaves(final WorldInfo worldInfo, final java.util.Random random, final int chunkX,
					final int chunkZ,
					final ChunkData chunkData) {
			}

			@Override
			public void generateBedrock(final WorldInfo worldInfo, final java.util.Random random, final int chunkX,
					final int chunkZ, final ChunkData chunkData) {
				if (chunkX == 0 && chunkZ == 0)
					chunkData.setBlock(0, 0, 0, Material.BEDROCK);
			}

			@Override
			public boolean shouldGenerateStructures() {
				return false;
			}

			@Override
			public boolean shouldGenerateMobs() {
				return false;
			}
		});
		wc.biomeProvider(new BiomeProvider() {
			@Override
			public Biome getBiome(final WorldInfo worldInfo, final int x, final int y, final int z) {
				return Biome.PLAINS;
			}

			@Override
			public List<Biome> getBiomes(final WorldInfo worldInfo) {
				return List.of();
			}
		});

		final var w = wc.createWorld();

		return w;
	}

	public static org.bukkit.World randomWorld(final NamespacedKey key) {
		final var wc = new WorldCreator(key);
		wc.keepSpawnLoaded(TriState.FALSE);
		wc.environment(Environment.NETHER);

		final var r = new Random(new java.util.Random(wc.seed()));

		wc.generator(new ChunkGenerator() {
			private static final int density = 100;

			@Override
			public void generateSurface(final WorldInfo worldInfo, final java.util.Random random, final int chunkX,
					final int chunkZ,
					final ChunkData chunkData) {
				for (var x = 0; x < 16; x++)
					for (var z = 0; z < 16; z++)
						for (var y = worldInfo.getMinHeight(); y < worldInfo.getMaxHeight(); y++)
							chunkData.setBlock(x, y, z, r.block());
			}

			@Override
			public void generateCaves(final WorldInfo worldInfo, final java.util.Random random, final int chunkX,
					final int chunkZ, final ChunkData chunkData) {
				for (var x = 0; x < 16; x++)
					for (var z = 0; z < 16; z++)
						for (var y = worldInfo.getMinHeight(); y < worldInfo.getMaxHeight(); y++) {
							final var m = chunkData.getType(x, y, z);
							if (m == Material.NETHER_PORTAL || m == Material.END_PORTAL || m == Material.END_GATEWAY
									|| !r.oneIn(density)
									|| r.chance((y - worldInfo.getMinHeight())
											/ (float) (worldInfo.getMaxHeight() - worldInfo.getMinHeight())))
								chunkData.setBlock(x, y, z, Material.AIR);
						}
			}

			@Override
			public void generateBedrock(final WorldInfo worldInfo, final java.util.Random random, final int chunkX,
					final int chunkZ, final ChunkData chunkData) {
				chunkData.setBlock(0, 0, 0, Material.BEDROCK);
			}
		});
		wc.biomeProvider(new BiomeProvider() {
			@Override
			public Biome getBiome(final WorldInfo worldInfo, final int x, final int y, final int z) {
				return r.pick(getBiomes(worldInfo));
			}

			@Override
			public List<Biome> getBiomes(final WorldInfo worldInfo) {
				return biomes;
			}
		});

		final var w = wc.createWorld();

		return w;
	}

	public static void idle(final org.bukkit.World w) {
		w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		w.setPVP(false);
		w.setAutoSave(false);
	}
}
