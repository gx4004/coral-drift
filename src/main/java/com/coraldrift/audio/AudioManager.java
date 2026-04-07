package com.coraldrift.audio;

import com.coraldrift.util.SaveManager;

/**
 * Handles all game audio.
 * Currently provides placeholder hooks for future sound implementation.
 * Audio can be added by placing .wav files in resources and updating the play methods.
 */
public class AudioManager {
    
    private static AudioManager instance;
    private boolean soundEnabled;
    
    // Sound effect placeholders
    // To add real sounds, use JavaFX MediaPlayer:
    // private MediaPlayer jumpSound;
    // private MediaPlayer collectSound;
    // etc.
    
    private AudioManager() {
        soundEnabled = SaveManager.getInstance().isSoundEnabled();
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * Initialize audio system.
     * Load sound files here when implementing real audio.
     */
    public void initialize() {
        // Example of loading a sound file:
        // URL resource = getClass().getResource("/com/coraldrift/sounds/jump.wav");
        // if (resource != null) {
        //     Media media = new Media(resource.toExternalForm());
        //     jumpSound = new MediaPlayer(media);
        //     jumpSound.setVolume(0.5);
        // }
    }
    
    /**
     * Play jump sound effect.
     */
    public void playJump() {
        if (!soundEnabled) return;
        // if (jumpSound != null) {
        //     jumpSound.stop();
        //     jumpSound.play();
        // }
    }
    
    /**
     * Play heart collection sound.
     */
    public void playCollect() {
        if (!soundEnabled) return;
        // Play a satisfying "ding" or sparkle sound
    }
    
    /**
     * Play collision/hit sound.
     */
    public void playHit() {
        if (!soundEnabled) return;
        // Play a soft thud or "bonk"
    }
    
    /**
     * Play UI click sound.
     */
    public void playClick() {
        if (!soundEnabled) return;
        // Play a subtle click
    }
    
    /**
     * Play chain combo sound (pitched based on chain level).
     */
    public void playChainCombo(int chainLevel) {
        if (!soundEnabled) return;
        // Play ascending tones for higher chains
        // Pitch could be: basePitch * (1 + chainLevel * 0.1)
    }
    
    /**
     * Play game over sound.
     */
    public void playGameOver() {
        if (!soundEnabled) return;
        // Play sad trombone or gentle failure sound
    }
    
    /**
     * Start background music.
     */
    public void playMusic() {
        if (!soundEnabled) return;
        // Start looping ambient underwater music
    }
    
    /**
     * Stop background music.
     */
    public void stopMusic() {
        // Stop the music loop
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        SaveManager.getInstance().setSoundEnabled(enabled);
        if (!enabled) {
            stopMusic();
        }
    }
    
    public void toggleSound() {
        setSoundEnabled(!soundEnabled);
    }
    
    /**
     * Cleanup audio resources.
     */
    public void dispose() {
        stopMusic();
        // Dispose all MediaPlayer instances
    }
}
