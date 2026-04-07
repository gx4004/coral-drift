package com.coraldrift.graphics;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Screen-wide visual effects: directional shake, chromatic aberration flag,
 * flash, pulse, and pause dimming.
 */
public class ScreenEffects {

    // Screen shake
    private double shakeIntensity = 0;
    private double shakeDuration  = 0;
    private double shakeTimer     = 0;
    private double shakeOffsetX   = 0;
    private double shakeOffsetY   = 0;
    private double shakeDirX      = 0; // primary shake direction
    private double shakeDirY      = 0;

    // Flash effect
    private double flashAlpha    = 0;
    private Color  flashColor    = Color.WHITE;
    private double flashDuration = 0;
    private double flashTimer    = 0;

    // Pulse effect
    private double pulseScale  = 1.0;
    private double pulseTarget = 1.0;

    // Chromatic aberration (frame count)
    private int chromaticFramesLeft = 0;

    // Pause dim
    private double darkOverlayAlpha = 0;

    // ─── Update ───────────────────────────────────────────────────────────────

    public void update(double deltaTime) {
        updateShake(deltaTime);
        updateFlash(deltaTime);
        updatePulse(deltaTime);
    }

    private void updateShake(double deltaTime) {
        if (shakeTimer > 0) {
            shakeTimer -= deltaTime;
            double progress  = shakeTimer / shakeDuration;
            double intensity = shakeIntensity * progress;

            // Directional primary + small random secondary
            double t = shakeTimer * 40; // high-freq oscillation
            shakeOffsetX = Math.cos(t) * intensity * (shakeDirX != 0 ? shakeDirX : 1)
                + MathUtil.randomRange(-0.3, 0.3) * intensity;
            shakeOffsetY = Math.sin(t * 0.7) * intensity * (shakeDirY != 0 ? shakeDirY : 0.4)
                + MathUtil.randomRange(-0.3, 0.3) * intensity;
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
        if (Math.abs(pulseScale - pulseTarget) < 0.001) pulseTarget = 1.0;
    }

    // ─── Pre/post render ──────────────────────────────────────────────────────

    public void applyPreRender(GraphicsContext gc) {
        if (shakeOffsetX != 0 || shakeOffsetY != 0) {
            gc.translate(shakeOffsetX, shakeOffsetY);
        }
        if (pulseScale != 1.0) {
            double cx = Constants.WINDOW_WIDTH / 2, cy = Constants.WINDOW_HEIGHT / 2;
            gc.translate(cx, cy);
            gc.scale(pulseScale, pulseScale);
            gc.translate(-cx, -cy);
        }
    }

    public void renderOverlay(GraphicsContext gc) {
        // Pause dim
        if (darkOverlayAlpha > 0) {
            gc.save();
            gc.setGlobalAlpha(darkOverlayAlpha);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            gc.restore();
        }

        // Flash
        if (flashAlpha > 0.01) {
            gc.save();
            gc.setGlobalAlpha(flashAlpha * 0.55);
            gc.setFill(flashColor);
            gc.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            gc.restore();
        }
    }

    // ─── Trigger methods ──────────────────────────────────────────────────────

    /** Directional shake — direction (1,0) = shakes rightward (coral hit from right). */
    public void shake(double intensity, double duration, double dirX, double dirY) {
        this.shakeIntensity = intensity;
        this.shakeDuration  = duration;
        this.shakeTimer     = duration;
        this.shakeDirX      = dirX;
        this.shakeDirY      = dirY;
    }

    public void shake(double intensity, double duration) { shake(intensity, duration, 1, 0.3); }
    public void shake() { shake(Constants.SCREEN_SHAKE_INTENSITY, Constants.SCREEN_SHAKE_DURATION); }

    public void flash(Color color, double duration) {
        this.flashColor    = color;
        this.flashDuration = duration;
        this.flashTimer    = duration;
    }

    public void flashWhite() { flash(Color.WHITE, 0.15); }
    public void flashRed()   { flash(Color.rgb(255, 50, 50), 0.2); }

    public void pulse(double scale) { this.pulseScale = scale; this.pulseTarget = 1.0; }

    public void triggerHitEffect() {
        shake(Constants.SCREEN_SHAKE_INTENSITY, Constants.SCREEN_SHAKE_DURATION, 1.0, 0.25);
        flash(Color.rgb(255, 100, 100), 0.12);
        pulse(1.02);
    }

    public void triggerCollectEffect() {
        pulse(1.015);
        flash(Color.rgb(255, 215, 0, 0.3), 0.10);
    }

    // ─── Chromatic aberration ─────────────────────────────────────────────────

    public void triggerChromaticAberration() { chromaticFramesLeft = 3; }
    public boolean isChromaticActive()       { return chromaticFramesLeft > 0; }
    public void decrementChromatic()         { if (chromaticFramesLeft > 0) chromaticFramesLeft--; }

    // ─── Pause dim ────────────────────────────────────────────────────────────

    public void setPauseDim(double alpha)   { this.darkOverlayAlpha = alpha; }
    public double getDarkOverlayAlpha()     { return darkOverlayAlpha; }

    // ─── Getters / reset ──────────────────────────────────────────────────────

    public double getShakeOffsetX() { return shakeOffsetX; }
    public double getShakeOffsetY() { return shakeOffsetY; }

    public void reset() {
        shakeTimer      = 0;
        shakeOffsetX    = 0;
        shakeOffsetY    = 0;
        flashTimer      = 0;
        flashAlpha      = 0;
        pulseScale      = 1.0;
        pulseTarget     = 1.0;
        chromaticFramesLeft = 0;
        darkOverlayAlpha    = 0;
    }
}
