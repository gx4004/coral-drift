package com.coraldrift.entity;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;

/**
 * Shield bubble power-up. Rare collectible that gives temporary invincibility.
 */
public class ShieldBubble extends Entity {
    
    private double animTime = 0;
    private double wobblePhase;
    private boolean collected = false;
    
    public static final double SIZE = 45;
    public static final double SHIELD_DURATION = 4.0; // seconds
    
    public ShieldBubble(double x, double y) {
        super(x, y, SIZE, SIZE);
        this.wobblePhase = MathUtil.randomRange(0, Math.PI * 2);
    }
    
    @Override
    public void update(double deltaTime) {
        animTime += deltaTime;
    }
    
    public void scroll(double amount) {
        x -= amount;
    }
    
    @Override
    public void render(GraphicsContext gc) {
        if (collected) return;
        
        gc.save();
        
        double cx = x + width / 2;
        double cy = y + height / 2;
        
        // Wobble animation
        double wobble = Math.sin(animTime * 3 + wobblePhase) * 3;
        double pulse = 1.0 + Math.sin(animTime * 4) * 0.08;
        double r = (SIZE / 2) * pulse;
        
        // Outer glow
        RadialGradient outerGlow = new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.TRANSPARENT),
            new Stop(0.6, Color.TRANSPARENT),
            new Stop(1, Color.rgb(100, 200, 255, 0.3))
        );
        gc.setFill(outerGlow);
        gc.fillOval(cx - r * 1.5, cy - r * 1.5 + wobble, r * 3, r * 3);
        
        // Main bubble
        RadialGradient bubbleGrad = new RadialGradient(
            0, 0, 0.3, 0.3, 0.8, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(200, 240, 255, 0.9)),
            new Stop(0.5, Color.rgb(100, 200, 255, 0.5)),
            new Stop(0.8, Color.rgb(60, 160, 220, 0.3)),
            new Stop(1, Color.rgb(40, 140, 200, 0.2))
        );
        gc.setFill(bubbleGrad);
        gc.fillOval(cx - r, cy - r + wobble, r * 2, r * 2);
        
        // Inner highlight (shine)
        gc.setFill(Color.rgb(255, 255, 255, 0.7));
        gc.fillOval(cx - r * 0.5, cy - r * 0.6 + wobble, r * 0.5, r * 0.35);
        
        // Small shine dot
        gc.setFill(Color.rgb(255, 255, 255, 0.9));
        gc.fillOval(cx - r * 0.3, cy - r * 0.4 + wobble, r * 0.2, r * 0.2);
        
        // Rainbow shimmer effect
        double shimmerAngle = animTime * 2;
        gc.setStroke(Color.hsb((shimmerAngle * 50) % 360, 0.5, 1, 0.4));
        gc.setLineWidth(2);
        gc.strokeOval(cx - r + 2, cy - r + 2 + wobble, r * 2 - 4, r * 2 - 4);
        
        // Shield icon inside (small shield shape)
        gc.setFill(Color.rgb(255, 255, 255, 0.6));
        double iconSize = r * 0.5;
        gc.beginPath();
        gc.moveTo(cx, cy - iconSize * 0.8 + wobble);
        gc.lineTo(cx + iconSize * 0.6, cy - iconSize * 0.3 + wobble);
        gc.lineTo(cx + iconSize * 0.5, cy + iconSize * 0.5 + wobble);
        gc.lineTo(cx, cy + iconSize * 0.8 + wobble);
        gc.lineTo(cx - iconSize * 0.5, cy + iconSize * 0.5 + wobble);
        gc.lineTo(cx - iconSize * 0.6, cy - iconSize * 0.3 + wobble);
        gc.closePath();
        gc.fill();
        
        gc.restore();
    }
    
    @Override
    public double[] getHitbox() {
        double scale = 0.7;
        double hitW = width * scale;
        double hitH = height * scale;
        double hitX = x + (width - hitW) / 2;
        double hitY = y + (height - hitH) / 2;
        return new double[] { hitX, hitY, hitW, hitH };
    }
    
    public boolean isOffScreen() {
        return x + width < -20;
    }
    
    public void collect() {
        collected = true;
    }
    
    public boolean isCollected() {
        return collected;
    }
}
