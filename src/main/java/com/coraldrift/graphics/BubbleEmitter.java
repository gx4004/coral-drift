package com.coraldrift.graphics;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * Ambient bubble emitter for underwater atmosphere.
 * Creates gently rising bubbles with wobble motion.
 */
public class BubbleEmitter {
    
    private final List<Bubble> bubbles;
    private final int maxBubbles;
    
    public BubbleEmitter() {
        this.maxBubbles = Constants.MAX_BUBBLES;
        this.bubbles = new ArrayList<>(maxBubbles);
        
        // Initialize bubbles
        for (int i = 0; i < maxBubbles; i++) {
            bubbles.add(createBubble(true));
        }
    }
    
    private Bubble createBubble(boolean randomY) {
        Bubble b = new Bubble();
        b.x = MathUtil.randomRange(0, Constants.WINDOW_WIDTH);
        b.y = randomY ? MathUtil.randomRange(0, Constants.WINDOW_HEIGHT) : Constants.WINDOW_HEIGHT + 20;
        b.size = MathUtil.randomRange(Constants.BUBBLE_MIN_SIZE, Constants.BUBBLE_MAX_SIZE);
        b.speed = Constants.BUBBLE_SPEED * (0.5 + b.size / Constants.BUBBLE_MAX_SIZE);
        b.wobbleSpeed = MathUtil.randomRange(2, 4);
        b.wobbleAmount = MathUtil.randomRange(10, 25);
        b.wobblePhase = MathUtil.randomRange(0, Math.PI * 2);
        b.alpha = MathUtil.randomRange(0.3, 0.7);
        return b;
    }
    
    /**
     * Update all bubbles.
     */
    public void update(double deltaTime) {
        for (Bubble b : bubbles) {
            // Rise upward
            b.y -= b.speed * deltaTime;
            
            // Wobble side to side
            b.wobblePhase += b.wobbleSpeed * deltaTime;
            b.wobbleX = Math.sin(b.wobblePhase) * b.wobbleAmount;
            
            // Reset if off screen
            if (b.y + b.size < 0) {
                b.x = MathUtil.randomRange(0, Constants.WINDOW_WIDTH);
                b.y = Constants.WINDOW_HEIGHT + MathUtil.randomRange(10, 50);
                b.wobblePhase = MathUtil.randomRange(0, Math.PI * 2);
            }
        }
    }
    
    /**
     * Render all bubbles.
     */
    public void render(GraphicsContext gc) {
        for (Bubble b : bubbles) {
            renderBubble(gc, b);
        }
    }
    
    private void renderBubble(GraphicsContext gc, Bubble b) {
        double x = b.x + b.wobbleX;
        double y = b.y;
        double size = b.size;
        
        gc.save();
        gc.setGlobalAlpha(b.alpha);
        
        // Bubble gradient (lighter at top-left)
        RadialGradient gradient = new RadialGradient(
            0, 0, 0.3, 0.3, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 255, 255, 0.8)),
            new Stop(0.5, Color.rgb(200, 230, 255, 0.4)),
            new Stop(1, Color.rgb(150, 200, 230, 0.1))
        );
        
        gc.setFill(gradient);
        gc.fillOval(x - size/2, y - size/2, size, size);
        
        // Outline
        gc.setStroke(Color.rgb(200, 230, 255, 0.5));
        gc.setLineWidth(0.5);
        gc.strokeOval(x - size/2, y - size/2, size, size);
        
        // Highlight shine
        gc.setFill(Color.rgb(255, 255, 255, 0.9));
        double shineSize = size * 0.25;
        gc.fillOval(x - size * 0.25, y - size * 0.25, shineSize, shineSize);
        
        gc.restore();
    }
    
    /**
     * Spawn extra bubbles at a position (e.g., for effects).
     */
    public void burstAt(double x, double y, int count) {
        for (int i = 0; i < count && bubbles.size() < maxBubbles + 20; i++) {
            Bubble b = new Bubble();
            b.x = x + MathUtil.randomRange(-20, 20);
            b.y = y + MathUtil.randomRange(-10, 10);
            b.size = MathUtil.randomRange(4, 10);
            b.speed = Constants.BUBBLE_SPEED * MathUtil.randomRange(1.5, 3.0);
            b.wobbleSpeed = MathUtil.randomRange(3, 6);
            b.wobbleAmount = MathUtil.randomRange(5, 15);
            b.wobblePhase = MathUtil.randomRange(0, Math.PI * 2);
            b.alpha = MathUtil.randomRange(0.5, 0.9);
            bubbles.add(b);
        }
    }
    
    public void reset() {
        bubbles.clear();
        for (int i = 0; i < maxBubbles; i++) {
            bubbles.add(createBubble(true));
        }
    }
    
    private static class Bubble {
        double x, y;
        double size;
        double speed;
        double wobbleSpeed;
        double wobbleAmount;
        double wobblePhase;
        double wobbleX;
        double alpha;
    }
}
