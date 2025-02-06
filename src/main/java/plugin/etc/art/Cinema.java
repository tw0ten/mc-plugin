package plugin.etc.art;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import plugin.Plugin;

public class Cinema {
	private final World w;

	public Cinema() {
		final var key = new NamespacedKey(Plugin.instance, "cinema");
		var w = Plugin.instance.getServer().getWorld(key);
		if (w == null) {
			w = plugin.World.voidWorld(key);
			w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		}
		this.w = w;
	}

	class Subtitles {
	}
}
