package com.coraldrift.graphics;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorative fish swimming in the background.
 */
public class FishSwarm {
    
    private final List<Fish> fishes = new ArrayList<>();
    
    private static final Color[] FISH_COLORS = {
        Color.web("#ff9a9e"), // Pink
        Color.web("#a8edea"), // Mint
        Color.web("#ffd89b"), // Gold
        Color.web("#c3cfe2"), // Silver
        Color.web("#fbc2eb"), // Light pink
        Color.web("#89f7fe"), // Cyan
    };
    
    public FishSwarm() {
        // Spawn initial fish
        for (int i = 0; i < 12; i++) {
            spawnFish(MathUtil.randomRange(0, Constants.WINDOW_WIDTH));
        }
    }
    
    private void spawnFish(double x) {
        Fish f = new Fish();
        f.x = x;
        f.y = MathUtil.randomRange(100, Constants.GROUND_Y - 100);
        f.size = MathUtil.randomRange(12, 28);
        f.speed = MathUtil.randomRange(40, 120);
        f.color = MathUtil.randomElement(FISH_COLORS);
        f.tailPhase = MathUtil.randomRange(0, Math.PI * 2);
        f.depth = MathUtil.randomRange(0.3, 0.8); // For parallax effect
        f.goingRight = MathUtil.randomChance(0.5);
        fishes.add(f);
    }
    
    public void update(double deltaTime, double scrollSpeed) {
        for (Fish f : fishes) {
            // Move fish
            double moveDir = f.goingRight ? 1 : -1;
            f.x += moveDir * f.speed * deltaTime;
            f.x -= scrollSpeed * f.depth * deltaTime; // Parallax with world
            
            // Animate tail
            f.tailPhase += deltaTime * 8;
            
            // Gentle bobbing
            f.bobPhase += deltaTime * 2;
        }
        
        // Respawn off-screen fish
        fishes.removeIf(f -> f.x < -50 || f.x > Constants.WINDOW_WIDTH + 50);
        while (fishes.size() < 12) {
            Fish f = new Fish();
            f.goingRight = MathUtil.randomChance(0.5);
            f.x = f.goingRight ? -40 : Constants.WINDOW_WIDTH + 40;
            f.y = MathUtil.randomRange(100, Constants.GROUND_Y - 100);
            f.size = MathUtil.randomRange(12, 28);
            f.speed = MathUtil.randomRange(40, 120);
            f.color = MathUtil.randomElement(FISH_COLORS);
            f.tailPhase = MathUtil.randomRange(0, Math.PI * 2);
            f.depth = MathUtil.randomRange(0.3, 0.8);
            fishes.add(f);
        }
    }
    
    public void render(GraphicsContext gc) {
        for (Fish f : fishes) {
            renderFish(gc, f);
        }
    }
    
    private void renderFish(GraphicsContext gc, Fish f) {
        gc.save();
        gc.setGlobalAlpha(0.6 + f.depth * 0.3);
        
        double x = f.x;
        double y = f.y + Math.sin(f.bobPhase) * 5;
        double size = f.size;
        
        // Flip if going left
        double scaleX = f.goingRight ? 1 : -1;
        gc.translate(x, y);
        gc.scale(scaleX, 1);
        
        // Body gradient
        RadialGradient bodyGrad = new RadialGradient(
            0, 0, 0.3, 0.4, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, f.color.brighter()),
            new Stop(1, f.color.darker())
        );
        
        // Body (oval)
        gc.setFill(bodyGrad);
        gc.fillOval(-size/2, -size/3, size, size * 0.6);
        
        // Tail (wagging triangle)
        double tailWag = Math.sin(f.tailPhase) * 8;
        gc.setFill(f.color);
        gc.beginPath();
        gc.moveTo(-size/2, 0);
        gc.lineTo(-size/2 - size * 0.4, -size * 0.25 + tailWag);
        gc.lineTo(-size/2 - size * 0.4, size * 0.25 + tailWag);
        gc.closePath();
        gc.fill();
        
        // Eye
        gc.setFill(Color.WHITE);
        gc.fillOval(size * 0.15, -size * 0.12, size * 0.18, size * 0.18);
        gc.setFill(Color.BLACK);
        gc.fillOval(size * 0.2, -size * 0.08, size * 0.1, size * 0.1);
        
        // Fin
        gc.setFill(f.color.deriveColor(0, 1, 0.9, 0.7));
        gc.fillOval(-size * 0.1, -size * 0.4, size * 0.3, size * 0.25);
        
        gc.restore();
    }
    
    public void reset() {
        fishes.clear();
        for (int i = 0; i < 12; i++) {
            spawnFish(MathUtil.randomRange(0, Constants.WINDOW_WIDTH));
        }
    }
    
    private static class Fish {
        double x, y, size, speed;
        Color color;
        double tailPhase, bobPhase;
        double depth;
        boolean goingRight;
    }
}
