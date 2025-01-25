import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Rand {
	private static final Random r = new Random();

	private static ItemStack[][] books = new ItemStack[][] {};

	public static void load() {
		final List<ItemStack[]> books = new ArrayList<>();
		final File p = Plugin.instance.getDataPath().resolve("lib").toFile();
		for (final File author : p.listFiles()) {
			for (final File book : author.listFiles()) {
				try {
					final String s = Files.readString(book.toPath());
					books.add(new Book(book.getName(), author.getName(), s).items);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
		Rand.books = books.toArray(ItemStack[][]::new);
	}

	private final static Material[] allItems = Arrays.stream(Material.values())
			.filter((i) -> i.isItem())
			.toArray(Material[]::new);

	private static <T> T pick(final T[] a) {
		return a[r.nextInt(a.length)];
	}

	public static ItemStack[] item() {
		final Material m = pick(allItems);
		switch (m) {
			case Material.WRITTEN_BOOK:
				return pick(books);
			default:
				return new ItemStack[] { new ItemStack(m) };
		}
	}
}
