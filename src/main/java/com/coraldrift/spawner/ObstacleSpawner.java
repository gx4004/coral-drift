package com.coraldrift.spawner;

import com.coraldrift.entity.CoralObstacle;
import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Spawns coral obstacles with increasing difficulty.
 */
public class ObstacleSpawner {
    
    private final List<CoralObstacle> corals;
    private double spawnX;
    private double nextSpawnDistance;
    private double currentGapMin;
    private double currentGapMax;
    
    public ObstacleSpawner() {
        corals = new ArrayList<>();
        reset();
    }
    
    /**
     * Update spawner - spawn new corals, remove off-screen ones.
     */
    public void update(double scrollAmount, double difficultyFactor) {
        // Move all corals
        for (CoralObstacle coral : corals) {
            coral.scroll(scrollAmount);
        }
        
        // Remove off-screen corals
        corals.removeIf(CoralObstacle::isOffScreen);
        
        // Advance spawn position
        spawnX -= scrollAmount;
        
        // Spawn new coral when needed
        if (spawnX <= Constants.WINDOW_WIDTH + 100) {
            spawnCoral(difficultyFactor);
            
            // Update gaps based on difficulty
            updateDifficultyGaps(difficultyFactor);
            
            // Calculate next spawn distance
            nextSpawnDistance = MathUtil.randomRange(currentGapMin, currentGapMax);
            spawnX += nextSpawnDistance;
        }
    }
    
    private void spawnCoral(double difficultyFactor) {
        CoralObstacle.CoralType type = CoralObstacle.randomType(difficultyFactor);
        CoralObstacle coral = new CoralObstacle(spawnX, type);
        corals.add(coral);
    }
    
    private void updateDifficultyGaps(double difficultyFactor) {
        // Reduce gaps as difficulty increases
        double reduction = Math.pow(Constants.GAP_REDUCTION_RATE, difficultyFactor * 50);
        
        currentGapMin = Math.max(
            Constants.MIN_OBSTACLE_GAP * reduction,
            Constants.MIN_GAP_FLOOR
        );
        
        currentGapMax = Math.max(
            Constants.MAX_OBSTACLE_GAP * reduction,
            currentGapMin + 100
        );
    }
    
    /**
     * Get the most recently spawned coral (for heart placement).
     */
    public CoralObstacle getLastSpawnedCoral() {
        if (corals.isEmpty()) return null;
        return corals.get(corals.size() - 1);
    }
    
    /**
     * Get the second-to-last spawned coral (for heart placement between obstacles).
     */
    public CoralObstacle getPreviousCoral() {
        if (corals.size() < 2) return null;
        return corals.get(corals.size() - 2);
    }
    
    public List<CoralObstacle> getCorals() {
        return corals;
    }
    
    public void reset() {
        corals.clear();
        spawnX = Constants.WINDOW_WIDTH + 300; // Initial spawn position
        currentGapMin = Constants.MIN_OBSTACLE_GAP;
        currentGapMax = Constants.MAX_OBSTACLE_GAP;
        nextSpawnDistance = MathUtil.randomRange(currentGapMin, currentGapMax);
    }
}
