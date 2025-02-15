package plugin.etc.art;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import plugin.World;
import plugin.etc.Book;
import plugin.Command;
import plugin.Item;
import plugin.Plugin;
import plugin.Text;

public class Library {
	private final org.bukkit.World w;

	public Library() {
		{
			final var key = new NamespacedKey(Plugin.i(), "library");
			var w = Plugin.s().getWorld(key);
			if (w == null) {
				w = World.voidWorld(key);
				World.idle(w);
			}
			this.w = w;
		}

		Command.add(new Command.Admin("library") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (args.length < 1)
					return;
				switch (args[0]) {
					case "find":
						if (args.length < 2) {
							final var bs = books();
							for (final var b : bs)
								sender.sendMessage(b.toString());
							sender.sendMessage("" + bs.length);
							return;
						}
						final var s = String.join(" ", args).substring(args[0].length() + 1);
						sender.sendMessage(Text.plain("\"" + s + "\""));
						for (final var b : books()) {
							final var r = b.find(s);
							if (r == null)
								continue;
							sender.sendMessage(Text.plain(r));
						}
						return;
					case "get":
						if (sender instanceof final Player p) {
							var i = 0;

							var title = "";
							for (i = 1; i < args.length; i++) {
								title += args[i];
								if (!args[i].endsWith("/"))
									break;
								title = title.substring(0, title.length() - 1);
								title += " ";
							}

							var author = "";
							for (i++; i < args.length; i++) {
								author += args[i];
								if (!args[i].endsWith("/"))
									break;
								author = author.substring(0, author.length() - 1);
								author += " ";
							}

							Item.n(p, loadBook(title, author).toItems());
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

	private static Path path() {
		return Plugin.i().getDataPath().resolve("library");
	}

	public static Book[] books() {
		final var books = new ArrayList<Book>();
		final var library = path().toFile();
		for (final var author : library.listFiles()) {
			if (author.isFile()) {
				books.add(loadBook(author.getName(), null));
				continue;
			}
			for (final var title : author.listFiles())
				books.add(loadBook(title.getName(), author.getName()));
		}
		return books.toArray(Book[]::new);
	}

	public static Book loadBook(final String title, final String author) {
		final var library = path();
		var p = library;
		if (author != null)
			p = p.resolve(author);
		p = p.resolve(title);

		if (!p.getParent().equals(library) && !p.getParent().getParent().equals(library))
			return new Book("😐");

		try {
			return new Book(title, author, Files.readString(p));
		} catch (final Exception e) {
			return Book.exception(e);
		}
	}

}
