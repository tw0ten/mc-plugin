package plugin;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import plugin.etc.Audio;
import plugin.etc.Image;
import plugin.etc.Random;

public class Event implements Listener {
	private static final Component motd = Text.plain("the leather club");
	private static BufferedImage icon;

	private int tick = 0;
	Audio audio;

	public Event() {
		Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.instance, () -> {
			this.tick(tick++);
		}, 0, 1);
	}

	private void tick(final int tick) {
		if ((tick + 1) % Random.itemInterval == 0)
			for (final var p : Player.s())
				if (!p.isDead() && p.getGameMode() == GameMode.SURVIVAL)
					Item.n(p, Random.item());

		// DEBUG
		if (tick % 5 == 0) {
			final var p = Image.Screen.p;
			if (p != null)
				Image.Screen.l = p.getLocation().add(0, p.getEyeHeight(), 0)
						.add(p.getLocation().getDirection().multiply(8));

			if (Image.Screen.l == null)
				return;
			try {
				icon = ImageIO.read(Plugin.instance.getDataPath().resolve("screen.png").toFile());
			} catch (final Exception e) {
			}
			Image.particles(icon);
		}
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
		e.joinMessage(Text.plain("+").appendSpace().append(Text.player(i)));
	}

	@EventHandler
	private void quit(final PlayerQuitEvent e) {
		final var i = e.getPlayer();
		Player.quit(i);
		e.quitMessage(Text.plain("-").appendSpace().append(Text.player(i)));
	}

	@EventHandler
	private void chat(final AsyncChatEvent e) {
		final var i = e.signedMessage();
		e.setCancelled(true);
		Bukkit.broadcast(Text.player(e.getPlayer())
				.append(Text.plain(": "))
				.append(Text.plain(i.message())));
	}

	@EventHandler
	private void despawn(final ItemDespawnEvent e) {
		e.getEntity().setGlowing(true);
	}
}
