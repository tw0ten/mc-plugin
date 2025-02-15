package plugin;

import static net.kyori.adventure.text.format.TextColor.color;

import java.time.Instant;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Text {
	public static Component lore(final String s) {
		return Component.text().content(s)
				.style(Style.style().decoration(TextDecoration.ITALIC, false)
						.color(color(0xAA, 0xAA, 0xAA)).build())
				.build();
	}

	public static Component plain(final String s) {
		return Component.text().content(s).build();
	}

	public static Component empty() {
		return Component.empty();
	}

	public static Entity nametag(final Location l) {
		final var e = l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
		e.setCustomNameVisible(true);
		e.setGravity(false);
		e.setInvulnerable(true);
		e.setInvisible(true);
		return e;
	}

	public static void sign(final Block block, final Component... s) {
		final var sign = (Sign) block.getState();
		final var side = sign.getSide(Side.FRONT);

		for (var i = 0; i < s.length; i++)
			if (s[i] != null)
				side.line(i, s[i]);

		sign.update();
	}

	public static TextColor qualityGradient(final float v) {
		return TextColor.lerp(v, color(0xff, 0x00, 0x00), color(0x00, 0xff, 0x00));
	}

	public static Component player(final Player p) {
		final var uuid = p.getUniqueId().toString();
		return p.displayName()
				.clickEvent(ClickEvent.copyToClipboard(uuid))
				.hoverEvent(HoverEvent.showText(Text.empty()
						.append(p.displayName())
						.appendNewline()
						.append(plain(p.getName() + " " + uuid))
						.appendNewline()
						.append(plain(new Date(plugin.Player.offline(p.getUniqueId()).getFirstPlayed()).toInstant()
								.toString()))
						.appendNewline()
						.append(Text.plain(p.isOp() ? "op" : ""))));
	}
}
