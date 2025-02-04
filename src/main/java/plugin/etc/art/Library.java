package plugin.etc.art;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import plugin.Plugin;

public class Library {
	private final World w;

	public Library() {
		final var key = new NamespacedKey(Plugin.instance, "library");
		var w = Plugin.instance.getServer().getWorld(key);
		if (w == null) {
			w = plugin.World.voidWorld(key);
			w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		}
		this.w = w;
	}
}
