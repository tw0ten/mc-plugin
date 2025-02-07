package plugin;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Text {
	public static Component lore(final String s) {
		return Component.text()
				.content(s)
				.style(Style.style().decoration(TextDecoration.ITALIC, false)
						.color(TextColor.color(0xAAAAAA))
						.build())
				.build();
	}

	public static Component plain(final String s) {
		return Component.text().content(s).build();
	}

	public static Component empty() {
		return Component.empty();
	}

	public static Entity nametag(Location l) {
		final var e = l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
		e.setCustomNameVisible(true);
		e.setGravity(false);
		e.setInvulnerable(true);
		e.setInvisible(true);
		return e;
	}

	public static Component player(final Player p) {
		final var uuid = p.getUniqueId().toString();
		return p.displayName();
		/*
		 * .hoverEvent(HoverEvent.showText(plain(uuid)))
		 * .clickEvent(ClickEvent.copyToClipboard(uuid));
		 */
	}
}
