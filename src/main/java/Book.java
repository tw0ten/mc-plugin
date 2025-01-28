import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import net.kyori.adventure.text.Component;

public class Book {
	private final String title, author, content;

	public Book(final String title, final String author, final String content) {
		this.title = title;
		this.author = author;
		this.content = content;
	}

	private ItemStack defaultItem() {
		final var i = new ItemStack(Material.WRITTEN_BOOK);
		final var b = (BookMeta) i.getItemMeta();

		b.setGeneration(Generation.TATTERED);

		b.setAuthor(this.author);

		for (var j = 0; !b.hasTitle(); j++)
			b.setTitle(this.title.substring(0, this.title.length() - j));

		b.lore(List.of(Text.lore(String.valueOf(content.length()))));

		i.setItemMeta(b);
		return i;
	}

	class Page {
		private interface max {
			int chars = 1023, width = 131, lines = 13;
		}

		private final String content;

		private static String chopWordBack(final String s) {
			for (var j = s.length() - 1; j >= 0; j--)
				if (Character.isWhitespace(s.charAt(j)))
					return s.substring(0, j + 1);
			return s;
		}

		public Page(String s) {
			assert s.length() <= max.chars;
			while (lines(s) > max.lines)
				s = s.substring(0, s.length() - 1);
			this.content = chopWordBack(s);
		}

		private static int lines(final String s) {
			final String[] lines = s.split("\n");
			var ls = lines.length;
			for (final var l : lines) {
				// approximate
				final var ll = l.length() * 8;
				ls += (ll - 1) / max.width;
			}
			return ls;
		}

		@Override
		public String toString() {
			return content;
		}

		public Component asComponent() {
			return Component.text().content(this.content).build();
		}
	}

	private interface max {
		int pages = 100;
	}

	public ItemStack[] toItems() {
		var s = this.content;
		final List<ItemStack> books = new ArrayList<>();

		while (s.length() > 0) {
			Plugin.instance.getLogger().info(this + " " + s.length());

			final var b = defaultItem();

			final List<Component> pages = new ArrayList<>();
			while (pages.size() < max.pages && s.length() > 0) {
				final var p = new Page(s.substring(0, Math.min(Page.max.chars, s.length())));
				s = s.substring(p.toString().length());
				pages.add(p.asComponent());
			}

			final var m = (BookMeta) b.getItemMeta();
			m.pages(pages);
			b.setItemMeta(m);
			books.add(b);
		}

		for (var j = 0; j < books.size(); j++) {
			final var i = books.get(j);
			final var m = i.getItemMeta();
			final var lore = m.lore();
			if (books.size() > 1)
				lore.add(Text.lore((j + 1) + " / " + books.size()));
			m.lore(lore);
			i.setItemMeta(m);
			books.set(j, i);
		}

		Plugin.instance.getLogger().info(this + " (" + books.size() + ")");
		return books.toArray(ItemStack[]::new);
	}

	@Override
	public String toString() {
		return "\"" + this.title + "\" - " + this.author;
	}
}
