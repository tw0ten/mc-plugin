package plugin.etc;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class Audio {
	public static final float frequency = 20f;

	private final Sound sound = Sound.BLOCK_NOTE_BLOCK_HARP;
	public float volume = 1f;
	public int i = 0;
	// [channels][waves]
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
		if (true) {
			this.waves = new Wave[0];
			return;
		}

		if (frequency < Audio.frequency) {
			throw new IllegalArgumentException("TODO freq < target");
		}

		final var chunk = (int) (frequency / Audio.frequency);
		this.waves = new Wave[(waves.length + chunk - 1) / chunk];
		plugin.Plugin.instance.getLogger().info(chunk + " " + this.waves.length);

		for (var i = 0; i < this.waves.length; i++) {
			final var frame = Arrays.copyOfRange(waves, i * chunk, Math.min((i + 1) * chunk, waves.length));
			final var pitchAndVolume = PitchVolumeAnalyzer.analyzePitchAndVolume(waves,
					Math.max(0, i * chunk - frame.length), Math.min(i * chunk + frame.length * 2, waves.length),
					frequency);
			final var pitch = (float) pitchAndVolume.getPitchHz() / frequency / 2f; // detectPitchYIN(frame, frequency);
			final var volume = (float) (pitchAndVolume.getVolumeDb() + 100f) / 50f;
			this.waves[i] = new Wave(volume, pitch);
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
		var m = 0f;
		for (final var f : i)
			m = Math.max(m, Math.abs(f));
		if (m == 0)
			return i;
		final var o = new float[i.length];
		for (var j = 0; j < i.length; j++)
			o[j] = i[j] / m;
		return o;
	}

	public static Audio load(final File f) throws Exception {
		final var audioStream = AudioSystem.getAudioInputStream(f);
		final var format = audioStream.getFormat();
		final var audio = new Audio(readBytes(audioStream, format)[0], format.getSampleRate());
		plugin.Plugin.instance.getLogger()
				.info(f.getName() + ": "
						+ format.getChannels() + " channels, " + audio.waves.length + " waves, "
						+ audio.waves.length / Audio.frequency + " seconds");
		audioStream.close();
		return audio;
	}

	private static float[][] readBytes(final AudioInputStream stream, final AudioFormat format) throws Exception {
		final var bitDepth = format.getSampleSizeInBits();
		final var isBigEndian = format.isBigEndian();
		final var channels = format.getChannels();
		final var bytes = stream.readAllBytes();
		final var samples = new float[channels][bytes.length * 8 / bitDepth / channels];

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


	// deepseek bs, no clue if it works or not
	// certainly takes its time though

	public class PitchVolumeAnalyzer {

		public static class PitchVolumeResult {
			private final double pitchHz;
			private final double volumeDb;

			public PitchVolumeResult(double pitchHz, double volumeDb) {
				this.pitchHz = pitchHz;
				this.volumeDb = volumeDb;
			}

			public double getPitchHz() {
				return pitchHz;
			}

			public double getVolumeDb() {
				return volumeDb;
			}
		}

		public static PitchVolumeResult analyzePitchAndVolume(float[] audioData, int startIndex, int endIndex,
				float sampleRate) {
			// Calculate volume in dB
			double volumeDb = calculateVolumeDb(audioData, startIndex, endIndex);

			// Calculate pitch in Hz
			double pitchHz = calculatePitch(audioData, startIndex, endIndex, sampleRate);

			return new PitchVolumeResult(pitchHz, volumeDb);
		}

		private static double calculateVolumeDb(float[] audioData, int start, int end) {
			int length = end - start;
			if (length <= 0)
				return -Double.MAX_VALUE;

			double sumSquares = 0.0;
			for (int i = start; i < end; i++) {
				float sample = audioData[i];
				sumSquares += sample * sample;
			}
			double rms = Math.sqrt(sumSquares / length);
			// Avoid log(0) by adding a tiny value to prevent -Infinity
			return 20 * Math.log10(rms + 1e-16);
		}

		private static double calculatePitch(float[] audioData, int start, int end, float sampleRate) {
			int length = end - start;
			if (length < 2)
				return 0.0;

			// Remove DC offset (mean) to improve autocorrelation
			float[] samples = new float[length];
			double mean = 0.0;
			for (int i = start; i < end; i++) {
				mean += audioData[i];
			}
			mean /= length;
			for (int i = 0; i < length; i++) {
				samples[i] = (float) (audioData[start + i] - mean);
			}

			// Autocorrelation to find the best lag
			int maxLag = Math.min(length / 2, 2000); // Limit lag to avoid low frequencies
			double maxCorrelation = -1;
			int bestLag = 0;

			for (int lag = 1; lag < maxLag; lag++) {
				double correlation = 0;
				for (int i = 0; i < length - lag; i++) {
					correlation += samples[i] * samples[i + lag];
				}
				correlation /= (length - lag); // Normalize by number of terms

				if (correlation > maxCorrelation) {
					maxCorrelation = correlation;
					bestLag = lag;
				}
			}

			// Apply a threshold to avoid false positives (e.g., noise)
			if (maxCorrelation < 0.1)
				return 0.0;

			return sampleRate / bestLag;
		}
	}
}
