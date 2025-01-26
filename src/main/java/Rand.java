import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Rand {
	private static final Random r = new Random();

	private static ItemStack[][] books = new ItemStack[][] {};

	public static void load() {
		final var books = new ArrayList<>();
		final var lib = Plugin.instance.getDataPath().resolve("lib").toFile();
		for (final var author : lib.listFiles()) {
			for (final var book : author.listFiles()) {
				try {
					final var s = Files.readString(book.toPath());
					books.add(new Book(book.getName(), author.getName(), s).toItems());
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
		Rand.books = books.toArray(ItemStack[][]::new);
	}

	private final static Material[] allItems = Arrays.stream(Material.values())
			.filter(i -> i.isItem())
			.toArray(Material[]::new);

	private static <T> T pick(final T[] a) {
		return a[r.nextInt(a.length)];
	}

	public static ItemStack[] item() {
		final var m = pick(allItems);
		switch (m) {
			case Material.WRITTEN_BOOK:
				return pick(books);
			default:
		}
		return new ItemStack[] { new ItemStack(m) };
	}
}
