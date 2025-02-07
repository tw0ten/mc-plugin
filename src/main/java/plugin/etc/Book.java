package plugin.etc;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import plugin.Command;
import plugin.Item;
import plugin.Plugin;
import plugin.Text;

public class Book {
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
			for (final var l : lines)
				ls += (l.length() * 8 - 1) / max.width;
			return ls;
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
			int book = -1, page = -1;
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

	public static Book[] books() {
		final var books = new ArrayList<Book>();
		final var library = Plugin.instance.getDataPath().resolve("library").toFile();
		for (final var author : library.listFiles()) {
			if (author.isFile()) {
				books.add(Book.load(author.getName(), null));
				continue;
			}

			for (final var title : author.listFiles())
				books.add(Book.load(title.getName(), author.getName()));
		}
		return books.toArray(Book[]::new);
	}

	public static Book exception(final Exception e) {
		final var sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return new Book(e.getClass().getSimpleName(), sw.toString());
	}

	public static Book load(final String title, final String author) {
		final var lib = Plugin.instance.getDataPath().resolve("library");

		var p = lib;
		if (author != null)
			p = p.resolve(author);
		p = p.resolve(title);

		if (!p.getParent().equals(lib) && !p.getParent().getParent().equals(lib))
			return new Book("😐");

		try {
			return new Book(title, author, Files.readString(p));
		} catch (final Exception e) {
			return Book.exception(e);
		}
	}

	public static void load() {
		Command.add(new Command.Admin("book") {
			@Override
			protected void run(CommandSender sender, String[] args) {
				switch (args[0]) {
					case "find":
						final var s = String.join(" ", args).substring(args[0].length() + 1);
						sender.sendMessage(Text.plain("\"" + s + "\""));
						for (final var b : Book.books()) {
							final var r = b.find(s);
							if (r == null)
								continue;
							sender.sendMessage(Text.plain(r));
						}
						break;
					case "get":
						if (sender instanceof Player p) {
							int i;
							var title = "";
							for (i = 1; i < args.length; i++) {
								title += args[i];

								if (!args[i].endsWith(File.pathSeparator))
									break;
								title = title.substring(0, title.length() - 1);
								title += " ";
							}

							var author = "";
							for (i++; i < args.length; i++) {
								author += args[i];
								if (!args[i].endsWith(File.pathSeparator))
									break;
								author = author.substring(0, author.length() - 1);
								author += " ";
							}

							Item.n(p, Book.load(title, author).toItems());
						}
						return;
					default:
				}
			}

			@Override
			protected List<String> complete(final CommandSender sender, final String[] args) {
				switch (args.length) {
					case 1:
						return List.of("get", "find");
					default:
				}
				return List.of();
			}
		});
	}
}
