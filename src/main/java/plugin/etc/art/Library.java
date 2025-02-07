package plugin.etc.art;

import org.bukkit.NamespacedKey;

import plugin.World;
import plugin.Plugin;

public class Library {
	private final org.bukkit.World w;

	public Library() {
		final var key = new NamespacedKey(Plugin.instance, "library");
		var w = Plugin.instance.getServer().getWorld(key);
		if (w == null) {
			w = World.voidWorld(key);
			World.idle(w);
		}
		this.w = w;
	}
}
