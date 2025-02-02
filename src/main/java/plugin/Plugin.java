package plugin;

import org.bukkit.plugin.java.JavaPlugin;

import plugin.book.Library;

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

		getLogger().info("loaded");
	}

	@Override
	public void onEnable() {
		super.onEnable();

		for (final var p : getServer().getOnlinePlayers()) {
			if (!Player.allowed(p)) {
				p.kick();
				continue;
			}
			Player.join(p);
		}

		new Event();
		new Command();

		new Library();

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
