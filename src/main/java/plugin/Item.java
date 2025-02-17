package plugin;

import java.util.Arrays;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Item {
	public static final Material[] s = Arrays.stream(Material.values())
			.filter(i -> i.isItem())
			.toArray(Material[]::new);

	public static ItemStack i(final Material m) {
		return new ItemStack(m);
	}

	public static ItemStack m(final ItemStack i, final Function<ItemMeta, ItemMeta> m) {
		i.setItemMeta(m.apply(i.getItemMeta()));
		return i;
	}

	public static ItemStack[] s(final ItemStack... i) {
		return i;
	}

	public static void n(final Player p, final ItemStack[] items) {
		final var w = p.getWorld();
		final var m = p.getInventory().addItem(items).values();
		for (final var i : m)
			w.dropItem(p.getLocation(), i);
	}
}
