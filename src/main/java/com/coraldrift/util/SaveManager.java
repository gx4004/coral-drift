package com.coraldrift.util;

import java.util.prefs.Preferences;

/**
 * Handles saving and loading game data using Java Preferences API.
 * Data persists between game sessions.
 */
public class SaveManager {
    
    private static SaveManager instance;
    private final Preferences prefs;
    
    private int bestScore;
    private int bestHearts;
    private boolean soundEnabled;
    
    private SaveManager() {
        prefs = Preferences.userNodeForPackage(SaveManager.class);
        load();
    }
    
    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }
    
    /**
     * Load saved data from preferences.
     */
    private void load() {
        bestScore = prefs.getInt(Constants.SAVE_BEST_SCORE, 0);
        bestHearts = prefs.getInt(Constants.SAVE_BEST_HEARTS, 0);
        soundEnabled = prefs.getBoolean(Constants.SAVE_SOUND_ENABLED, true);
    }
    
    /**
     * Save current data to preferences.
     */
    public void save() {
        prefs.putInt(Constants.SAVE_BEST_SCORE, bestScore);
        prefs.putInt(Constants.SAVE_BEST_HEARTS, bestHearts);
        prefs.putBoolean(Constants.SAVE_SOUND_ENABLED, soundEnabled);
    }
    
    /**
     * Update best score if current is higher.
     * @return true if this was a new best score
     */
    public boolean updateBestScore(int score) {
        if (score > bestScore) {
            bestScore = score;
            save();
            return true;
        }
        return false;
    }
    
    /**
     * Update best hearts if current is higher.
     * @return true if this was a new best hearts count
     */
    public boolean updateBestHearts(int hearts) {
        if (hearts > bestHearts) {
            bestHearts = hearts;
            save();
            return true;
        }
        return false;
    }
    
    public int getBestScore() {
        return bestScore;
    }
    
    public int getBestHearts() {
        return bestHearts;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        save();
    }
    
    public void toggleSound() {
        setSoundEnabled(!soundEnabled);
    }
    
    /**
     * Reset all saved data (for testing).
     */
    public void resetAll() {
        bestScore = 0;
        bestHearts = 0;
        soundEnabled = true;
        save();
    }
}
