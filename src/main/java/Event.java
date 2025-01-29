import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
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
		if ((tick + 1) % Random.itemInterval == 0)
			for (final var p : Player.s())
				if (!p.isDead())
					Item.n(p, Random.item());

		if (tick % 15 == 0 && false)
			for (final var p : Player.s())
				Image.particles(
						p.getLocation().add(p.getLocation().getDirection().multiply(5)).add(0, p.getEyeHeight(), 0),
						Random.image(32, 32));
	}

	@EventHandler
	private void serverPing(final ServerListPingEvent e) {
		e.motd(Event.motd);
	}

	@EventHandler
	private void login(final PlayerLoginEvent e) {
		final var i = e.getPlayer();
		if (!Player.allowed(i))
			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Text.plain("disallow"));
	}

	@EventHandler
	private void join(final PlayerJoinEvent e) {
		final var i = e.getPlayer();
		Player.join(i);
		e.joinMessage();
	}

	@EventHandler
	private void quit(final PlayerQuitEvent e) {
		final var i = e.getPlayer();
		Player.quit(i);
		e.quitMessage();
	}

	@EventHandler
	private void despawn(final ItemDespawnEvent e) {
		e.setCancelled(true);
	}
}
