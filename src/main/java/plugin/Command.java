package plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public abstract class Command {
	public static abstract class Admin extends Command {
		public Admin(final String label) {
			super(label);
		}

		protected boolean perms(final CommandSender sender) {
			return super.perms(sender) && sender.isOp();
		}
	}

	private static List<Command> commands = new ArrayList<>();

	public static void load() {
		add(new Admin("reload") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				Plugin.s().reload();
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
		add(new Command("statistics") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (args.length < 1)
					return;
				final var s = Statistic.valueOf(args[0]);
				sender.sendMessage(s.name());
				for (final var p : Player.offline())
					sender.sendMessage(p.getName() + ": " +
							(args.length > 1 ? p.getStatistic(s, Material.valueOf(args[1])) : p.getStatistic(s)));
			}

			@Override
			protected List<String> complete(final CommandSender sender, final String[] args) {
				switch (args.length) {
					case 1:
						return Command.complete(Arrays.stream(Statistic.values()).map(i -> {
							return i.name();
						}), args[0]);
					case 2:
						if (Statistic.valueOf(args[0]).isSubstatistic())
							return materials(args[1]);
					default:
				}
				return List.of();
			}

		});

		for (final var cmd : commands)
			register(cmd);
		commands.clear();
	}

	public static void register(final Command command) {
		final var cmd = Plugin.i().getCommand(command.label);

		cmd.setDescription("description");
		cmd.setUsage("usage");

		cmd.setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command cmd,
					final String label, final String[] args) {
				if (!command.perms(sender))
					return false;
				command.run(sender, args);
				return true;
			}
		});

		cmd.setTabCompleter(new TabCompleter() {
			@Override
			public List<String> onTabComplete(final CommandSender sender, final org.bukkit.command.Command cmd,
					final String label, final String[] args) {
				if (!command.perms(sender))
					return List.of();
				return command.complete(sender, args);
			}
		});
	}

	public static void add(final Command cmd) {
		commands.add(cmd);
	}

	private static List<String> materials(final String arg) {
		return complete(Arrays.stream(Material.values()).map(i -> {
			return i.name();
		}), arg.toUpperCase());
	}

	public static List<String> complete(final Stream<String> stream, final String arg) {
		return stream.filter(i -> {
			return i.startsWith(arg);
		}).toList();
	}

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
}
