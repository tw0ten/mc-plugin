import java.awt.image.BufferedImage;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

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

	final static float scale = 1.5f;
	final static float interval = scale * 0.125f;

	public static void particles(final Location l, final BufferedImage img) {
		if (img == null)
			return;

		final Vector r = l.getDirection().crossProduct(new Vector(0, 1, 0)).normalize().multiply(interval);
		final Vector d = l.getDirection().crossProduct(r).normalize().multiply(interval);

		l.add(r.clone().multiply(-img.getWidth() / 2));
		l.add(d.clone().multiply(-img.getHeight() / 2));

		for (var x = 0; x < img.getWidth(); x++)
			for (var y = 0; y < img.getHeight(); y++)
				particle(l.clone().add(r.clone().multiply(x)).add(d.clone().multiply(y)),
						Color.fromARGB(img.getRGB(x, y)));
	}

	public static void particles(final Location from, final Location to, final BufferedImage img) {
		for (var x = 0; x < img.getWidth(); x++)
			for (var y = 0; y < img.getHeight(); y++)
				particle(from.clone(), Color.fromARGB(img.getRGB(x, y)));
	}

	private static void particle(final Location l, final Color c) {
		final var i = new Particle.DustOptions(c, scale);
		l.getWorld().spawnParticle(Particle.DUST, l, 0, i);
	}
}
