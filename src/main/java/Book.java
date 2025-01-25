import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Book {
	private final String title, author, content;
	public final ItemStack[] items;

	public Book(final String title, final String author, final String content) {
		this.title = title;
		this.author = author;
		this.content = content;
		this.items = this.toItems();
	}

	private ItemStack defaultItem() {
		final ItemStack i = new ItemStack(Material.WRITTEN_BOOK);
		final BookMeta b = (BookMeta) i.getItemMeta();

		b.setGeneration(Generation.TATTERED);

		b.setAuthor(this.author);

		for (int j = 0;; j++) {
			b.setTitle(this.title.substring(0, this.title.length() - j));
			if (b.hasTitle())
				break;
		}

		i.setItemMeta(b);
		return i;
	}

	class Page {
		private interface max {
			int chars = 1023, width = 131, lines = 13;
		}

		private final String content;

		private static String chopWordBack(final String s) {
			for (int j = s.length() - 1; j >= 0; j--)
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
			int ls = lines.length;
			for (final String l : lines) {
				// !words
				// !monospace
				int ll = l.length() * 8; // avg
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

	interface max {
		int pages = 100;
	}

	private ItemStack[] toItems() {
		String s = this.content;
		final List<ItemStack> books = new ArrayList<>();

		while (s.length() > 0) {
			Plugin.instance.getLogger().info(this.toString() + " " + s.length());

			final ItemStack b = defaultItem();

			final List<Component> pages = new ArrayList<>();
			while (pages.size() < max.pages && s.length() > 0) {
				final Page p = new Page(s.substring(0, Math.min(Page.max.chars, s.length())));
				s = s.substring(p.toString().length());
				pages.add(p.asComponent());
			}

			final BookMeta m = (BookMeta) b.getItemMeta();
			m.pages(pages);
			b.setItemMeta(m);
			books.add(b);
		}
		Plugin.instance.getLogger().info("new Book: " + this.toString());

		if (books.size() > 1)
			for (int j = 0; j < books.size(); j++) {
				final ItemStack i = books.get(j);
				final ItemMeta m = i.getItemMeta();
				m.lore(List.of(Component.text()
						.content((j + 1) + " / " + books.size()).style(
								Style.style()
										.decoration(TextDecoration.ITALIC, false)
										.color(TextColor.color(0xaaaaaa)).build())
						.build()));
				i.setItemMeta(m);
				books.set(j, i);
			}

		return books.toArray(ItemStack[]::new);
	}

	@Override
	public String toString() {
		return "\"" + this.title + "\" by " + this.author;
	}
}
