import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class Random {
	private static final java.util.Random r = new java.util.Random();

	public final static EntityType[] entities = EntityType.values();

	private final static Material[] items = Arrays.stream(Material.values())
			.filter(i -> i.isItem())
			.toArray(Material[]::new);

	public final static Material[] blocks = Arrays.stream(Material.values())
			.filter(i -> i.isBlock())
			.toArray(Material[]::new);

	private static <T> T pick(final T[] a) {
		if (a.length == 0)
			return null;
		return a[exc(a.length)];
	}

	public static int inc(int bound) {
		return exc(bound + 1);
	}

	public static int inc(int start, int bound) {
		return exc(start, bound + 1);
	}

	public static int exc(int bound) {
		return exc(0, bound);
	}

	public static int exc(int start, int bound) {
		return r.nextInt(start, bound);
	}

	public static Color color() {
		return Color.fromRGB(inc(0xFF), inc(0xFF), inc(0xFF));
	}

	public static Book book() {
		final var library = Plugin.instance.getDataPath().resolve("library").toFile();
		final var author = pick(library.listFiles());
		final var book = pick(author.listFiles());
		try {
			final var s = Files.readString(book.toPath());
			return new Book(book.getName(), author.getName(), s);
		} catch (final Exception e) {
			e.printStackTrace();
			return new Book("e", e.getClass().getSimpleName(), e.toString());
		}
	}

	public static Material block() {
		return pick(blocks);
	}

	public static ItemStack[] item() {
		final var m = pick(items);
		switch (m) {
			case WRITTEN_BOOK:
				return book().toItems();

			case ENCHANTED_BOOK:
				return Item.s(Item.m(Item.i(m), e -> {
					final var c = pick(Enchantment.values());
					((EnchantmentStorageMeta) e).addStoredEnchant(c, inc(c.getStartLevel(), c.getMaxLevel()),
							false);
					return e;
				}));

			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
				return Item.s(Item.m(Item.i(m), e -> {
					((LeatherArmorMeta) e).setColor(color());
					return e;
				}));

			case TIPPED_ARROW:
			case SPLASH_POTION:
			case LINGERING_POTION:
			case POTION:
				return Item.s(Item.m(Item.i(m), e -> {
					((PotionMeta) e).setBasePotionType(pick(PotionType.values()));
					return e;
				}));

			case PLAYER_HEAD:

			default:
		}
		return Item.s(Item.i(m));
	}

	public static Entity entity(final Location l) {
		return l.getWorld().spawnEntity(l, pick(entities));
	}

	public static BufferedImage image(int w, int h) {
		final var img = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				img.setRGB(x, y, new java.awt.Color(inc(0xFF), inc(0xFF), inc(0xFF)).getRGB());
		return img;
	}
}
