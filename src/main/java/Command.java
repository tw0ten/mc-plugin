import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class Command {
	public static void load() {
		new Executor("ping") {
			@Override
			protected boolean perms(final CommandSender sender) {
				return true;
			}

			@Override
			protected void run(final CommandSender sender, final String[] args) {
				sender.sendMessage("pong");
			}
		};
		new Executor("echo") {
			@Override
			protected boolean perms(final CommandSender sender) {
				return true;
			}

			@Override
			protected void run(final CommandSender sender, final String[] args) {
				sender.sendMessage(String.join(" ", args));
			}
		};
		new Executor("random") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (!sender.isOp())
					return;
				if (sender instanceof final Player p) {
					if (args.length < 1)
						return;
					switch (args[0]) {
						case "book":
							Item.n(p, Random.book().toItems());
							return;
						default:
					}
					return;
				}
			}
		};
	}

	private static abstract class Executor {
		public final PluginCommand command;

		public Executor(final String label) {
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
		}

		protected boolean perms(final CommandSender sender) {
			return sender.isOp();
		}

		protected abstract void run(CommandSender sender, String[] args);
	}
}
