package plugin.etc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import plugin.Plugin;
import plugin.Text;

public class Book {
	private class Page {
		private interface max {
			int chars = 1023, width = 131, lines = 13;
		}

		private static String chopWord(final String s) {
			var j = s.length();
			for (j--; j > 0; j--)
				if (Character.isWhitespace(s.charAt(j)))
					break;
			return s.substring(0, j);
		}

		private static int lines(final String s) {
			final String[] lines = s.split("\n");
			var ls = lines.length;
			for (final var l : lines)
				ls += (l.length() * 8 - 1) / max.width;
			return ls;
		}

		private final String content;

		public Page(String s) {
			assert s.length() <= max.chars;
			while (lines(s) > max.lines)
				s = chopWord(s);
			this.content = s;
		}

		@Override
		public String toString() {
			return content;
		}

		public Component asComponent() {
			return Text.plain(this.content);
		}
	}

	private interface max {
		int pages = 100;
	}

	public static Book exception(final Exception e) {
		final var sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return new Book(e.getClass().getSimpleName(), sw.toString());
	}

	public final String title, author, content;

	public Book(final String title, final String author, final String content) {
		this.title = title;
		this.author = author;
		this.content = content;
	}

	public Book(final String title, final String content) {
		this(title, null, content);
	}

	public Book(final String content) {
		this(null, null, content);
	}

	public ItemStack[] toItems() {
		var s = this.content;
		final List<ItemStack> books = new ArrayList<>();

		while (s.length() > 0) {
			Plugin.i().getLogger().info(this + " " + s.length());

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

		Plugin.i().getLogger().info(this + " (" + books.size() + ")");
		return books.toArray(ItemStack[]::new);
	}

	public String find(final String s) {
		if (!this.content.contains(s))
			return null;
		var o = this.toString() + "\n";
		final var items = Arrays.stream(this.toItems())
				.map(i -> ((BookMeta) i.getItemMeta()).pages().stream()
						.map(j -> ((TextComponent) j).content().length())
						.toArray(Integer[]::new))
				.toArray(Integer[][]::new);
		for (var i = -1; (i = this.content.indexOf(s, i)) != -1; o += "\n", i++) {
			o += i + ": ";
			var c = 0;
			var book = -1;
			var page = -1;
			for (book = 0; book < items.length; book++) {
				if (c >= i)
					break;
				for (page = 0; page < items[book].length; page++) {
					if (c >= i)
						break;
					c += items[book][page];
				}
			}
			o += "book " + book + ", page " + page;
		}
		return o.substring(0, o.length() - 1);
	}

	@Override
	public String toString() {
		if (this.author == null) {
			if (this.title == null)
				return super.toString();
			return "\"" + this.title + "\"";
		}
		return "\"" + this.title + "\" - " + this.author;
	}

	private ItemStack defaultItem() {
		final var i = new ItemStack(Material.WRITTEN_BOOK);
		final var b = (BookMeta) i.getItemMeta();
		final var lore = new ArrayList<Component>();

		b.setGeneration(Generation.TATTERED);

		b.setAuthor(this.author);
		if (this.title != null) {
			for (var j = 0; !b.hasTitle(); j++)
				b.setTitle(this.title.substring(0, this.title.length() - j));
			if (!this.title.equals(b.getTitle())) {
				lore.add(Text.lore("\"" + this.title + "\""));
				b.setTitle(b.getTitle().substring(0, b.getTitle().length() - 1) + "-");
			}
		}

		lore.add(Text.lore(String.valueOf(content.length())));
		b.lore(lore);

		i.setItemMeta(b);
		return i;
	}
}
