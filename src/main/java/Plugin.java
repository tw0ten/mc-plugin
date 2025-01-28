import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {
	public static JavaPlugin instance;

	public Plugin() {
		super();
	}

	@Override
	public void onLoad() {
		super.onLoad();
		Plugin.instance = this;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		Bukkit.getPluginManager().registerEvents(new Events(), this);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (final var p : Bukkit.getOnlinePlayers())
				if (!p.isDead())
					Item.n(p, Random.item());
		}, 0, 20 * 15);
		Command.load();
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}
}
