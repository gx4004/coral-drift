package com.coraldrift.graphics;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.paint.*;
import javafx.scene.shape.ArcType;

/**
 * Procedural renderer for the pink octopus protagonist.
 * All visuals are drawn using JavaFX primitives - no external images needed.
 */
public class OctopusRenderer {
    
    // Animation state
    private double animTime = 0;
    private double squashStretch = 1.0;
    private double targetSquashStretch = 1.0;
    private boolean isGlowing = false;
    private double glowIntensity = 0;
    
    // Cached effects
    private final DropShadow shadow;
    private final Glow glow;
    
    public OctopusRenderer() {
        shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setColor(Color.rgb(255, 105, 180, 0.4));
        
        glow = new Glow();
        glow.setLevel(0.3);
    }
    
    /**
     * Update animation state.
     */
    public void update(double deltaTime, boolean isJumping, boolean isOnGround, double velocityY) {
        animTime += deltaTime;
        
        // Calculate squash/stretch based on state
        if (isJumping && velocityY < 0) {
            // Stretching upward during jump
            targetSquashStretch = 1.0 + Constants.STRETCH_AMOUNT;
        } else if (velocityY > 200) {
            // Stretching downward while falling fast
            targetSquashStretch = 1.0 + Constants.STRETCH_AMOUNT * 0.5;
        } else if (isOnGround) {
            // Slight squash when on ground (landing recovery)
            targetSquashStretch = 1.0 - Constants.SQUASH_AMOUNT * 0.3;
        } else {
            targetSquashStretch = 1.0;
        }
        
        // Smoothly interpolate squash/stretch
        squashStretch = MathUtil.lerp(squashStretch, targetSquashStretch, deltaTime * 15);
        
        // Glow decay
        if (isGlowing) {
            glowIntensity = MathUtil.lerp(glowIntensity, 1.0, deltaTime * 5);
        } else {
            glowIntensity = MathUtil.lerp(glowIntensity, 0.0, deltaTime * 3);
        }
    }
    
    /**
     * Trigger landing squash effect.
     */
    public void onLand() {
        squashStretch = 1.0 - Constants.SQUASH_AMOUNT;
    }
    
    /**
     * Trigger jump stretch effect.
     */
    public void onJump() {
        squashStretch = 1.0 + Constants.STRETCH_AMOUNT * 0.5;
    }
    
    /**
     * Set glow state (for chain combo visual).
     */
    public void setGlowing(boolean glowing) {
        this.isGlowing = glowing;
    }
    
    /**
     * Render the octopus at the given position.
     */
    public void render(GraphicsContext gc, double x, double y, double width, double height) {
        gc.save();
        
        // Apply idle bobbing motion
        double bobOffset = Math.sin(animTime * Constants.IDLE_BOB_SPEED * 2 * Math.PI) * Constants.IDLE_BOB_AMOUNT;
        y += bobOffset;
        
        // Center point for transformations
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        // Apply squash and stretch (inverse relationship)
        double scaleX = 1.0 / squashStretch;
        double scaleY = squashStretch;
        
        gc.translate(centerX, centerY);
        gc.scale(scaleX, scaleY);
        gc.translate(-centerX, -centerY);
        
        // Draw glow aura if active
        if (glowIntensity > 0.05) {
            drawGlowAura(gc, x, y, width, height);
        }
        
        // Draw tentacles (behind body)
        drawTentacles(gc, x, y, width, height);
        
        // Draw main body
        drawBody(gc, x, y, width, height);
        
        // Draw face
        drawFace(gc, x, y, width, height);
        
        gc.restore();
    }
    
