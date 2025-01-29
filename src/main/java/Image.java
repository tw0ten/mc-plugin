import java.awt.image.BufferedImage;

import org.bukkit.Color;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class Image {
	public static ItemStack[] map(final BufferedImage img) {
		return Item.s(Item.m(Item.i(Material.MAP), m -> {
			return m;
		}));
	}

	final static float scale = 1.5f;
	final static float interval = scale * 0.125f;

	public static void particles(final Location l, final BufferedImage img) {
		l.add(-img.getWidth() / 2 * interval, -img.getHeight() / 2 * interval, 0);

		Vector worldUp = new Vector(0, 1, 0);
		Vector right = l.getDirection().crossProduct(worldUp).normalize();
		Vector cameraUp = l.getDirection().crossProduct(right).normalize();

		for (var x = 0; x < img.getWidth(); x++)
			for (var y = 0; y < img.getHeight(); y++)
				particle(
						l.clone()
								.add(cameraUp.clone().multiply(interval * x).add(right.clone().multiply(interval * y))),
						Color.fromARGB(img.getRGB(x, y)));
	}

	private static void particle(final Location l, final Color c) {
		final var i = new Particle.DustOptions(Random.color(), scale);
		l.getWorld().spawnParticle(Particle.DUST, l, 0, i);
	}
}
