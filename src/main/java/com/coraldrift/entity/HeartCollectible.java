package com.coraldrift.entity;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Glow;
import javafx.scene.paint.*;

/**
 * Heart collectible that the player can pick up for points.
 * Features a glowing pulse animation.
 */
public class HeartCollectible extends Entity {
    
    protected double animTime = 0;
    private double pulseScale = 1.0;
    private boolean collected = false;
    private double collectAnimTime = 0;
    
    // Visual effects
    private final Glow glow;
    
    public HeartCollectible(double x, double y) {
        super(x, y, Constants.HEART_SIZE, Constants.HEART_SIZE);
        glow = new Glow(0.5);
    }
    
    @Override
    public void update(double deltaTime) {
        if (collected) {
            collectAnimTime += deltaTime;
            // Collection animation: scale up and fade
            if (collectAnimTime > 0.3) {
                active = false;
            }
            return;
        }
        
        animTime += deltaTime;
        
        // Gentle floating motion
        y += Math.sin(animTime * 3) * 0.3;
        
        // Pulse animation
        pulseScale = 1.0 + Math.sin(animTime * Constants.HEART_PULSE_SPEED * 2 * Math.PI) 
                          * Constants.HEART_PULSE_AMOUNT;
    }
    
    /**
     * Move heart left (called by game engine during scroll).
     */
    public void scroll(double amount) {
        x -= amount;
    }
    
    @Override
    public void render(GraphicsContext gc) {
        gc.save();
        
        double size = width * pulseScale;
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        // Collection animation
        if (collected) {
            double t = collectAnimTime / 0.3;
            size = width * (1 + t * 0.5);
            gc.setGlobalAlpha(1 - t);
        }
        
        // Draw glow behind heart
        drawGlow(gc, centerX, centerY, size * 1.5);
        
        // Draw heart shape
        drawHeart(gc, centerX, centerY, size);
        
        // Sparkle highlight
        if (!collected) {
            drawSparkle(gc, centerX, centerY, size);
        }
        
        gc.restore();
    }
    
    private void drawGlow(GraphicsContext gc, double cx, double cy, double size) {
        RadialGradient glowGradient = new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 107, 122, 0.5)),
            new Stop(0.5, Color.rgb(255, 71, 87, 0.2)),
            new Stop(1, Color.TRANSPARENT)
        );
        
        gc.setFill(glowGradient);
        gc.fillOval(cx - size/2, cy - size/2, size, size);
    }
    
    private void drawHeart(GraphicsContext gc, double cx, double cy, double size) {
        // Heart gradient
        RadialGradient heartGradient = new RadialGradient(
            0, 0, 0.3, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Constants.HEART_PINK),
            new Stop(0.6, Constants.HEART_RED),
            new Stop(1, Constants.HEART_RED.darker())
        );
        
        gc.setFill(heartGradient);
        
        // Draw heart using bezier curves
        double w = size;
        double h = size;
        double topY = cy - h * 0.35;
        double bottomY = cy + h * 0.45;
        
        gc.beginPath();
        gc.moveTo(cx, bottomY); // Bottom point
        
        // Left curve
        gc.bezierCurveTo(
            cx - w * 0.5, cy + h * 0.1,  // Control point 1
            cx - w * 0.5, topY,           // Control point 2
            cx, topY + h * 0.15           // End at top center dip
        );
        
        // Right curve
        gc.bezierCurveTo(
            cx + w * 0.5, topY,           // Control point 1
            cx + w * 0.5, cy + h * 0.1,   // Control point 2
            cx, bottomY                    // Back to bottom
        );
        
        gc.closePath();
        gc.fill();
        
        // Outline
        gc.setStroke(Constants.HEART_RED.darker());
        gc.setLineWidth(1.5);
        gc.stroke();
        
        // Inner highlight
        gc.setFill(Color.rgb(255, 255, 255, 0.35));
        gc.fillOval(cx - w * 0.2, topY + h * 0.15, w * 0.25, h * 0.2);
    }
    
    private void drawSparkle(GraphicsContext gc, double cx, double cy, double size) {
        // Animated sparkle position
        double sparkleAngle = animTime * 2;
        double sparkleRadius = size * 0.5;
        double sparkleX = cx + Math.cos(sparkleAngle) * sparkleRadius * 0.3;
        double sparkleY = cy + Math.sin(sparkleAngle) * sparkleRadius * 0.2 - size * 0.2;
        
        double sparkleSize = 4 + Math.sin(animTime * 5) * 2;
        
        gc.setFill(Color.WHITE);
        // Draw 4-point star
        gc.beginPath();
        gc.moveTo(sparkleX, sparkleY - sparkleSize);
        gc.lineTo(sparkleX + sparkleSize * 0.3, sparkleY);
        gc.lineTo(sparkleX + sparkleSize, sparkleY);
        gc.lineTo(sparkleX + sparkleSize * 0.3, sparkleY);
        gc.lineTo(sparkleX, sparkleY + sparkleSize);
        gc.lineTo(sparkleX - sparkleSize * 0.3, sparkleY);
        gc.lineTo(sparkleX - sparkleSize, sparkleY);
        gc.lineTo(sparkleX - sparkleSize * 0.3, sparkleY);
        gc.closePath();
        gc.fill();
    }
    
    @Override
    public double[] getHitbox() {
        // Slightly larger hitbox for easier collection
        double scale = 1.2;
        double hitW = width * scale;
        double hitH = height * scale;
        double hitX = x + (width - hitW) / 2;
        double hitY = y + (height - hitH) / 2;
        return new double[] { hitX, hitY, hitW, hitH };
    }
    
    /**
     * Trigger collection animation.
     */
    public void collect() {
        if (!collected) {
            collected = true;
            collectAnimTime = 0;
        }
    }
    
    public boolean isCollected() {
        return collected;
    }
    
    /**
     * Check if heart is off-screen to the left.
     */
    public boolean isOffScreen() {
        return x + width < -50;
    }
    
    @Override
    public void reset() {
        super.reset();
        animTime = 0;
        pulseScale = 1.0;
        collected = false;
        collectAnimTime = 0;
    }
}
