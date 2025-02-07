package plugin.etc;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionType;

import com.google.common.collect.Lists;

import io.papermc.paper.potion.SuspiciousEffectEntry;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import plugin.Command;
import plugin.Item;

public class Random {
	private static final java.util.Random r = new java.util.Random();

	public final static EntityType[] entities = EntityType.values();

	public final static Material[] blocks = Arrays.stream(Material.values())
			.filter(i -> i.isBlock()).toArray(Material[]::new);

	public static <T> T pick(final T[] a) {
		if (a.length == 0)
			return null;
		return a[exc(a.length)];
	}

	public static <T> T pick(final List<T> a) {
		if (a.isEmpty())
			return null;
		return a.get(exc(a.size()));
	}

	public static <T> T pick(final Iterator<T> a) {
		return pick(Lists.newArrayList(a));
	}

	public static <T> T pick(final Stream<T> a) {
		return pick(a.iterator());
	}

	public static int inc(final int bound) {
		return exc(bound + 1);
	}

	public static int inc(final int start, final int bound) {
		return exc(start, bound + 1);
	}

	public static int exc(final int bound) {
		return exc(0, bound);
	}

	public static int exc(final int start, final int bound) {
		return r.nextInt(start, bound);
	}

	public static Color color() {
		return Color.fromRGB(inc(0xFF), inc(0xFF), inc(0xFF));
	}

	public static Book book() {
		final var b = pick(Book.books());
		return Book.load(b.title, b.author);
	}

	public static Material block() {
		return pick(blocks);
	}

	private static DyeColor dyeColor() {
		return pick(DyeColor.values());
	}

	private static Pattern pattern() {
		return new Pattern(dyeColor(),
				pick(RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).stream()));
	}

	public static final int itemInterval = 20 * 15;

	private static final Material[] pottery = Arrays.stream(Item.s)
			.filter(i -> i.name().endsWith("_POTTERY_SHERD")).toArray(Material[]::new);
	private static final Material[] smithing = Arrays.stream(Item.s)
			.filter(i -> i.name().endsWith("_SMITHING_TEMPLATE")).toArray(Material[]::new);

	public static ItemStack[] item() {
		final var m = pick(Item.s);

		if (Arrays.stream(pottery).anyMatch(m::equals)) {
			if (m == pottery[0])
				return Item.s(Item.i(pick(pottery)));
			return item();
		}
		if (Arrays.stream(smithing).anyMatch(m::equals)) {
			if (m == smithing[0])
				return Item.s(Item.i(pick(smithing)));
			return item();
		}

		switch (m) {
			case KNOWLEDGE_BOOK:
			case WRITTEN_BOOK:
				return book().toItems();

			case ENCHANTED_BOOK:
				return Item.s(Item.m(Item.i(m), e -> {
					final var c = pick(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).iterator());
					((EnchantmentStorageMeta) e).addStoredEnchant(c, inc(c.getStartLevel(), c.getMaxLevel()), false);
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

			case SHIELD:
				return Item.s(Item.m(Item.i(m), i -> {
					((ShieldMeta) i).setBaseColor(dyeColor());
					final var l = inc(6);
					for (var k = 0; k < l; k++)
						((ShieldMeta) i).addPattern(pattern());
					return i;
				}));

			case BLACK_BANNER:
			case BLUE_BANNER:
			case BROWN_BANNER:
			case CYAN_BANNER:
			case GRAY_BANNER:
			case GREEN_BANNER:
			case LIGHT_BLUE_BANNER:
			case LIGHT_GRAY_BANNER:
			case LIME_BANNER:
			case MAGENTA_BANNER:
			case ORANGE_BANNER:
			case PINK_BANNER:
			case PURPLE_BANNER:
			case RED_BANNER:
			case WHITE_BANNER:
			case YELLOW_BANNER:
				return Item.s(Item.m(Item.i(m), i -> {
					final var l = inc(6);
					for (var k = 0; k < l; k++)
						((BannerMeta) i).addPattern(pattern());
					return i;
				}));

			case PLAYER_HEAD:
				return Item.s(Item.m(Item.i(m), i -> {
					((SkullMeta) i).setOwningPlayer(pick(Bukkit.getOfflinePlayers()));
					return i;
				}));

			case FILLED_MAP:
				return map();

			case SUSPICIOUS_STEW:
				return Item.s(Item.m(Item.i(m), i -> {
					((SuspiciousStewMeta) i).addCustomEffect(
							SuspiciousEffectEntry
									.create(pick(PotionType.values()).getPotionEffects().getFirst().getType(), 20 * 30),
							true);
					return i;
				}));

			default:
		}
		return Item.s(Item.i(m));

	}

	public static ItemStack[] map() {
		return Image.map(image(Image.mapDims.w, Image.mapDims.h));
	}

	public static Entity entity(final Location l) {
		return l.getWorld().spawnEntity(l, pick(entities));
	}

	public static BufferedImage image(final int w, final int h) {
		final var img = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		for (var x = 0; x < img.getWidth(); x++)
			for (var y = 0; y < img.getHeight(); y++)
				img.setRGB(x, y, new java.awt.Color(inc(0xFF), inc(0xFF), inc(0xFF)).getRGB());
		return img;
	}

	public static void chunk(final Chunk c) {
		final var y0 = c.getWorld().getMaxHeight();
		final var x0 = 16;
		final var z0 = 16;

		for (var y = c.getWorld().getMinHeight(); y < y0; y++) {
			for (var x = 0; x < x0; x++) {
				for (var z = 0; z < z0; z++) {
					final var b = c.getBlock(x, y, z);
					if (b.getType() == Material.AIR)
						continue;
					b.setType(block());
				}
			}
		}
	}

	public static void load() {
		Command.add(new Command.Admin("random") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (sender instanceof final Player p) {
					if (args.length < 1)
						return;
					switch (args[0]) {
						case "book":
							Item.n(p, Random.book().toItems());
							return;
						case "item":
							Item.n(p, Random.item());
							return;
						case "entity":
							Random.entity(p.getLocation());
							return;
						case "map":
							Item.n(p, Random.map());
							return;
						default:
					}
					return;
				}
			}

			@Override
			protected List<String> complete(final CommandSender sender, final String[] args) {
				switch (args.length) {
					case 1:
						return List.of("book", "item", "entity", "map");
					default:
				}
				return List.of();
			}
		});
	}
}
