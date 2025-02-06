package plugin.etc;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class Audio {
	public static final float frequency = 20f;

	private final Sound sound = Sound.BLOCK_NOTE_BLOCK_HARP;
	public float volume = 1f;
	public int i = 0;
	public final Wave[] waves;

	public class Wave {
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

	public Audio(final float[] waves) {
		this(waves, Audio.frequency);
	}

	public Audio(final float[] waves, final float frequency) {
		if (frequency < Audio.frequency) {
			throw new IllegalArgumentException("TODO freq < target");
		}

		final var chunk = (int) (frequency / Audio.frequency);
		this.waves = new Wave[(waves.length + chunk - 1) / chunk];
		plugin.Plugin.instance.getLogger().info(chunk + " " + this.waves.length);

		for (var i = 0; i < this.waves.length; i++) {
			final var frame = Arrays.copyOfRange(waves, i * chunk, Math.min((i + 1) * chunk, waves.length));
			final var pitch = detectPitchYIN(frame, frequency);
			var volume = 0f;
			for (final var f : frame)
				volume += f;
			// bigger chunks, overlapping? study audio theory instead of guessing and crying to ai
			this.waves[i] = new Wave(1 + volume / frame.length, pitch);
		}
	}

	public void play(final Location l) {
		if (i++ < 0)
			return;
		if (i > this.waves.length)
			return;

		final var f = this.waves[i - 1];
		plugin.Plugin.instance.getLogger().info(i + "/" + this.waves.length + " " + f);

		if (f.pitch == 0)
			return;

		l.getWorld().playSound(l, this.sound, SoundCategory.RECORDS, this.volume * f.volume, f.pitch);
	}

	private static float detectPitchYIN(final float[] frame, final float sampleRate) {
		final int maxTau = frame.length / 2;
		final float[] difference = new float[maxTau];

		for (int tau = 0; tau < maxTau; tau++) {
			for (int i = 0; i < maxTau; i++) {
				if (i + tau < frame.length) {
					final float delta = frame[i] - frame[i + tau];
					difference[tau] += delta * delta;
				}
			}
		}

		final float[] cmnd = new float[maxTau];
		cmnd[0] = 1.0f;
		float runningSum = 0.0f;
		for (int tau = 1; tau < maxTau; tau++) {
			runningSum += difference[tau];
			cmnd[tau] = difference[tau] * tau / runningSum;
		}

		final float threshold = 0.1f;
		int tauEstimate = 0;
		for (int tau = 2; tau < maxTau; tau++) {
			if (cmnd[tau] < threshold) {
				tauEstimate = tau;
				break;
			}
		}

		if (tauEstimate == 0)
			return tauEstimate;
		return sampleRate / tauEstimate;
	}

	private static float[] normalize(final float[] i) {
		var m = Float.MIN_VALUE;
		for (var f : i)
			m = Math.max(m, f);
		final var o = new float[i.length];
		for (var j = 0; j < i.length; j++)
			o[j] = i[j] / m;
		return o;
	}

	public static Audio load(final File f) throws Exception {
		final var audioStream = AudioSystem.getAudioInputStream(f);
		final var format = audioStream.getFormat();
		final var bytes = audioStream.readAllBytes();
		audioStream.close();
		final var audio = new Audio(normalize(readBytes(bytes, format)[0]), format.getSampleRate());
		plugin.Plugin.instance.getLogger()
				.info(f.getName() + ": "
						+ format.getChannels() + " channels, " + audio.waves.length + " waves, "
						+ audio.waves.length / Audio.frequency + " seconds");
		return audio;
	}

	private static float[][] readBytes(final byte[] bytes, final AudioFormat format) {
		final var bitDepth = format.getSampleSizeInBits();
		final var isBigEndian = format.isBigEndian();
		final var channels = format.getChannels();
		final var samples = new float[channels][bytes.length * Byte.SIZE / bitDepth / channels];

		switch (bitDepth) {
			case 16:
				for (var i = 0; i < bytes.length; i += 2 * channels) {
					for (var c = 0; c < channels; c++) {
						final var i0 = i + c * 2;
						final var b1 = bytes[i0 + 0] & 0xFF;
						final var b2 = bytes[i0 + 1] & 0xFF;
						final var sampleShort = (short) (isBigEndian ? ((b1 << 8) | b2) : ((b2 << 8) | b1));
						samples[c][i / 2 / channels] = (float) sampleShort / Short.MAX_VALUE;
					}
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported bit depth: " + bitDepth);
		}

		return samples;
	}
}
