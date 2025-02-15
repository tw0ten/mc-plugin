package plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import net.kyori.adventure.text.format.TextColor;

public class Player {
	private static final Map<UUID, org.bukkit.entity.Player> s = new HashMap<>();

	public static void join(final org.bukkit.entity.Player p) {
		Player.s.put(p.getUniqueId(), p);
		p.displayName(Text.plain(p.getName()));
		if (p.isOp())
			p.displayName(p.displayName().color(TextColor.color(0x40e0d0)));
	}

	public static void quit(final org.bukkit.entity.Player p) {
		Player.s.remove(p.getUniqueId());
	}

	public static final Collection<org.bukkit.entity.Player> s() {
		return s.values();
	}

	public static final UUID uuid(final String uuid) {
		try {
			return UUID.fromString(uuid);
		} catch (final Exception e) {
			return null;
		}
	}

	public static final OfflinePlayer offline(final UUID uuid) {
		if (uuid == null)
			return null;
		return Plugin.s().getOfflinePlayer(uuid);
	}

	public static final OfflinePlayer[] offline() {
		return Plugin.s().getOfflinePlayers();
	}

	private static class Wrapper {
	}
}
