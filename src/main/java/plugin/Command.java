package plugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public abstract class Command {
	public final String label;

	// TODO: actual command constructors
	public Command(final String label) {
		this.label = label;
	}

	protected boolean perms(final CommandSender sender) {
		return true;
	}

	protected abstract void run(CommandSender sender, String[] args);

	protected List<String> complete(final CommandSender sender, final String[] args) {
		return List.of();
	}

	public static abstract class Admin extends Command {
		public Admin(final String label) {
			super(label);
		}

		protected boolean perms(final CommandSender sender) {
			return super.perms(sender) && sender.isOp();
		}
	}

	public static void load() {
		add(new Admin("reload") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				Bukkit.reload();
				sender.sendMessage("reloaded");
			}
		});
		add(new Command("ping") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				sender.sendMessage("pong");
			}
		});
		add(new Command("disconnect") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (sender instanceof final org.bukkit.entity.Player p)
					p.kick(Text.plain("/" + this.label));
			}
		});
		add(new Command("echo") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				sender.sendMessage(String.join(" ", args));
			}
		});

		for (final var cmd : commands)
			register(cmd);
		commands.clear();
	}

	private static List<Command> commands = new ArrayList<>();

	public static void register(final Command cmd) {
		// final var map = Bukkit.getCommandMap()
		final var command = Plugin.instance.getCommand(cmd.label);

		command.setDescription("description");
		command.setUsage("usage");

		command.setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command,
					final String label, final String[] args) {
				if (!cmd.perms(sender))
					return false;
				cmd.run(sender, args);
				return true;
			}
		});

		command.setTabCompleter(new TabCompleter() {
			@Override
			public List<String> onTabComplete(final CommandSender sender, final org.bukkit.command.Command command,
					final String label, final String[] args) {
				if (!cmd.perms(sender))
					return List.of();
				return cmd.complete(sender, args);
			}
		});
	}

	public static void add(final Command cmd) {
		commands.add(cmd);
	}
}
