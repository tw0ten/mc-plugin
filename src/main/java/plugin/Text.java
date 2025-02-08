package plugin;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
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

	public static void sign(Block block, Component... s) {
		final var sign = (Sign) block.getState();
		final var side = sign.getSide(Side.FRONT);

		for (var i = 0; i < s.length; i++)
			if (s[i] != null)
				side.line(i, s[i]);

		sign.update();
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
