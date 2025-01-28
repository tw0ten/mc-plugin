import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import net.kyori.adventure.text.Component;

public class Event implements Listener {
	private static final Component motd = Text.plain("the leather club");

	int tick = 0;

	public Event() {
		Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.instance, () -> {
			this.tick(tick++);
		}, 0, 1);
	}

	private void tick(final int tick) {
		if (tick % Random.itemInterval == 0)
			for (final var p : Player.s())
				if (!p.isDead())
					Item.n(p, Random.item());
	}

	@EventHandler
	private void serverPing(final ServerListPingEvent e) {
		try {
			final var i = Plugin.instance.getServer().loadServerIcon(Random.image(64, 64));
			e.setServerIcon(i);
		} catch (final Exception i) {
			i.printStackTrace();
		}
		e.motd(Event.motd);
	}

	@EventHandler
	private void join(final PlayerJoinEvent e) {
		final var i = e.getPlayer();
		Player.join(i);
	}

	@EventHandler
	private void quit(final PlayerQuitEvent e) {
		final var i = e.getPlayer();
		Player.quit(i);
	}
}
