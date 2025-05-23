package plugin;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import plugin.etc.Image;
import plugin.etc.Random;
import plugin.etc.art.Cinema;
import plugin.etc.art.Library;
import plugin.etc.art.Orchestra;

public class Plugin extends JavaPlugin {
	private static Plugin instance;

	public static float tps() {
		return s().getServerTickManager().getTickRate();
	}

	public static Plugin i() {
		return instance;
	}

	public static Server s() {
		return instance.getServer();
	}

	public static void l(String... s) {
		i().getLogger().info(String.join(" ", s));
	}

	public final FileConfiguration config = getConfig();

	public final long uptime;

	public org.bukkit.World world, nether, end;

	public Plugin() {
		super();
		instance = this;
		this.uptime = System.currentTimeMillis();

		getLogger().info("created");
	}

	@Override
	public void onLoad() {
		super.onLoad();

		getLogger().info("loaded");
	}

	@Override
	public void onEnable() {
		super.onEnable();

		Image.load();

		this.world = s().getWorlds().get(0);
		this.nether = s().getWorlds().get(1);
		this.end = s().getWorlds().get(2);

		World.load();
		Random.load();

		for (final var p : s().getOnlinePlayers())
			Player.join(p);

		new Scoreboard();
		new Event();

		new Library();
		new Cinema();
		new Orchestra();

		Command.load();

		getLogger().info("enabled");
	}

	@Override
	public void onDisable() {
		super.onDisable();

		for (final var p : Player.s())
			Player.quit(p);

		saveConfig();

		getLogger().info("disabled");
	}
}
