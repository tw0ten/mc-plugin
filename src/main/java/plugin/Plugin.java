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
	public final FileConfiguration config = getConfig();
	private static Plugin instance;

	public final long uptime;

	public static float tps() {
		return s().getServerTickManager().getTickRate();
	}

	public static Plugin i() {
		return instance;
	}

	public static Server s() {
		return instance.getServer();
	}

	public Plugin() {
		super();
		getLogger().info("created");
		this.uptime = System.currentTimeMillis();
		instance = this;
	}

	@Override
	public void onLoad() {
		super.onLoad();

		World.load();
		Image.load();
		Random.load();

		saveConfig();

		getLogger().info("loaded");
	}

	@Override
	public void onEnable() {
		super.onEnable();

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
