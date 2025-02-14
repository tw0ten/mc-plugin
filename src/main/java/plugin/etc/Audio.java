package plugin.etc;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioSystem;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

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
			final var volume = 1f;
			final var pitch = 1f;
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

	public static Audio load(final File f) throws Exception {
		final var audioStream = AudioSystem.getAudioInputStream(f);
		final var format = audioStream.getFormat();
		final var audio = new Audio(new float[] {}, format.getSampleRate());
		plugin.Plugin.instance.getLogger()
				.info(f.getName() + ": "
						+ format.getChannels() + " channels, " + audio.waves.length + " waves, "
						+ audio.waves.length / Audio.frequency + " seconds");
		audioStream.close();

		AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(f.toPath().toString(), 44100, 1024, 0);

		PitchDetectionHandler handler = new PitchDetectionHandler() {
			@Override
			public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
				System.out.println("Pitch: " + pitchDetectionResult.getPitch());
			}
		};

		dispatcher.addAudioProcessor(
				new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 1024, handler));
		new Thread(dispatcher).start();
		return audio;
	}
}
