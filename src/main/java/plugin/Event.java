package plugin;

import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.format.TextColor;
import plugin.etc.Random;

public class Event implements Listener {
	private int tick = 0;

	public Event() {
		Plugin.s().getPluginManager().registerEvents(this, Plugin.i());
		Plugin.s().getScheduler().scheduleSyncRepeatingTask(Plugin.i(), this::tick, 0, 1);
	}

	private void tick() {
		final var tick = this.tick++;
		for (final var p : Player.s())
			if ((p.getTicksLived() + 1) % (int) (Plugin.tps() * 20) == 0)
				if (!p.isDead() && p.getGameMode() == GameMode.SURVIVAL)
					Item.n(p, Random.i.item());
	}

	@EventHandler
	private void serverPing(final ServerListPingEvent e) {
		final var s = Plugin.s();
		final var player = Player.offline(Player.uuid(
				Plugin.i().config.getString("ipcache." + e.getAddress().getHostAddress())));
		final var tps = (float) s.getTPS()[1] / Plugin.tps();

		e.setMaxPlayers(e.getNumPlayers() + 1);
		e.motd(Text.empty().color(TextColor.color(0xaa, 0xaa, 0xaa))

				.append(Text.plain(s.getName()))
				.appendSpace()
				.append(Text.plain(s.getMinecraftVersion()).color(TextColor.color(0xff, 0xff, 0xff)))
				.appendSpace()
				.append(Text.plain(String.valueOf(Math.round(tps * 100) / 100f)).color(Text.qualityGradient(tps)))
				.appendSpace()
				.append(Text.plain((System.currentTimeMillis() - Plugin.i().uptime) / 1000 + "s"))

				.appendNewline()

				.append(Text.plain(player == null ? "" : player.getName()).color(TextColor.color(0xff, 0xff, 0xff)))
				.append(Text.plain(player == null ? "" : "@" + e.getAddress().getHostAddress()))
				.append(Text.plain(player == null ? "" : " " + Text.date(new Date(player.getLastLogin())))));
	}

	@EventHandler
	private void login(final PlayerLoginEvent e) {
		Plugin.i().config.set("ipcache." + e.getAddress().getHostAddress(), e.getPlayer().getUniqueId().toString());
	}

	@EventHandler
	private void join(final PlayerJoinEvent e) {
		final var i = e.getPlayer();
		Player.join(i);
		e.joinMessage(Text.plain("+").appendSpace().asComponent().append(Text.player(i)));
	}

	@EventHandler
	private void quit(final PlayerQuitEvent e) {
		final var i = e.getPlayer();
		Player.quit(i);
		e.quitMessage(Text.plain("-").appendSpace().asComponent().append(Text.player(i)));
	}

	@EventHandler
	private void chat(final AsyncChatEvent e) {
		final var i = e.signedMessage();
		e.setCancelled(true);
		Plugin.s().broadcast(Text.empty()
				.append(Text.player(e.getPlayer()))
				.append(Text.plain(": "))
				.append(Text.plain(i.message())));
	}

	@EventHandler
	private void itemDespawn(final ItemDespawnEvent e) {
		e.getEntity().setGlowing(true);
	}

	@EventHandler
	private void playerPortal(final PlayerPortalEvent e) {
		final var to = e.getTo();
		if (e.getFrom().getWorld() == Plugin.i().world)
			to.setWorld(Random.w);
		if (e.getFrom().getWorld() == Random.w)
			to.setWorld(Plugin.i().world);
		e.setTo(to);
	}
}
