package com.coraldrift.spawner;

import com.coraldrift.entity.CoralObstacle;
import com.coraldrift.entity.HeartCollectible;
import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Spawns heart collectibles in strategic positions.
 */
public class HeartSpawner {
    
    private final List<HeartCollectible> hearts;
    private CoralObstacle lastProcessedCoral;
    
    public HeartSpawner() {
        hearts = new ArrayList<>();
    }
    
    /**
     * Update spawner - move hearts, remove off-screen ones.
     */
    public void update(double scrollAmount, ObstacleSpawner obstacleSpawner, double difficultyFactor) {
        // Move all hearts
        for (HeartCollectible heart : hearts) {
            heart.scroll(scrollAmount);
            heart.update(Constants.FRAME_TIME);
        }
        
        // Remove off-screen or collected hearts
        hearts.removeIf(h -> h.isOffScreen() || !h.isActive());
        
        // Check if we should spawn a heart with the latest coral
        CoralObstacle latestCoral = obstacleSpawner.getLastSpawnedCoral();
        if (latestCoral != null && latestCoral != lastProcessedCoral) {
            lastProcessedCoral = latestCoral;
            
            // Chance to spawn a heart
            if (MathUtil.randomChance(Constants.HEART_SPAWN_CHANCE)) {
                spawnHeart(latestCoral, obstacleSpawner.getPreviousCoral(), difficultyFactor);
            }
        }
    }
    
    private void spawnHeart(CoralObstacle nearCoral, CoralObstacle prevCoral, double difficultyFactor) {
        double x, y;
        
        // Position heart between corals or above current coral
        if (prevCoral != null && MathUtil.randomChance(0.6)) {
            // Between two corals
            double midX = (prevCoral.getX() + nearCoral.getX()) / 2;
            x = midX + MathUtil.randomRange(-30, 30);
        } else {
            // Near the coral but ahead of it
            x = nearCoral.getX() + MathUtil.randomRange(-50, 100);
        }
        
        // Height based on jump arc - ALL hearts must be reachable!
        // Player jumps to roughly y = GROUND_Y - 250 at peak, so cap hearts there
        double maxReachableHeight = 240.0; // Maximum height player can reach
        double heartHeightRange = Math.min(Constants.HEART_MAX_HEIGHT, maxReachableHeight) - Constants.HEART_MIN_HEIGHT;
        
        // Most hearts spawn in the easy-to-reach zone (lower 70%)
        double heightFactor;
        if (MathUtil.randomChance(0.7)) {
            // Easy hearts - lower portion of range
            heightFactor = MathUtil.randomRange(0.0, 0.5);
        } else {
            // Challenge hearts - but still reachable
            heightFactor = MathUtil.randomRange(0.5, 0.85);
        }
        
        y = Constants.GROUND_Y - Constants.HEART_MIN_HEIGHT - heightFactor * heartHeightRange;
        
        // Clamp to ensure always reachable
        double minY = Constants.GROUND_Y - maxReachableHeight;
        y = Math.max(y, minY);
        
        hearts.add(new HeartCollectible(x, y));
    }
    
    public List<HeartCollectible> getHearts() {
        return hearts;
    }
    
    public void reset() {
        hearts.clear();
        lastProcessedCoral = null;
    }
}
