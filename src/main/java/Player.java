import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Player {
	private static final Map<UUID, org.bukkit.entity.Player> s = new HashMap<>();

	public static void join(final org.bukkit.entity.Player p) {
		Player.s.put(p.getUniqueId(), p);
	}

	public static void quit(final org.bukkit.entity.Player p) {
		Player.s.remove(p.getUniqueId());
	}

	public static final Collection<org.bukkit.entity.Player> s() {
		return s.values();
	}

	private static class Wrapper {
	}
}
