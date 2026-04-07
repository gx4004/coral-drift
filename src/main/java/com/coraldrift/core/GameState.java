package com.coraldrift.core;

/**
 * Holds the current state of the game.
 */
public class GameState {
    
    public enum Status {
        MENU,
        PLAYING,
        PAUSED,
        GAME_OVER
    }
    
    private Status status = Status.MENU;
    
    // Score tracking
    private double score = 0;
    private int displayScore = 0;
    private int heartsCollected = 0;
    private int bestScore = 0;
    private int bestHearts = 0;
    
    // Harmony Chain system
    private int chainCount = 0;
    private int maxChainThisRun = 0;
    private double chainTimer = 0;
    private static final double CHAIN_TIMEOUT = 3.0; // Seconds to maintain chain
    
    // Timing
    private double elapsedTime = 0;
    private double scrollSpeed;
    private double difficultyFactor = 0;
    
    // Flags
    private boolean newBestScore = false;
    private boolean newBestHearts = false;
    
    public GameState(double initialScrollSpeed) {
        this.scrollSpeed = initialScrollSpeed;
    }
    
    /**
     * Update game state each frame.
     */
    public void update(double deltaTime) {
        if (status != Status.PLAYING) return;
        
        elapsedTime += deltaTime;
        
        // Smooth score display
        if (displayScore < (int) score) {
            displayScore = Math.min(displayScore + 1, (int) score);
        }
        
        // Chain timeout
        if (chainCount > 0) {
            chainTimer -= deltaTime;
            if (chainTimer <= 0) {
                breakChain();
            }
        }
    }
    
    /**
     * Add points from surviving (distance).
     */
    public void addDistanceScore(double deltaTime, double scorePerSecond) {
        score += scorePerSecond * deltaTime;
    }
    
    /**
     * Collect a heart and update chain.
     */
    public void collectHeart(int baseValue) {
        heartsCollected++;
        chainCount++;
        chainTimer = CHAIN_TIMEOUT;
        
        if (chainCount > maxChainThisRun) {
            maxChainThisRun = chainCount;
        }
        
        // Calculate chain bonus
        double multiplier = 1.0 + (chainCount - 1) * 0.5; // 1x, 1.5x, 2x, 2.5x...
        multiplier = Math.min(multiplier, 5.0); // Cap at 5x
        
        int points = (int) (baseValue * multiplier);
        score += points;
    }
    
    /**
     * Break the chain (missed heart or hit obstacle).
     */
    public void breakChain() {
        chainCount = 0;
        chainTimer = 0;
    }
    
    /**
     * Get current chain multiplier.
     */
    public double getChainMultiplier() {
        if (chainCount <= 0) return 1.0;
        return 1.0 + (chainCount - 1) * 0.5;
    }
    
    /**
     * Check if chain is active (for visual effects).
     */
    public boolean isChainActive() {
        return chainCount >= 2;
    }
    
    /**
     * Get chain timer progress (0-1).
     */
    public double getChainProgress() {
        return chainTimer / CHAIN_TIMEOUT;
    }
    
    /**
     * Update scroll speed based on difficulty.
     */
    public void updateScrollSpeed(double baseSpeed, double maxSpeed, double increment, double interval) {
        int increments = (int) (elapsedTime / interval);
        scrollSpeed = Math.min(baseSpeed + increments * increment, maxSpeed);
        
        // Difficulty factor 0-1
        difficultyFactor = Math.min(elapsedTime / 120.0, 1.0); // Max difficulty at 2 minutes
    }
    
    /**
     * Set best scores from save data.
     */
    public void setBestScores(int bestScore, int bestHearts) {
        this.bestScore = bestScore;
        this.bestHearts = bestHearts;
    }
    
    /**
     * Finalize score and check for new bests.
     */
    public void finalizeScore() {
        newBestScore = (int) score > bestScore;
        newBestHearts = heartsCollected > bestHearts;
        
        if (newBestScore) {
            bestScore = (int) score;
        }
        if (newBestHearts) {
            bestHearts = heartsCollected;
        }
    }
    
    /**
     * Reset state for new game.
     */
    public void reset(double initialScrollSpeed) {
        status = Status.PLAYING;
        score = 0;
        displayScore = 0;
        heartsCollected = 0;
        chainCount = 0;
        maxChainThisRun = 0;
        chainTimer = 0;
        elapsedTime = 0;
        scrollSpeed = initialScrollSpeed;
        difficultyFactor = 0;
        newBestScore = false;
        newBestHearts = false;
    }
    
    // Getters and setters
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public int getScore() { return (int) score; }
    public int getDisplayScore() { return displayScore; }
    public int getHeartsCollected() { return heartsCollected; }
    public int getBestScore() { return bestScore; }
    public int getBestHearts() { return bestHearts; }
    
    public int getChainCount() { return chainCount; }
    public int getMaxChainThisRun() { return maxChainThisRun; }
    
    public double getElapsedTime() { return elapsedTime; }
    public double getScrollSpeed() { return scrollSpeed; }
    public double getDifficultyFactor() { return difficultyFactor; }
    
    public boolean isNewBestScore() { return newBestScore; }
    public boolean isNewBestHearts() { return newBestHearts; }
    
    public boolean isPlaying() { return status == Status.PLAYING; }
    public boolean isPaused() { return status == Status.PAUSED; }
    public boolean isGameOver() { return status == Status.GAME_OVER; }
}
