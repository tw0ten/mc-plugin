package plugin;

import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

public class Scoreboard {
	private static void set(final Objective o, final String text) {
		o.getScore(text).setScore(0);
	}

	private final org.bukkit.scoreboard.Scoreboard board;

	public Scoreboard() {
		this.board = Plugin.s().getScoreboardManager().getMainScoreboard();

		if (true)
			return;

		final var sidebar = create(DisplaySlot.SIDEBAR);
		sidebar.displayName(Text.plain("sidebar"));

		final var tab = create(DisplaySlot.PLAYER_LIST);
		tab.displayName(Text.plain("tab"));

		final var name = create(DisplaySlot.BELOW_NAME);
		name.displayName(Text.plain("name"));

		Plugin.s().getScheduler().scheduleSyncRepeatingTask(Plugin.i(), () -> {
			set(sidebar, "xd");
			set(name, "xd");
			set(tab, "xd");

			for (final var p : Player.s())
				p.setScoreboard(board);
		}, 0, (long) Plugin.tps() * 5);
	}

	private Objective create(final DisplaySlot ds) {
		final var o = board.registerNewObjective(ds.name(), Criteria.DUMMY, Text.empty());
		o.setDisplaySlot(ds);
		o.setAutoUpdateDisplay(false);
		return o;
	}
}
