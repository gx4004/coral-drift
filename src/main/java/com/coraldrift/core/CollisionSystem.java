package com.coraldrift.core;

import com.coraldrift.entity.CoralObstacle;
import com.coraldrift.entity.HeartCollectible;
import com.coraldrift.entity.Player;

import java.util.List;

/**
 * Handles collision detection between game entities.
 */
public class CollisionSystem {
    
    /**
     * Check if player collides with any coral.
     * @return the coral hit, or null if no collision
     */
    public CoralObstacle checkCoralCollision(Player player, List<CoralObstacle> corals) {
        if (player.isDead()) return null;
        
        double[] playerBox = player.getHitbox();
        
        for (CoralObstacle coral : corals) {
            if (!coral.isActive()) continue;
            
            double[] coralBox = coral.getHitbox();
            
            if (boxesIntersect(playerBox, coralBox)) {
                return coral;
            }
        }
        
        return null;
    }
    
    /**
     * Check if player collects any hearts.
     * @return list of collected hearts (may be empty)
     */
    public void checkHeartCollisions(Player player, List<HeartCollectible> hearts, 
                                     HeartCollectCallback callback) {
        if (player.isDead()) return;
        
        double[] playerBox = player.getHitbox();
        
        for (HeartCollectible heart : hearts) {
            if (!heart.isActive() || heart.isCollected()) continue;
            
            double[] heartBox = heart.getHitbox();
            
            if (boxesIntersect(playerBox, heartBox)) {
                callback.onHeartCollected(heart);
            }
        }
    }
    
    /**
     * AABB intersection test.
     */
    private boolean boxesIntersect(double[] a, double[] b) {
        // a and b are [x, y, width, height]
        return a[0] < b[0] + b[2] &&
               a[0] + a[2] > b[0] &&
               a[1] < b[1] + b[3] &&
               a[1] + a[3] > b[1];
    }
    
    /**
     * Callback interface for heart collection.
     */
    public interface HeartCollectCallback {
        void onHeartCollected(HeartCollectible heart);
    }
}
