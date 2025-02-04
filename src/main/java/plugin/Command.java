package plugin;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import plugin.etc.*;

public class Command {
	public Command() {
		// TODO ???
		new Admin("reload") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				Bukkit.reload();
				sender.sendMessage("reloaded");
			}
		};
		new Default("ping") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				sender.sendMessage("pong");
			}
		};
		new Default("echo") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				sender.sendMessage(String.join(" ", args));
			}
		};
		new Default("image") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (sender instanceof Player p) {
					if (args.length < 1)
						return;
					switch (args[0]) {
						case "screen":
							if (args.length < 2)
								return;
							switch (args[1]) {
								case "follow":
									if (Image.Screen.p == p)
										Image.Screen.p = null;
									else
										Image.Screen.p = p;
									return;
								case "clear":
									Image.Screen.p = null;
									Image.Screen.l = null;
									return;
								default:
							}
							return;
						default:
					}
					return;
				}
			}

			@Override
			protected List<String> complete(CommandSender sender, String[] args) {
				switch (args.length) {
					case 1:
						return List.of("screen");
					case 2:
						switch (args[0]) {
							case "screen":
								return List.of("follow", "clear");
							default:
						}
					default:
				}
				return List.of();
			}
		};
		new Admin("book") {
			@Override
			protected void run(CommandSender sender, String[] args) {
				switch (args[0]) {
					case "find":
						final var s = String.join(" ", args).substring("find ".length());
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
							for (i = 0; i < args.length; i++) {
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
		};
		new Admin("random") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (sender instanceof final Player p) {
					if (args.length < 1)
						return;
					switch (args[0]) {
						case "book":
							Item.n(p, Random.book().toItems());
							return;
						case "item":
							Item.n(p, Random.item());
							return;
						case "entity":
							Random.entity(p.getLocation());
							return;
						case "map":
							Item.n(p, Random.map());
							return;
						default:
					}
					return;
				}
			}

			@Override
			protected List<String> complete(final CommandSender sender, final String[] args) {
				switch (args.length) {
					case 1:
						return List.of("book", "item", "entity", "map");
					default:
				}
				return List.of();
			}
		};
	}

	private static abstract class Admin extends Default {
		public Admin(final String label) {
			super(label);
		}

		protected boolean perms(final CommandSender sender) {
			return super.perms(sender) && sender.isOp();
		}
	}

	private static abstract class Default {
		public final PluginCommand command;

		public Default(final String label) {
			this.command = Plugin.instance.getCommand(label);

			this.command.setDescription("description");
			this.command.setUsage("usage");

			this.command.setExecutor(new CommandExecutor() {
				@Override
				public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command,
						final String label, final String[] args) {
					if (!perms(sender))
						return false;
					run(sender, args);
					return true;
				}
			});

			this.command.setTabCompleter(new TabCompleter() {
				@Override
				public List<String> onTabComplete(final CommandSender sender, final org.bukkit.command.Command command,
						final String label, final String[] args) {
					if (!perms(sender))
						return List.of();
					return complete(sender, args);
				}
			});
		}

		protected boolean perms(final CommandSender sender) {
			return true;
		}

		protected abstract void run(CommandSender sender, String[] args);

		protected List<String> complete(final CommandSender sender, final String[] args) {
			return List.of();
		}
	}
}
