package com.coraldrift.audio;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Synthesizes short PCM tones at runtime — no audio files required.
 * All game sounds are described by a ToneDesc and played asynchronously.
 */
public class ToneGenerator {

    private static final int SAMPLE_RATE = 44100;
    private static final int BYTES_PER_SAMPLE = 2; // 16-bit

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audio-synth");
        t.setDaemon(true);
        return t;
    });

    // ─── Wave shape ───────────────────────────────────────────────────────────

    public enum WaveShape { SINE, SQUARE, SAWTOOTH, TRIANGLE }

    // ─── Tone descriptor ──────────────────────────────────────────────────────

    public static class ToneDesc {
        public final WaveShape shape;
        public final double[]  frequencies;  // Hz — interpolated linearly over duration
        public final double    durationSeconds;
        public final double    attackSeconds;
        public final double    releaseSeconds;
        public final double    volume;        // 0.0–1.0
        public final boolean   reverb;        // simple 80ms delay echo

        public ToneDesc(WaveShape shape, double[] frequencies,
                        double durationSeconds, double attackSeconds,
                        double releaseSeconds, double volume, boolean reverb) {
            this.shape           = shape;
            this.frequencies     = frequencies;
            this.durationSeconds = durationSeconds;
            this.attackSeconds   = attackSeconds;
            this.releaseSeconds  = releaseSeconds;
            this.volume          = volume;
            this.reverb          = reverb;
        }

        /** Convenience constructor — no reverb. */
        public ToneDesc(WaveShape shape, double[] frequencies,
                        double durationSeconds, double attackSeconds,
                        double releaseSeconds, double volume) {
            this(shape, frequencies, durationSeconds, attackSeconds, releaseSeconds, volume, false);
        }
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /** Synthesize and play the tone asynchronously. Returns immediately. */
    public void playAsync(ToneDesc desc) {
        executor.submit(() -> {
            try {
                byte[] pcm = synthesize(desc);
                play(pcm);
            } catch (Exception ignored) {
                // Graceful degradation — audio system may not be available
            }
        });
    }

    public void dispose() {
        executor.shutdownNow();
    }

    // ─── Synthesis ────────────────────────────────────────────────────────────

    private byte[] synthesize(ToneDesc desc) {
        int totalSamples = (int) (SAMPLE_RATE * desc.durationSeconds);
        int echoDelaySamples = (int) (SAMPLE_RATE * 0.08); // 80ms
        int bufferSize = desc.reverb ? totalSamples + echoDelaySamples : totalSamples;

        double[] samples = new double[bufferSize];
        double phase = 0;

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double tNorm = (desc.durationSeconds > 0) ? t / desc.durationSeconds : 0;

            // Sweep frequency across the frequencies array
            double freqIdx = tNorm * (desc.frequencies.length - 1);
            int f0 = (int) freqIdx;
            int f1 = Math.min(f0 + 1, desc.frequencies.length - 1);
            double frac = freqIdx - f0;
            double freq = desc.frequencies[f0] + frac * (desc.frequencies[f1] - desc.frequencies[f0]);
            if (freq <= 0) freq = 1;

            // Advance phase accumulator (avoids discontinuity in sweeps)
            phase += freq / SAMPLE_RATE;
            double p = phase - Math.floor(phase); // 0..1

            // Wave shape
            double raw;
            switch (desc.shape) {
                case SQUARE:   raw = (p < 0.5) ? 1.0 : -1.0; break;
                case SAWTOOTH: raw = 2.0 * p - 1.0; break;
                case TRIANGLE: raw = 1.0 - 4.0 * Math.abs(p - 0.5); break;
                default:       raw = Math.sin(2 * Math.PI * p); break; // SINE
            }

            // ADSR envelope (attack + release only — sufficient for SFX)
            double envelope = 1.0;
            if (t < desc.attackSeconds && desc.attackSeconds > 0) {
                envelope = t / desc.attackSeconds;
            } else {
                double timeUntilEnd = desc.durationSeconds - t;
                if (timeUntilEnd < desc.releaseSeconds && desc.releaseSeconds > 0) {
                    envelope = timeUntilEnd / desc.releaseSeconds;
                }
            }

            samples[i] += raw * envelope * desc.volume;
        }

        // Optional reverb: add 80ms delayed echo at 35% amplitude
        if (desc.reverb) {
            for (int i = 0; i < totalSamples; i++) {
                int echoIdx = i + echoDelaySamples;
                if (echoIdx < bufferSize) {
                    samples[echoIdx] += samples[i] * 0.35;
                }
            }
        }

        // Convert to 16-bit little-endian PCM bytes
        byte[] pcm = new byte[bufferSize * BYTES_PER_SAMPLE];
        for (int i = 0; i < bufferSize; i++) {
            short s = (short) (Math.max(-1.0, Math.min(1.0, samples[i])) * 32767);
            pcm[i * 2]     = (byte) (s & 0xFF);
            pcm[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return pcm;
    }

    private void play(byte[] pcm) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) return;

        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        line.write(pcm, 0, pcm.length);
        line.drain();
        line.close();
    }
}
