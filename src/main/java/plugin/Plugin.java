package plugin;

import org.bukkit.plugin.java.JavaPlugin;

import plugin.etc.Image;
import plugin.etc.Random;
import plugin.etc.art.Cinema;
import plugin.etc.art.Library;
import plugin.etc.art.Orchestra;

public class Plugin extends JavaPlugin {
	public static JavaPlugin instance;

	public Plugin() {
		super();
		getLogger().info("created");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		Plugin.instance = this;

		Image.load();
		Random.load();

		getLogger().info("loaded");
	}

	@Override
	public void onEnable() {
		super.onEnable();

		for (final var p : getServer().getOnlinePlayers())
			Player.join(p);

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

		getLogger().info("disabled");
	}
}
