package com.coraldrift.graphics;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Screen-wide visual effects: shake, flash, etc.
 */
public class ScreenEffects {
    
    // Screen shake
    private double shakeIntensity = 0;
    private double shakeDuration = 0;
    private double shakeTimer = 0;
    private double shakeOffsetX = 0;
    private double shakeOffsetY = 0;
    
    // Flash effect
    private double flashAlpha = 0;
    private Color flashColor = Color.WHITE;
    private double flashDuration = 0;
    private double flashTimer = 0;
    
    // Pulse effect (for impacts)
    private double pulseScale = 1.0;
    private double pulseTarget = 1.0;
    
    /**
     * Update all screen effects.
     */
    public void update(double deltaTime) {
        updateShake(deltaTime);
        updateFlash(deltaTime);
        updatePulse(deltaTime);
    }
    
    private void updateShake(double deltaTime) {
        if (shakeTimer > 0) {
            shakeTimer -= deltaTime;
            double progress = shakeTimer / shakeDuration;
            double currentIntensity = shakeIntensity * progress;
            
            shakeOffsetX = MathUtil.randomRange(-currentIntensity, currentIntensity);
            shakeOffsetY = MathUtil.randomRange(-currentIntensity, currentIntensity);
        } else {
            shakeOffsetX = 0;
            shakeOffsetY = 0;
        }
    }
    
    private void updateFlash(double deltaTime) {
        if (flashTimer > 0) {
            flashTimer -= deltaTime;
            flashAlpha = flashTimer / flashDuration;
        } else {
            flashAlpha = 0;
        }
    }
    
    private void updatePulse(double deltaTime) {
        pulseScale = MathUtil.lerp(pulseScale, pulseTarget, deltaTime * 10);
        if (Math.abs(pulseScale - pulseTarget) < 0.001) {
            pulseTarget = 1.0;
        }
    }
    
    /**
     * Apply pre-render transformations (call before rendering game).
     */
    public void applyPreRender(GraphicsContext gc) {
        if (shakeOffsetX != 0 || shakeOffsetY != 0) {
            gc.translate(shakeOffsetX, shakeOffsetY);
        }
        
        if (pulseScale != 1.0) {
            double centerX = Constants.WINDOW_WIDTH / 2;
            double centerY = Constants.WINDOW_HEIGHT / 2;
            gc.translate(centerX, centerY);
            gc.scale(pulseScale, pulseScale);
            gc.translate(-centerX, -centerY);
        }
    }
    
    /**
     * Render overlay effects (call after rendering game).
     */
    public void renderOverlay(GraphicsContext gc) {
        if (flashAlpha > 0.01) {
            gc.save();
            gc.setGlobalAlpha(flashAlpha * 0.5);
            gc.setFill(flashColor);
            gc.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            gc.restore();
        }
    }
    
    /**
     * Trigger screen shake effect.
     */
    public void shake(double intensity, double duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
        this.shakeTimer = duration;
    }
    
    /**
     * Trigger screen shake with defaults.
     */
    public void shake() {
        shake(Constants.SCREEN_SHAKE_INTENSITY, Constants.SCREEN_SHAKE_DURATION);
    }
    
    /**
     * Trigger screen flash effect.
     */
    public void flash(Color color, double duration) {
        this.flashColor = color;
        this.flashDuration = duration;
        this.flashTimer = duration;
    }
    
    /**
     * Trigger white flash with default duration.
     */
    public void flashWhite() {
        flash(Color.WHITE, 0.15);
    }
    
    /**
     * Trigger red flash for damage.
     */
    public void flashRed() {
        flash(Color.rgb(255, 50, 50), 0.2);
    }
    
    /**
     * Trigger subtle pulse effect (zoom in and back).
     */
    public void pulse(double scale) {
        this.pulseScale = scale;
        this.pulseTarget = 1.0;
    }
    
    /**
     * Trigger collision effects: shake + flash + pulse.
     */
    public void triggerHitEffect() {
        shake(Constants.SCREEN_SHAKE_INTENSITY, Constants.SCREEN_SHAKE_DURATION);
        flash(Color.rgb(255, 100, 100), 0.1);
        pulse(1.02);
    }
    
    /**
     * Trigger heart collection effect.
     */
    public void triggerCollectEffect() {
        pulse(1.015);
        flash(Color.rgb(255, 215, 0, 0.3), 0.1);
    }
    
    /**
     * Get current shake offset X.
     */
    public double getShakeOffsetX() {
        return shakeOffsetX;
    }
    
    /**
     * Get current shake offset Y.
     */
    public double getShakeOffsetY() {
        return shakeOffsetY;
    }
    
    /**
     * Reset all effects.
     */
    public void reset() {
        shakeTimer = 0;
        shakeOffsetX = 0;
        shakeOffsetY = 0;
        flashTimer = 0;
        flashAlpha = 0;
        pulseScale = 1.0;
        pulseTarget = 1.0;
    }
}
