package plugin;

import org.bukkit.plugin.java.JavaPlugin;

import plugin.etc.*;
import plugin.etc.art.*;

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

		Book.load();
		Image.load();
		Random.load();

		getLogger().info("loaded");
	}

	@Override
	public void onEnable() {
		super.onEnable();

		for (final var p : getServer().getOnlinePlayers())
			Player.join(p);

		Command.load();
		new Event();

		new Library();
		new Cinema();
		new Orchestra();

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
