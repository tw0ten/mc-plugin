package plugin.etc.art;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Entity;

import plugin.World;
import plugin.Plugin;
import plugin.Text;
import plugin.etc.Audio;
import plugin.etc.Image;

public class Cinema {
	private final org.bukkit.World w;
	private final Location tl, br;
	private final Location subsL;
	private final Location aLL, aRL;

	private Path movie;
	private Subtitles subtitles;
	private Audio audio;

	private final static int fps = 5, tpf = 20 / fps;

	private int i = 125000;

	public Cinema() {
		final var key = new NamespacedKey(Plugin.instance, "cinema");
		var w = Plugin.instance.getServer().getWorld(key);
		if (w == null) {
			w = World.voidWorld(key);
			World.idle(w);
		}
		this.w = w;

		this.tl = new Location(w, -7.5, 11.5, -14.5);
		this.br = this.tl.clone().add(16, 9, 0);
		this.subsL = new Location(w, -2, 1, -9);
		this.aLL = tl;
		this.aRL = br;

		e1 = Text.nametag(subsL.clone().add(2.5, -1, 0));
		e2 = Text.nametag(subsL.clone().add(2.5, -1.25, 0));

		try {
			this.movie = Path.of("/home/twoten/store/torrent/bladerunner");
			this.subtitles = Subtitles.loadSRT(movie.resolve("subtitles").toFile());
			this.audio = Audio.load(movie.resolve("audio").toFile()); // audio too fucking big negativearraysize exception
		} catch (final Exception e) {
			e.printStackTrace();
		}

		Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.instance, () -> {
			if (this.w.getPlayers().isEmpty())
				return;
			this.tick(i++);
		}, 0, 1);
	}

	private final Entity e1, e2;

	private void writeSigns(final String text) {
		final var l = this.subsL.clone().add(-1, 0, 0);
		final var maxChars = 15;
		final var s1 = text == null ? null : text.split("\n")[0];
		final var s2 = s1 != null && s1.length() < text.length() ? text.split("\n")[1] : null;

		for (var x = 0; x < 5; x++) {
			final var sign = (Sign) l.add(1, 0, 0).getBlock().getState();
			final var side = sign.getSide(Side.FRONT);

			side.line(1, s1 == null ? Text.empty() : Text.plain(sub(s1, maxChars * x, maxChars * (x + 1))));
			side.line(2, s2 == null ? Text.empty() : Text.plain(sub(s2, maxChars * x, maxChars * (x + 1))));

			sign.update();
		}
	}

	private void writeNametag(final String text) {
		final var s1 = text == null ? null : text.split("\n")[0];
		final var s2 = s1 != null && s1.length() < text.length() ? text.split("\n")[1] : null;
		e1.customName(s1 == null ? Text.empty() : Text.plain(s1));
		e2.customName(s2 == null ? Text.empty() : Text.plain(s2));
	}

	private static String sub(final String s, final int b, final int e) {
		return s.substring(Math.min(b, s.length()), Math.min(e, s.length()));
	}

	private void tick(final int i) {
		writeSigns("\n" + i);
		writeNametag(subtitles.at(i));
		if (i % tpf == 0) {
			try {
				final var image = ImageIO.read(movie.resolve("frames").resolve((i / tpf) + ".png").toFile());
				Image.particles(image, 1.2f, tl, br);
			} catch (final Exception e) {
			}
		}
	}

	private static class Subtitles {
		private final Subtitle[] subs;

		private static class Subtitle {
			private final String text;
			public final int begins, ends;

			public Subtitle(final String text, final int begins, final int ends) {
				this.text = text;
				this.begins = begins;
				this.ends = ends;
			}

			@Override
			public String toString() {
				return text;
			}
		}

		private String at(final int tick) {
			for (var i = 0; i < subs.length; i++)
				if (subs[i].begins * 20 <= tick && subs[i].ends * 20 >= tick)
					return subs[i].toString();
			return null;
		}

		private Subtitles(final Subtitle[] subs) {
			this.subs = subs;
		}

		private static int HMSm2s(final String s) {
			final var HMS_m = s.split(",");
			final var HMS = HMS_m[0].split(":");
			final var H = Integer.parseInt(HMS[0]) * 60 * 60;
			final var M = Integer.parseInt(HMS[1]) * 60;
			final var S = Integer.parseInt(HMS[2]);
			final var m = Integer.parseInt(HMS_m[1]) / 1000;
			return H + M + S + m;
		}

		private static Subtitles loadSRT(final File f) throws Exception {
			final var s = Files.readString(f.toPath()).split("\n\n");
			final var o = new Subtitle[s.length];
			for (var i = 0; i < s.length; i++) {
				final var j = s[i].split("\n");

				final var time = j[1].split(" --> ");
				final var begins = HMSm2s(time[0]);
				final var ends = HMSm2s(time[1]);

				final var text = String.join("\n", Arrays.copyOfRange(j, 2, j.length));

				o[i] = new Subtitle(text, begins, ends);
			}
			return new Subtitles(o);
		}
	}
}
