package com.coraldrift.core;

import com.coraldrift.entity.CoralObstacle;
import com.coraldrift.entity.HeartCollectible;
import com.coraldrift.entity.Player;
import com.coraldrift.util.Constants;

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
     * Detect near-misses: player hitbox just cleared a coral's right edge
     * while being within NEAR_MISS_THRESHOLD px vertically.
     */
    public void checkNearMisses(Player player, List<CoralObstacle> corals, NearMissCallback callback) {
        if (player.isDead()) return;

        double[] playerBox = player.getHitbox();
        double playerLeft  = playerBox[0];
        double playerCY    = playerBox[1] + playerBox[3] / 2;

        for (CoralObstacle coral : corals) {
            if (!coral.isActive() || coral.isNearMissChecked()) continue;

            double[] coralBox  = coral.getHitbox();
            double coralRight  = coralBox[0] + coralBox[2];
            double coralCY     = coralBox[1] + coralBox[3] / 2;

            // Player has just fully passed the coral
            if (playerLeft > coralRight) {
                coral.setNearMissChecked(true);

                // Was it a close call? Check vertical proximity
                boolean verticalClose = Math.abs(playerCY - coralCY)
                    < (playerBox[3] / 2 + coralBox[3] / 2 + Constants.NEAR_MISS_THRESHOLD);

                // Make sure hitboxes did NOT actually intersect (would have been a hit)
                if (verticalClose && !boxesIntersect(playerBox, coralBox)) {
                    coral.triggerNearMissFlash();
                    callback.onNearMiss(coral,
                        playerBox[0] + playerBox[2] / 2,
                        playerBox[1] + playerBox[3] / 2);
                }
            }
        }
    }

    /**
     * Callback interface for heart collection.
     */
    public interface HeartCollectCallback {
        void onHeartCollected(HeartCollectible heart);
    }

    /**
     * Callback interface for near-miss events.
     */
    public interface NearMissCallback {
        void onNearMiss(CoralObstacle coral, double playerCX, double playerCY);
    }
}
