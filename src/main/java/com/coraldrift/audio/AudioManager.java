package com.coraldrift.audio;

import com.coraldrift.audio.ToneGenerator.ToneDesc;
import com.coraldrift.audio.ToneGenerator.WaveShape;
import com.coraldrift.util.SaveManager;

/**
 * Manages all game audio using synthesized tones — no audio files required.
 * All sounds are described by ToneDesc objects and played via ToneGenerator.
 */
public class AudioManager {

    private static AudioManager instance;
    private boolean soundEnabled;
    private boolean initialized = false;
    private ToneGenerator generator;

    // ─── Pre-defined tone descriptors ─────────────────────────────────────────

    private static final ToneDesc JUMP_DESC = new ToneDesc(
        WaveShape.SINE, new double[]{320, 520}, 0.12, 0.005, 0.08, 0.55);

    private static final ToneDesc COLLECT_DESC = new ToneDesc(
        WaveShape.SINE, new double[]{660, 880, 1100}, 0.18, 0.008, 0.12, 0.6);

    private static final ToneDesc GOLDEN_COLLECT_DESC = new ToneDesc(
        WaveShape.SINE, new double[]{880, 1100, 1320, 880}, 0.28, 0.01, 0.18, 0.7, true);

    private static final ToneDesc HIT_DESC = new ToneDesc(
        WaveShape.SAWTOOTH, new double[]{200, 80}, 0.30, 0.002, 0.25, 0.65);

    private static final ToneDesc GAME_OVER_DESC = new ToneDesc(
        WaveShape.TRIANGLE, new double[]{300, 240, 180, 150}, 0.80, 0.01, 0.60, 0.55);

    private static final ToneDesc SHIELD_DESC = new ToneDesc(
        WaveShape.SINE, new double[]{1400, 900}, 0.15, 0.003, 0.12, 0.5);

    private static final ToneDesc NEAR_MISS_DESC = new ToneDesc(
        WaveShape.SQUARE, new double[]{440, 550}, 0.10, 0.002, 0.07, 0.4);

    private static final ToneDesc CLICK_DESC = new ToneDesc(
        WaveShape.SINE, new double[]{800, 700}, 0.06, 0.002, 0.04, 0.4);

    // ─── Singleton ────────────────────────────────────────────────────────────

    private AudioManager() {
        soundEnabled = SaveManager.getInstance().isSoundEnabled();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    public void initialize() {
        try {
            generator = new ToneGenerator();
            initialized = true;
        } catch (Exception e) {
            // Audio system unavailable — gracefully continue without sound
            initialized = false;
        }
    }

    public void dispose() {
        if (generator != null) {
            generator.dispose();
        }
    }

    // ─── Sound effects ────────────────────────────────────────────────────────

    public void playJump() {
        play(JUMP_DESC);
    }

    public void playCollect() {
        play(COLLECT_DESC);
    }

    public void playGoldenCollect() {
        play(GOLDEN_COLLECT_DESC);
    }

    public void playHit() {
        play(HIT_DESC);
    }

    public void playGameOver() {
        play(GAME_OVER_DESC);
    }

    public void playShieldAbsorb() {
        play(SHIELD_DESC);
    }

    public void playNearMiss() {
        play(NEAR_MISS_DESC);
    }

    public void playClick() {
        play(CLICK_DESC);
    }

    /**
     * Ascending pitch per chain level — makes collecting hearts in a streak
     * feel increasingly exciting.
     */
    public void playChainCombo(int chainLevel) {
        if (!soundEnabled || !initialized) return;
        double baseFreq = 440.0 * (1.0 + Math.min(chainLevel, 10) * 0.12);
        ToneDesc chainDesc = new ToneDesc(
            WaveShape.SINE,
            new double[]{baseFreq, baseFreq * 1.25},
            0.15, 0.008, 0.10, 0.5
        );
        generator.playAsync(chainDesc);
    }

    // ─── Settings ─────────────────────────────────────────────────────────────

    public boolean isSoundEnabled() { return soundEnabled; }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        SaveManager.getInstance().setSoundEnabled(enabled);
    }

    public void toggleSound() {
        setSoundEnabled(!soundEnabled);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void play(ToneDesc desc) {
        if (!soundEnabled || !initialized || generator == null) return;
        generator.playAsync(desc);
    }

    // Stubs kept for compatibility
    public void playMusic() {}
    public void stopMusic() {}
}
