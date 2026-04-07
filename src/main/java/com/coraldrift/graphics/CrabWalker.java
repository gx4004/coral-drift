package com.coraldrift.graphics;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Cute crabs walking along the ocean floor.
 */
public class CrabWalker {
    
    private final List<Crab> crabs = new ArrayList<>();
    
    private static final Color[] CRAB_COLORS = {
        Color.web("#ff6b6b"), // Red
        Color.web("#ff8e53"), // Orange
        Color.web("#feca57"), // Yellow
        Color.web("#ff9ff3"), // Pink
    };
    
    public CrabWalker() {
        for (int i = 0; i < 5; i++) {
            spawnCrab(MathUtil.randomRange(0, Constants.WINDOW_WIDTH));
        }
    }
    
    private void spawnCrab(double x) {
        Crab c = new Crab();
        c.x = x;
        c.y = Constants.GROUND_Y + MathUtil.randomRange(5, 25);
        c.size = MathUtil.randomRange(18, 30);
        c.speed = MathUtil.randomRange(20, 50);
        c.color = MathUtil.randomElement(CRAB_COLORS);
        c.legPhase = MathUtil.randomRange(0, Math.PI * 2);
        c.goingRight = MathUtil.randomChance(0.5);
        c.directionTimer = MathUtil.randomRange(2, 6);
        crabs.add(c);
    }
    
    public void update(double deltaTime, double scrollSpeed) {
        for (Crab c : crabs) {
            // Move crab
            double moveDir = c.goingRight ? 1 : -1;
            c.x += moveDir * c.speed * deltaTime;
            c.x -= scrollSpeed * 0.9 * deltaTime; // Move with ground
            
            // Animate legs
            c.legPhase += deltaTime * 12;
            
            // Occasionally change direction
            c.directionTimer -= deltaTime;
            if (c.directionTimer <= 0) {
                c.goingRight = !c.goingRight;
                c.directionTimer = MathUtil.randomRange(2, 6);
            }
        }
        
        // Respawn off-screen crabs
        crabs.removeIf(c -> c.x < -50 || c.x > Constants.WINDOW_WIDTH + 100);
        while (crabs.size() < 5) {
            Crab c = new Crab();
            c.x = Constants.WINDOW_WIDTH + MathUtil.randomRange(50, 200);
            c.y = Constants.GROUND_Y + MathUtil.randomRange(5, 25);
            c.size = MathUtil.randomRange(18, 30);
            c.speed = MathUtil.randomRange(20, 50);
            c.color = MathUtil.randomElement(CRAB_COLORS);
            c.legPhase = MathUtil.randomRange(0, Math.PI * 2);
            c.goingRight = MathUtil.randomChance(0.3);
            c.directionTimer = MathUtil.randomRange(2, 6);
            crabs.add(c);
        }
    }
    
    public void render(GraphicsContext gc) {
        for (Crab c : crabs) {
            renderCrab(gc, c);
        }
    }
    
    private void renderCrab(GraphicsContext gc, Crab c) {
        gc.save();
        
        double x = c.x;
        double y = c.y;
        double size = c.size;
        
        // Legs (4 on each side)
        gc.setStroke(c.color.darker());
        gc.setLineWidth(2);
        for (int i = 0; i < 4; i++) {
            double legAngle = 20 + i * 15;
            double legWave = Math.sin(c.legPhase + i * 0.8) * 5;
            
            // Left legs
            double lx1 = x - size * 0.3;
            double ly1 = y;
            double lx2 = x - size * 0.5 - i * 3 + legWave;
            double ly2 = y + size * 0.3 + i * 2;
            gc.strokeLine(lx1, ly1, lx2, ly2);
            
            // Right legs
            double rx1 = x + size * 0.3;
            double ry1 = y;
            double rx2 = x + size * 0.5 + i * 3 - legWave;
            double ry2 = y + size * 0.3 + i * 2;
            gc.strokeLine(rx1, ry1, rx2, ry2);
        }
        
        // Body
        RadialGradient bodyGrad = new RadialGradient(
            0, 0, 0.4, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, c.color.brighter()),
            new Stop(1, c.color.darker())
        );
        gc.setFill(bodyGrad);
        gc.fillOval(x - size/2, y - size/3, size, size * 0.6);
        
        // Claws
        double clawSize = size * 0.35;
        gc.setFill(c.color);
        
        // Left claw
        double leftClawX = x - size * 0.6;
        double clawWave = Math.sin(c.legPhase * 0.5) * 3;
        gc.fillOval(leftClawX - clawSize/2, y - size * 0.2 + clawWave, clawSize, clawSize * 0.7);
        
        // Right claw  
        double rightClawX = x + size * 0.6;
        gc.fillOval(rightClawX - clawSize/2, y - size * 0.2 - clawWave, clawSize, clawSize * 0.7);
        
        // Eyes on stalks
        gc.setFill(c.color.darker());
        double eyeStalkH = size * 0.25;
        gc.fillRect(x - size * 0.15, y - size * 0.3 - eyeStalkH, 3, eyeStalkH);
        gc.fillRect(x + size * 0.12, y - size * 0.3 - eyeStalkH, 3, eyeStalkH);
        
        // Eye balls
        gc.setFill(Color.WHITE);
        gc.fillOval(x - size * 0.2, y - size * 0.35 - eyeStalkH, size * 0.15, size * 0.15);
        gc.fillOval(x + size * 0.08, y - size * 0.35 - eyeStalkH, size * 0.15, size * 0.15);
        
        // Pupils
        gc.setFill(Color.BLACK);
        double pupilOffset = c.goingRight ? 2 : -2;
        gc.fillOval(x - size * 0.16 + pupilOffset, y - size * 0.32 - eyeStalkH, size * 0.08, size * 0.08);
        gc.fillOval(x + size * 0.11 + pupilOffset, y - size * 0.32 - eyeStalkH, size * 0.08, size * 0.08);
        
        gc.restore();
    }
    
    public void reset() {
        crabs.clear();
        for (int i = 0; i < 5; i++) {
            spawnCrab(MathUtil.randomRange(0, Constants.WINDOW_WIDTH));
        }
    }
    
    private static class Crab {
        double x, y, size, speed;
        Color color;
        double legPhase;
        boolean goingRight;
        double directionTimer;
    }
}
