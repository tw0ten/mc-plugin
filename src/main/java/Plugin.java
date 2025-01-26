import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin implements Listener {
	public static JavaPlugin instance;

	public Plugin() {
		super();
	}

	@Override
	public void onLoad() {
		super.onLoad();
		Plugin.instance = this;

		Rand.load();
	}

	private void addItem(final Player p, final ItemStack[] items) {
		final var w = p.getWorld();
		final var m = p.getInventory().addItem(items);
		for (final var i : m.values()) {
			final var e = (Item) w.spawnEntity(p.getLocation(), EntityType.ITEM);
			e.setItemStack(i);
		}
	}

	@Override
	public void onEnable() {
		super.onEnable();
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (final var p : Bukkit.getOnlinePlayers())
				if (!p.isDead())
					addItem(p, Rand.item());
		}, 0, 20 * 15);
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}
}
