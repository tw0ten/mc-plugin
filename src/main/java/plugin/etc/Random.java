package plugin.etc;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionType;

import com.google.common.collect.Lists;

import io.papermc.paper.potion.SuspiciousEffectEntry;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import plugin.Command;
import plugin.Item;
import plugin.Plugin;
import plugin.World;
import plugin.etc.art.Library;

public class Random {
	public static final Random i = new Random(new java.util.Random());

	public static org.bukkit.World w;

	public static final EntityType[] entities = EntityType.values();

	public static final Material[] blocks = Arrays.stream(Material.values())
			.filter(i -> i.isBlock()).toArray(Material[]::new);

	private static final Material[] pottery = Arrays.stream(Item.s)
			.filter(i -> i.name().endsWith("_POTTERY_SHERD")).toArray(Material[]::new);

	private static final Material[] smithing = Arrays.stream(Item.s)
			.filter(i -> i.name().endsWith("_SMITHING_TEMPLATE")).toArray(Material[]::new);

	private final static PatternType[] banners = RegistryAccess.registryAccess()
			.getRegistry(RegistryKey.BANNER_PATTERN).stream().toArray(PatternType[]::new);

	private final static TrimMaterial[] trimMaterials = RegistryAccess.registryAccess()
			.getRegistry(RegistryKey.TRIM_MATERIAL).stream().toArray(TrimMaterial[]::new);

	private final static TrimPattern[] trimPatterns = RegistryAccess.registryAccess()
			.getRegistry(RegistryKey.TRIM_PATTERN).stream().toArray(TrimPattern[]::new);

	private final static Enchantment[] enchantments = RegistryAccess.registryAccess()
			.getRegistry(RegistryKey.ENCHANTMENT).stream().toArray(Enchantment[]::new);

	public static void load() {
		Command.add(new Command.Admin("random") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (sender instanceof final Player p) {
					if (args.length < 1)
						return;
					switch (args[0]) {
						case "book":
							Item.n(p, i.book().toItems());
							return;
						case "item":
							if (args.length < 2) {
								Item.n(p, i.item());
								return;
							}
							Item.n(p, i.item(Material.valueOf(args[1])));
							return;
						case "entity":
							i.entity(p.getLocation());
							return;
						case "map":
							Item.n(p, i.map());
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
					case 2:
						if (args[0].equals("item"))
							return Command.complete(Arrays.stream(Item.s).map(i -> {
								return i.name();
							}), args[1].toUpperCase());
					default:
				}
				return List.of();
			}
		});

		final var key = new NamespacedKey(Plugin.i(), "random");
		Random.w = Plugin.s().getWorld(key);
		if (Random.w == null)
			Random.w = World.randomWorld(key);
	}

	private final java.util.Random r;

	public Random(final java.util.Random r) {
		this.r = r;
	}

	public <T> T pick(final T[] a) {
		if (a.length == 0)
			return null;
		return a[exc(a.length)];
	}

	public <T> T pick(final List<T> a) {
		if (a.isEmpty())
			return null;
		return a.get(exc(a.size()));
	}

	public <T> T pick(final Iterator<T> a) {
		return pick(Lists.newArrayList(a));
	}

	public <T> T pick(final Stream<T> a) {
		return pick(a.iterator());
	}

	public <T extends Keyed> T pick(final Registry<T> a) {
		return pick(a.iterator());
	}

	public boolean coin() {
		return r.nextBoolean();
	}

	public boolean oneIn(final int c) {
		return inc(0, c) == 0;
	}

	public boolean chance(final float f) {
		return r.nextFloat(1) < f;
	}

	public int inc(final int bound) {
		return exc(bound + 1);
	}

	public int inc(final int start, final int bound) {
		return exc(start, bound + 1);
	}

	public int exc(final int bound) {
		return exc(0, bound);
	}

	public int exc(final int start, final int bound) {
		return r.nextInt(start, bound);
	}

	public Color color() {
		return Color.fromRGB(inc(0xFF), inc(0xFF), inc(0xFF));
	}

	public Book book() {
		final var b = pick(Library.books());
		return Library.loadBook(b.title, b.author);
	}

	public Material block() {
		return pick(blocks);
	}

	public ItemStack[] item() {
		return item(pick(Item.s));
	}

	public FireworkEffect firework() {
		return FireworkEffect.builder()
				.with(pick(FireworkEffect.Type.values()))
				.trail(coin())
				.flicker(coin())
				.withColor(color())
				.withFade(color(), color())
				.build();
	}

	public ItemStack[] item(final Material m) {
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
			case WRITTEN_BOOK:
				return book().toItems();

			case FIREWORK_ROCKET:
				return Item.s(Item.m(Item.i(m), e -> {
					((FireworkMeta) e).setPower(inc(255));
					final var l = exc(10);
					for (var i = 0; i < l; i++)
						((FireworkMeta) e).addEffect(firework());
					return e;
				}));

			case ENCHANTED_BOOK:
				return Item.s(Item.m(Item.i(m), e -> {
					final var c = pick(enchantments);
					((EnchantmentStorageMeta) e).addStoredEnchant(c, inc(c.getStartLevel(), c.getMaxLevel()), false);
					return e;
				}));

			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
				return Item.s(Item.m(Item.i(m), e -> {
					((LeatherArmorMeta) e).setColor(color());
					((ArmorMeta) e).setTrim(trim());
					return e;
				}));

			case TURTLE_HELMET:
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
			case GOLDEN_HELMET:
			case GOLDEN_CHESTPLATE:
			case GOLDEN_LEGGINGS:
			case GOLDEN_BOOTS:
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
			case NETHERITE_HELMET:
			case NETHERITE_CHESTPLATE:
			case NETHERITE_LEGGINGS:
			case NETHERITE_BOOTS:
				return Item.s(Item.m(Item.i(m), e -> {
					((ArmorMeta) e).setTrim(trim());
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
					((SkullMeta) i).setOwningPlayer(pick(plugin.Player.offline()));
					return i;
				}));

			case FILLED_MAP:
				return map();

			case SUSPICIOUS_STEW:
				return Item.s(Item.m(Item.i(m), i -> {
					((SuspiciousStewMeta) i).addCustomEffect(SuspiciousEffectEntry.create(
							pick(PotionType.values()).getPotionEffects().getFirst().getType(),
							(int) Plugin.tps() * 30), true);
					return i;
				}));

			default:
		}
		return Item.s(Item.i(m));
	}

	public ItemStack[] map() {
		return Image.map(image(Image.mapDims.w, Image.mapDims.h));
	}

	public Entity entity(final Location l) {
		return l.getWorld().spawnEntity(l, pick(entities));
	}

	public BufferedImage image(final int w, final int h) {
		final var img = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		for (var x = 0; x < img.getWidth(); x++)
			for (var y = 0; y < img.getHeight(); y++)
				img.setRGB(x, y, new java.awt.Color(inc(0xFF), inc(0xFF), inc(0xFF)).getRGB());
		return img;
	}

	public void chunk(final Chunk c) {
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

	private DyeColor dyeColor() {
		return pick(DyeColor.values());
	}

	private Pattern pattern() {
		return new Pattern(dyeColor(), pick(banners));
	}

	private ArmorTrim trim() {
		if (oneIn(trimMaterials.length * trimPatterns.length + 1))
			return null;
		return new ArmorTrim(pick(trimMaterials), pick(trimPatterns));
	}
}