    private void drawGlowAura(GraphicsContext gc, double x, double y, double w, double h) {
        double auraSize = Math.max(w, h) * 1.5;
        double centerX = x + w / 2;
        double centerY = y + h / 2;
        
        RadialGradient auraGradient = new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 215, 0, 0.3 * glowIntensity)),
            new Stop(0.5, Color.rgb(255, 182, 193, 0.2 * glowIntensity)),
            new Stop(1, Color.TRANSPARENT)
        );
        
        gc.setFill(auraGradient);
        gc.fillOval(centerX - auraSize/2, centerY - auraSize/2, auraSize, auraSize);
    }
    
    private void drawTentacles(GraphicsContext gc, double x, double y, double w, double h) {
        int numTentacles = 8;
        double tentacleLength = h * 0.6;
        double tentacleWidth = w * 0.12;
        double bodyBottom = y + h * 0.55;
        double bodyWidth = w * 0.8;
        
        // Create gradient for tentacles
        LinearGradient tentacleGradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Constants.OCTOPUS_PINK),
            new Stop(1, Constants.OCTOPUS_PINK.darker())
        );
        
        gc.setFill(tentacleGradient);
        gc.setStroke(Constants.OCTOPUS_PINK.darker().darker());
        gc.setLineWidth(1.5);
        
        for (int i = 0; i < numTentacles; i++) {
            // Position tentacles along body bottom
            double t = (double) i / (numTentacles - 1);
            double baseX = x + (w - bodyWidth) / 2 + t * bodyWidth;
            
            // Wave animation for each tentacle
            double phase = animTime * Constants.TENTACLE_WAVE_SPEED + i * 0.8;
            double waveX = Math.sin(phase) * tentacleWidth * 0.8;
            double waveX2 = Math.sin(phase + 1.5) * tentacleWidth * 1.2;
            
            // Draw tentacle as a curved shape
            gc.beginPath();
            gc.moveTo(baseX - tentacleWidth/2, bodyBottom);
            gc.bezierCurveTo(
                baseX - tentacleWidth/2 + waveX, bodyBottom + tentacleLength * 0.4,
                baseX + waveX2, bodyBottom + tentacleLength * 0.7,
                baseX + waveX * 0.5, bodyBottom + tentacleLength
            );
            gc.bezierCurveTo(
                baseX + tentacleWidth/2 + waveX2 * 0.5, bodyBottom + tentacleLength * 0.7,
                baseX + tentacleWidth/2 + waveX, bodyBottom + tentacleLength * 0.4,
                baseX + tentacleWidth/2, bodyBottom
            );
            gc.closePath();
            gc.fill();
            gc.stroke();
            
            // Sucker dots
            double suckerY1 = bodyBottom + tentacleLength * 0.3;
            double suckerY2 = bodyBottom + tentacleLength * 0.55;
            gc.setFill(Constants.OCTOPUS_PINK_SOFT);
            gc.fillOval(baseX + waveX * 0.3 - 2, suckerY1 - 2, 4, 4);
            gc.fillOval(baseX + waveX2 * 0.4 - 2, suckerY2 - 2, 3, 3);
        }
    }
    
    private void drawBody(GraphicsContext gc, double x, double y, double w, double h) {
        double bodyW = w * 0.85;
        double bodyH = h * 0.65;
        double bodyX = x + (w - bodyW) / 2;
        double bodyY = y;
        
        // Outer glow/halo for premium look
        RadialGradient outerGlow = new RadialGradient(
            0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.TRANSPARENT),
            new Stop(0.7, Color.TRANSPARENT),
            new Stop(1, Color.rgb(255, 182, 217, 0.25))
        );
        gc.setFill(outerGlow);
        gc.fillOval(bodyX - 8, bodyY - 5, bodyW + 16, bodyH + 10);
        
        // Main body gradient - richer colors
        RadialGradient bodyGradient = new RadialGradient(
            0, 0, 0.3, 0.25, 0.85, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 200, 230)),  // Bright highlight
            new Stop(0.3, Constants.OCTOPUS_PINK_LIGHT),
            new Stop(0.7, Constants.OCTOPUS_PINK),
            new Stop(1, Color.rgb(200, 80, 140))    // Deeper shadow
        );
        
        // Draw soft shadow
        gc.setFill(Color.rgb(80, 40, 80, 0.2));
        gc.fillOval(bodyX + 4, bodyY + 5, bodyW, bodyH);
        
        // Main body oval
        gc.setFill(bodyGradient);
        gc.fillOval(bodyX, bodyY, bodyW, bodyH);
        
        // Body outline - subtle
        gc.setStroke(Constants.OCTOPUS_PINK.darker());
        gc.setLineWidth(2);
        gc.strokeOval(bodyX, bodyY, bodyW, bodyH);
        
        // Highlight shine
        gc.setFill(Color.rgb(255, 255, 255, 0.3));
        gc.fillOval(bodyX + bodyW * 0.15, bodyY + bodyH * 0.1, bodyW * 0.3, bodyH * 0.25);
    }
    
    private void drawFace(GraphicsContext gc, double x, double y, double w, double h) {
        double bodyW = w * 0.85;
        double bodyH = h * 0.65;
        double bodyX = x + (w - bodyW) / 2;
        double bodyCenterX = bodyX + bodyW / 2;
        double bodyCenterY = y + bodyH * 0.45;
        
        // Eye parameters
        double eyeSpacing = bodyW * 0.25;
        double eyeSize = bodyW * 0.22;
        double pupilSize = eyeSize * 0.5;
        
        // Left eye
        drawEye(gc, bodyCenterX - eyeSpacing, bodyCenterY, eyeSize, pupilSize);
        // Right eye
        drawEye(gc, bodyCenterX + eyeSpacing, bodyCenterY, eyeSize, pupilSize);
        
        // Cute blush cheeks
        gc.setFill(Constants.OCTOPUS_CHEEK);
        double cheekSize = bodyW * 0.12;
        gc.fillOval(bodyCenterX - eyeSpacing * 1.6 - cheekSize/2, bodyCenterY + eyeSize * 0.4, cheekSize, cheekSize * 0.6);
        gc.fillOval(bodyCenterX + eyeSpacing * 1.6 - cheekSize/2, bodyCenterY + eyeSize * 0.4, cheekSize, cheekSize * 0.6);
        
        // Small happy mouth
        double mouthY = bodyCenterY + eyeSize * 0.8;
        gc.setStroke(Constants.OCTOPUS_PINK.darker().darker());
        gc.setLineWidth(2.5);
        gc.strokeArc(bodyCenterX - 8, mouthY - 4, 16, 10, 200, 140, ArcType.OPEN);
    }
    
    private void drawEye(GraphicsContext gc, double cx, double cy, double size, double pupilSize) {
        // Eye white
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - size/2, cy - size/2, size, size);
        gc.setStroke(Color.rgb(80, 80, 80, 0.5));
        gc.setLineWidth(1);
        gc.strokeOval(cx - size/2, cy - size/2, size, size);
        
        // Pupil with subtle animation
        double lookOffset = Math.sin(animTime * 0.5) * 2;
        gc.setFill(Color.rgb(30, 30, 50));
        gc.fillOval(cx - pupilSize/2 + lookOffset, cy - pupilSize/2, pupilSize, pupilSize);
        
        // Eye shine
        gc.setFill(Color.WHITE);
        double shineSize = pupilSize * 0.4;
        gc.fillOval(cx - pupilSize/4 + lookOffset, cy - pupilSize/3, shineSize, shineSize);
    }
    
    /**
     * Reset animation state.
     */
    public void reset() {
        animTime = 0;
        squashStretch = 1.0;
        targetSquashStretch = 1.0;
        isGlowing = false;
        glowIntensity = 0;
    }
}
