package plugin.etc;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class Audio {
	public static class Wave {
		final float volume;
		final float pitch;

		private Wave(final float volume, final float pitch) {
			this.volume = volume;
			this.pitch = pitch;
		}

		@Override
		public String toString() {
			return volume + " " + pitch;
		}
	}

	public static final float frequency = plugin.Plugin.tps();

	public static Audio load(final File f) throws Exception {
		final var waves = new ArrayList<Wave>();
		return new Audio(waves.toArray(Wave[]::new));
	}

	private final Sound sound = Sound.BLOCK_NOTE_BLOCK_HARP;

	public float volume = 1f;

	public int i = 0;

	public final Wave[] waves;

	public Audio(final Wave[] waves) {
		this.waves = waves;
	}

	public void play(final Location l) {
		if (i++ < 0)
			return;
		if (i > this.waves.length)
			return;

		final var f = this.waves[i - 1];
		plugin.Plugin.i().getLogger().info(i + "/" + this.waves.length + " " + f);

		l.getWorld().playSound(l, this.sound, SoundCategory.RECORDS, this.volume * f.volume, f.pitch);
	}
}
