import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Text {
	public static Component lore(final String s) {
		return Component.text()
				.content(s).style(
						Style.style()
								.decoration(TextDecoration.ITALIC, false)
								.color(TextColor.color(0xaaaaaa)).build())
				.build();
	}

	public static Component plain(final String s) {
		return Component.text().content(s).build();
	}
}
