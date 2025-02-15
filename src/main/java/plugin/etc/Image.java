package plugin.etc;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import plugin.Command;
import plugin.Item;

public class Image {
	public interface mapDims {
		int w = 128, h = 128;
	}

	public static class Screen {
		public static Location l;
		public static Player p;

		public final static float scale = 1.5f;
		public final static float interval = scale * 0.125f;
	}

	public static ItemStack[] map(final BufferedImage img) {
		final var v = Bukkit.createMap(Bukkit.getWorlds().getFirst());
		v.getRenderers().clear();
		v.setScale(MapView.Scale.CLOSEST);
		v.addRenderer(new MapRenderer() {
			@Override
			public void render(final MapView v, final MapCanvas c, final Player p) {
				c.drawImage(0, 0, img);
			}
		});
		return Item.s(Item.m(Item.i(Material.FILLED_MAP), m -> {
			((MapMeta) m).setMapView(v);
			return m;
		}));
	}

	public static void particles(final BufferedImage img) {
		final var s = 16;
		final var l = Screen.l.clone();

		final var r = l.getDirection().crossProduct(new Vector(0, 1, 0)).normalize().multiply(Screen.interval);
		final var d = l.getDirection().crossProduct(r).normalize().multiply(Screen.interval);

		l.add(r.clone().multiply(-img.getWidth() / s / 2));
		l.add(d.clone().multiply(-img.getHeight() / s / 2));

		for (var x = 0; x < img.getWidth() / s; x++)
			for (var y = 0; y < img.getHeight() / s; y++)
				particle(l.clone().add(r.clone().multiply(x)).add(d.clone().multiply(y)),
						Color.fromARGB(img.getRGB(x * s, y * s)), Screen.scale);
	}

	public static void particles(final BufferedImage img, final float scale, final Location tl, final Location br) {
		final var d = (tl.getY() - br.getY()) / img.getHeight();
		final var r = br.clone().subtract(tl).multiply(1.0 / img.getWidth());
		r.setY(0);

		for (var x = 0; x < img.getWidth(); x++)
			for (var y = 0; y < img.getHeight(); y++)
				particle(tl.clone().add(r.clone().multiply(x)).add(0, d * y, 0),
						Color.fromARGB(img.getRGB(x, y)), scale);
	}

	public static void load() {
		Command.add(new Command("image") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (sender instanceof final Player p) {
					if (args.length < 1)
						return;
					switch (args[0]) {
						case "map":
							if (args.length < 2) {
								sender.sendMessage("url?");
								return;
							}
							try {
								final var i = ImageIO.read(URI.create(args[1]).toURL());
								Item.n(p, Image.map(resize(i, Image.mapDims.w, Image.mapDims.h)));
							} catch (final Exception e) {
							}
							return;
						case "screen":
							if (args.length < 2)
								return;
							switch (args[1]) {
								case "follow":
									if (Image.Screen.p == p)
										Image.Screen.p = null;
									else
										Image.Screen.p = p;
									return;
								case "clear":
									Image.Screen.p = null;
									Image.Screen.l = null;
									return;
								default:
							}
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
						return List.of("map", "screen");
					case 2:
						switch (args[0]) {
							case "screen":
								return List.of("follow", "clear");
							default:
						}
					default:
				}
				return List.of();
			}
		});
	}

	private static void particle(final Location l, final Color c, final float scale) {
		final var i = new Particle.DustOptions(c, scale);
		l.getWorld().spawnParticle(Particle.DUST, l, 0, i);
	}

	private static BufferedImage resize(final BufferedImage img, final int newW, final int newH) {
		final var tmp = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
		final var dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		final var g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}
}
