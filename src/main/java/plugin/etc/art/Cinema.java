package plugin.etc.art;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import plugin.Command;
import plugin.Plugin;
import plugin.Text;
import plugin.World;
import plugin.etc.Audio;
import plugin.etc.Image;

// extends world?
public class Cinema {
	private static class Subtitles {
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

		private static int HMSm2s(final String s) {
			final var HMS_m = s.split(",");
			final var HMS = HMS_m[0].split(":");
			final var H = Integer.parseInt(HMS[0]) * 60 * 60;
			final var M = Integer.parseInt(HMS[1]) * 60;
			final var S = Integer.parseInt(HMS[2]);
			final var m = Integer.parseInt(HMS_m[1]) / 1000;
			return H + M + S + m;
		}

		private static Subtitles loadSRT(final Path p) throws Exception {
			final var s = Files.readString(p).split("\n\n");
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

		private final Subtitle[] subs;

		private Subtitles(final Subtitle[] subs) {
			this.subs = subs;
		}

		private String at(final int tick) {
			for (var i = 0; i < subs.length; i++)
				if (subs[i].begins * tpf * fps <= tick && subs[i].ends * tpf * fps >= tick)
					return subs[i].toString();
			return "";
		}
	}

	private final static int fps = 5, tpf = (int) Plugin.tps() / fps;
	private final org.bukkit.World w;
	private final Block info;
	private final Location tl, br;

	private final Location subsL;
	private final Location aLL, aRL;
	private Path frames;

	private Subtitles subtitles;
	private Audio audio;

	public boolean paused = false;

	private String title;

	private final Entity e1, e2;

	public Cinema() {
		{
			final var key = new NamespacedKey(Plugin.i(), "cinema");
			var w = Plugin.s().getWorld(key);
			if (w == null) {
				w = World.voidWorld(key);
				World.idle(w);
				w.getWorldBorder().setSize(512);
			}
			this.w = w;
		}

		Command.add(new Command("cinema") {
			@Override
			protected void run(final CommandSender sender, final String[] args) {
				if (args.length < 1) {
					if (sender instanceof final Player p) {
						if (w.getPlayers().contains(p)) {
							p.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation().add(0.5, 0, 0.5));
							p.setGameMode(GameMode.SURVIVAL);
							return;
						}
						p.teleport(w.getSpawnLocation().add(0.5, 0, 0.5));
						p.setGameMode(GameMode.ADVENTURE);
					}
					return;
				}
				switch (args[0]) {
					case "seek":
						if (args.length < 2) {
							sender.sendMessage(
									"\"" + title + "\"" + (paused ? " paused" : "") + " " +
											audio.i + "/" + audio.waves.length);
							return;
						}
						audio.i = Integer.parseInt(args[1]);
						return;
					case "play":
						if (args.length < 2) {
							paused = false;
							return;
						}
						play(Path.of(args[1]));
						return;
					case "pause":
						paused = true;
						return;
					default:
				}
			}

			@Override
			protected List<String> complete(final CommandSender sender, final String[] args) {
				switch (args.length) {
					case 1:
						return List.of("seek", "play", "pause");
					default:
				}
				return List.of();
			}
		});

		this.info = new Location(w, 0, 3, 5).getBlock();

		this.tl = new Location(w, -7.5, 11.5, -14.5);
		this.br = this.tl.clone().add(16, 9, 0);

		this.aLL = new Location(w, -6.8, 3.5, 0.5);
		this.aRL = new Location(w, +7.8, 3.5, 0.5);

		this.subsL = new Location(w, 0.5, 0, -9);
		w.getEntitiesByClass(ArmorStand.class).forEach(e -> {
			if (e.isInvulnerable())
				e.remove();
		});
		e1 = Text.nametag(subsL.clone());
		e2 = Text.nametag(subsL.clone().add(0, -0.25, 0));

		Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.i(), () -> {
			if (w.getPlayers().isEmpty())
				return;
			this.tick();
		}, 0, 1);

		play(Plugin.i().getDataPath().resolve("movie"));
	}

	public void play(final Path movie) {
		try {
			this.subtitles = Subtitles.loadSRT(movie.resolve("subtitles"));
			this.audio = Audio.load(movie.resolve("audio").toFile());
			this.frames = movie.resolve("frames");
			this.title = movie.toRealPath().toFile().getName();
			this.paused = false;
			Text.sign(info, Text.plain(title));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void writeNametag(final String text) {
		final var s1 = text.split("\n")[0];
		final var s2 = s1.length() < text.length() ? text.split("\n")[1] : "";
		e1.customName(Text.plain(s1));
		e2.customName(Text.plain(s2));
	}

	private void tick() {
		final var i = audio.i / 2;

		if (i % tpf == 0)
			try {
				final var image = ImageIO.read(frames.resolve((i / tpf) + ".png").toFile());
				Image.particles(image, 1.2f, tl, br);
			} catch (final Exception e) {
			}

		if (paused)
			return;

		Text.sign(info, null, Text.plain(i + "/" + audio.waves.length / 2));

		audio.play(aLL);
		audio.play(aRL);
		if (i % 10 == 0) {
			w.spawnParticle(Particle.NOTE, aLL, 1);
			w.spawnParticle(Particle.NOTE, aRL, 1);
		}

		writeNametag(subtitles.at(i));
	}
}
