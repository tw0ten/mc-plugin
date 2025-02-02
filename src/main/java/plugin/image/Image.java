package plugin.image;

import java.awt.image.BufferedImage;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import plugin.Item;

public class Image {
	public interface mapDims {
		int w = 128, h = 128;
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
						Color.fromARGB(img.getRGB(x * s, y * s)));
	}

	private static void particle(final Location l, final Color c) {
		final var i = new Particle.DustOptions(c, Screen.scale);
		l.getWorld().spawnParticle(Particle.DUST, l, 0, i);
	}

	public static class Screen {
		public static Location l;
		public static Player p;

		public final static float scale = 1.5f;
		public final static float interval = scale * 0.125f;
	}
}
