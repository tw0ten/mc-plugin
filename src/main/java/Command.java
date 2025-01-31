import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class Command {
	public Command() {
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
			protected List<String> complete(CommandSender sender, String[] args) {
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

		protected List<String> complete(CommandSender sender, String[] args) {
			return List.of();
		}
	}
}
