package plugin.etc;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

// TODO: ???
public class Audio {
	public static final float frequency = 20f;
	public final float[] waves;
	public float volume = 1f;
	public int i = 0;

	private final Sound sound = Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;

	public Audio(final float[] waves) {
		this(waves, Audio.frequency);
	}

	public Audio(final float[] waves, final float frequency) {
		if (frequency == Audio.frequency) {
			this.waves = waves;
			return;
		}

		if (frequency < Audio.frequency) {
			throw new IllegalArgumentException("TODO freq < target");
		}

		final var chunk = (int) (frequency / Audio.frequency);
		this.waves = new float[(waves.length + chunk - 1) / chunk];
		for (var i = 0; i < this.waves.length; i++) {
			final var start = i * chunk;
			final var end = Math.min(start + chunk, waves.length);
			var f = 0f;
			for (var j = start; j < end; j++)
				f += waves[j];
			this.waves[i] = f / (end - start);
		}
	}

	public void play(final Location l) {
		if (i >= this.waves.length || true)
			return;

		final var f = this.waves[i++];
		l.getWorld().playSound(l, this.sound, SoundCategory.RECORDS, f, f);
	}

	public static Audio readWavFile(final File f) throws Exception {
		final var audioStream = AudioSystem.getAudioInputStream(f);
		final var format = audioStream.getFormat();
		final var bytes = audioStream.readAllBytes();
		audioStream.close();
		final var audio = new Audio(convertBytesToFloats(bytes, format)[0], format.getSampleRate());
		plugin.Plugin.instance.getLogger()
				.info(f.getName() + ": "
						+ format.getChannels() + " channels, " + audio.waves.length + " waves, "
						+ audio.waves.length / Audio.frequency + " seconds");
		return audio;
	}

	// i have no clue how audio works ig
	private static float[][] convertBytesToFloats(final byte[] bytes, final AudioFormat format) {
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
						samples[c][i / 2 / channels] = sampleShort == 0 ? 0
								: 1f + sampleShort / Short.MAX_VALUE;
					}
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported bit depth: " + bitDepth);
		}

		return samples;
	}
}
