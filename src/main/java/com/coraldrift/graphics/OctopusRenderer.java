package com.coraldrift.graphics;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.paint.*;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;

/**
 * Procedural renderer for the pink octopus protagonist.
 * All visuals are drawn using JavaFX primitives - no external images needed.
 */
public class OctopusRenderer {

    // ─── Expression enum ──────────────────────────────────────────────────────
    public enum Expression { NORMAL, HAPPY_STAR, PAIN, DEAD }

    // Animation state
    private double animTime = 0;
    private double squashStretch = 1.0;
    private double targetSquashStretch = 1.0;
    private boolean isGlowing = false;
    private double glowIntensity = 0;
    private boolean isShielded = false;
    private double shieldPulse = 0;

    // Expression state
    private Expression currentExpression = Expression.NORMAL;

    // Blink state
    private double blinkTimer    = 0;
    private double nextBlinkTime = 2.5;
    private double blinkProgress = 0;   // 0 = open, 1 = fully closed
    private boolean blinking     = false;
    
    // Sparkle particles around octopus
    private final double[] sparkleX = new double[8];
    private final double[] sparkleY = new double[8];
    private final double[] sparklePhase = new double[8];
    
    // Cached effects
    private final DropShadow shadow;
    private final Glow glow;
    
    public OctopusRenderer() {
        shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setColor(Color.rgb(255, 105, 180, 0.4));
        
        glow = new Glow();
        glow.setLevel(0.3);
        
        // Initialize sparkle positions
        for (int i = 0; i < sparkleX.length; i++) {
            sparklePhase[i] = MathUtil.randomRange(0, Math.PI * 2);
        }
    }
    
    /** Set the current emotional expression. */
    public void setExpression(Expression e) { this.currentExpression = e; }

    /**
     * Update animation state.
     */
    public void update(double deltaTime, boolean isJumping, boolean isOnGround, double velocityY) {
        animTime += deltaTime;
        shieldPulse += deltaTime * 4;

        // Blink update — skip when dead/pain
        if (currentExpression == Expression.NORMAL || currentExpression == Expression.HAPPY_STAR) {
            blinkTimer += deltaTime;
            if (!blinking && blinkTimer >= nextBlinkTime) {
                blinking      = true;
                blinkProgress = 0;
                blinkTimer    = 0;
                nextBlinkTime = MathUtil.randomRange(2.0, 4.5);
            }
            if (blinking) {
                blinkProgress += deltaTime / (0.15 / 2.0); // 0.15s total = 0.075s each way
                if (blinkProgress >= 2.0) {
                    blinking      = false;
                    blinkProgress = 0;
                }
            }
        } else {
            blinking      = false;
            blinkProgress = 0;
        }
        
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
     * Set shield state.
     */
    public void setShielded(boolean shielded) {
        this.isShielded = shielded;
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
        
        // Draw shield bubble if active
        if (isShielded) {
            drawShieldBubble(gc, x, y, width, height);
        }
        
        // Draw ambient sparkles around octopus
        drawSparkles(gc, x, y, width, height);
        
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
        
        // Draw shimmer highlights
        drawShimmer(gc, x, y, width, height);
        
        gc.restore();
    }
    
    private void drawShieldBubble(GraphicsContext gc, double x, double y, double w, double h) {
        double bubbleSize = Math.max(w, h) * 1.8;
        double cx = x + w / 2;
        double cy = y + h / 2;
        double pulse = 1.0 + Math.sin(shieldPulse) * 0.05;
        double r = bubbleSize / 2 * pulse;
        
        // Outer glow
        RadialGradient outerGlow = new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.TRANSPARENT),
            new Stop(0.7, Color.rgb(100, 200, 255, 0.1)),
            new Stop(1, Color.rgb(100, 200, 255, 0.3))
        );
        gc.setFill(outerGlow);
        gc.fillOval(cx - r * 1.2, cy - r * 1.2, r * 2.4, r * 2.4);
        
        // Main bubble
        RadialGradient bubbleGrad = new RadialGradient(
            0, 0, 0.3, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(200, 240, 255, 0.1)),
            new Stop(0.7, Color.rgb(100, 200, 255, 0.15)),
            new Stop(1, Color.rgb(80, 180, 220, 0.25))
        );
        gc.setFill(bubbleGrad);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
        
        // Bubble edge
        gc.setStroke(Color.rgb(150, 220, 255, 0.5));
        gc.setLineWidth(2);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        
        // Shine
        gc.setFill(Color.rgb(255, 255, 255, 0.4));
        gc.fillOval(cx - r * 0.6, cy - r * 0.7, r * 0.4, r * 0.25);
    }
    
    private void drawSparkles(GraphicsContext gc, double x, double y, double w, double h) {
        double cx = x + w / 2;
        double cy = y + h / 2;
        double radius = Math.max(w, h) * 0.85;

        for (int i = 0; i < sparkleX.length; i++) {
            double angle = animTime * 0.7 + sparklePhase[i] + i * (Math.PI * 2 / sparkleX.length);
            double dist = radius * (0.55 + 0.45 * Math.sin(animTime * 1.8 + i));
            double sx = cx + Math.cos(angle) * dist;
            double sy = cy + Math.sin(angle) * dist;

            double alpha = 0.45 + 0.55 * Math.abs(Math.sin(animTime * 3.5 + sparklePhase[i]));
            double size = 3.5 + 2.5 * Math.abs(Math.sin(animTime * 2.5 + i));

            // Sparkle hue cycles
            double sparkHue = (animTime * 60 + i * 45) % 360;
            gc.setFill(Color.hsb(sparkHue, 0.3, 1.0, alpha));
            gc.fillOval(sx - size/2, sy - size/2, size, size);

            // Cross lines for sparkle star effect
            gc.setStroke(Color.rgb(255, 220, 255, alpha * 0.8));
            gc.setLineWidth(1.2);
            gc.strokeLine(sx - size * 1.3, sy, sx + size * 1.3, sy);
            gc.strokeLine(sx, sy - size * 1.3, sx, sy + size * 1.3);
        }

        // 3 tiny floating hearts drifting up around the octopus
        for (int i = 0; i < 3; i++) {
            double heartAngle = animTime * 0.3 + i * (Math.PI * 2 / 3) + sparklePhase[i];
            double heartDist = radius * 0.9;
            double hx = cx + Math.cos(heartAngle) * heartDist;
            double hy = cy + Math.sin(heartAngle) * heartDist - Math.sin(animTime * 2 + i) * 6;
            double heartAlpha = 0.3 + 0.4 * Math.abs(Math.sin(animTime * 1.5 + i));
            gc.setFill(Color.rgb(255, 150, 190, heartAlpha));
            drawHeart(gc, hx, hy, 7);
        }
    }

    private void drawHeart(GraphicsContext gc, double cx, double cy, double size) {
        gc.fillOval(cx - size * 0.48, cy - size * 0.5, size * 0.52, size * 0.52);
        gc.fillOval(cx - size * 0.06, cy - size * 0.5, size * 0.52, size * 0.52);
        gc.beginPath();
        gc.moveTo(cx - size * 0.55, cy - size * 0.15);
        gc.lineTo(cx, cy + size * 0.5);
        gc.lineTo(cx + size * 0.55, cy - size * 0.15);
        gc.closePath();
        gc.fill();
    }
    
    private void drawShimmer(GraphicsContext gc, double x, double y, double w, double h) {
        double bodyW = w * 0.85;
        double bodyH = h * 0.65;
        double bodyX = x + (w - bodyW) / 2;

        // Rainbow iridescent sheen sweeping across body
        double iridPhase = (animTime * 0.4) % 1.0;
        double iridX = bodyX + iridPhase * bodyW;
        double hue = (animTime * 40) % 360;
        gc.setFill(Color.hsb(hue, 0.6, 1.0, 0.15));
        gc.fillOval(iridX - bodyW * 0.3, y + bodyH * 0.1, bodyW * 0.6, bodyH * 0.8);

        // Moving shimmer highlight
        double shimmerPhase = (animTime * 0.8) % 1.0;
        double shimmerX = bodyX + shimmerPhase * bodyW;
        double shimmerY = y + h * 0.2;

        if (shimmerPhase > 0.1 && shimmerPhase < 0.9) {
            double shimmerAlpha = 0.55 * (1 - Math.abs(shimmerPhase - 0.5) * 2);
            gc.setFill(Color.rgb(255, 255, 255, shimmerAlpha));
            gc.fillOval(shimmerX - 10, shimmerY, 20, 9);
        }
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
        gc.save();
        gc.setLineCap(StrokeLineCap.ROUND);

        double cx0        = x + w / 2;
        double bodyBottom = y + h * 0.58;
        int n = 6;

        for (int i = 0; i < n; i++) {
            double normalT   = (double) i / (n - 1);              // 0..1
            double spreadRad = Math.toRadians(-60 + normalT * 120);

            double phase = animTime * 2.3 + i * 1.05;
            double wave  = Math.sin(phase) * 7;

            double baseX = cx0 + Math.sin(spreadRad) * w * 0.28;

            // Middle tentacles are slightly longer
            double lenFactor = 0.52 + 0.11 * (1 - Math.abs(normalT - 0.5) * 2);
            double len       = h * lenFactor;

            double endX  = baseX + Math.sin(spreadRad) * len * 0.5 + wave;
            double endY  = bodyBottom + len;

            double cp1x  = baseX + Math.sin(spreadRad) * len * 0.18 + wave * 0.3;
            double cp1y  = bodyBottom + len * 0.32;
            double cp2x  = baseX + Math.sin(spreadRad) * len * 0.38 + wave;
            double cp2y  = bodyBottom + len * 0.72;

            double baseW = w * 0.065;

            // Pass 1 – dark shadow outline
            gc.setStroke(Constants.OCTOPUS_PINK.darker().darker());
            gc.setLineWidth(baseW);
            drawCubicStroke(gc, baseX, bodyBottom, cp1x, cp1y, cp2x, cp2y, endX, endY);

            // Pass 2 – main colour
            gc.setStroke(Constants.OCTOPUS_PINK);
            gc.setLineWidth(baseW * 0.68);
            drawCubicStroke(gc, baseX, bodyBottom, cp1x + 1, cp1y, cp2x + 1, cp2y, endX, endY);

            // Pass 3 – highlight stripe
            gc.setStroke(Constants.OCTOPUS_PINK_SOFT);
            gc.setLineWidth(baseW * 0.25);
            drawCubicStroke(gc, baseX, bodyBottom, cp1x + 1, cp1y - 2, cp2x + 1, cp2y - 2, endX, endY);

            // Sucker dots along inner curve
            gc.setFill(Constants.OCTOPUS_PINK_SOFT);
            double[] suckerTs = {0.28, 0.52, 0.73};
            for (double t : suckerTs) {
                double[] pt = cubicPoint(baseX, bodyBottom, cp1x, cp1y, cp2x, cp2y, endX, endY, t);
                double sd   = 2.0 * (1 - t * 0.45);
                gc.fillOval(pt[0] - sd, pt[1] - sd, sd * 2, sd * 2);
            }
        }
        gc.restore();
    }

    private void drawCubicStroke(GraphicsContext gc,
                                  double x0, double y0,
                                  double cx1, double cy1,
                                  double cx2, double cy2,
                                  double x1, double y1) {
        gc.beginPath();
        gc.moveTo(x0, y0);
        gc.bezierCurveTo(cx1, cy1, cx2, cy2, x1, y1);
        gc.stroke();
    }

    private double[] cubicPoint(double x0, double y0,
                                 double cx1, double cy1,
                                 double cx2, double cy2,
                                 double x1, double y1, double t) {
        double u = 1 - t;
        return new double[]{
            u*u*u*x0 + 3*u*u*t*cx1 + 3*u*t*t*cx2 + t*t*t*x1,
            u*u*u*y0 + 3*u*u*t*cy1 + 3*u*t*t*cy2 + t*t*t*y1
        };
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
        
        // Soft cute eyebrow arches (alive expressions only)
        if (currentExpression == Expression.NORMAL || currentExpression == Expression.HAPPY_STAR) {
            gc.setStroke(Constants.OCTOPUS_PINK.darker());
            gc.setLineWidth(2.0);
            double browY = bodyCenterY - eyeSize * 0.75;
            gc.strokeArc(bodyCenterX - eyeSpacing - eyeSize * 0.38,
                         browY - eyeSize * 0.18,
                         eyeSize * 0.76, eyeSize * 0.36,
                         0, 180, ArcType.OPEN);
            gc.strokeArc(bodyCenterX + eyeSpacing - eyeSize * 0.38,
                         browY - eyeSize * 0.18,
                         eyeSize * 0.76, eyeSize * 0.36,
                         0, 180, ArcType.OPEN);
        }

        // Cute blush cheeks
        gc.setFill(Constants.OCTOPUS_CHEEK);
        double cheekSize = bodyW * 0.12;
        gc.fillOval(bodyCenterX - eyeSpacing * 1.6 - cheekSize/2, bodyCenterY + eyeSize * 0.4, cheekSize, cheekSize * 0.6);
        gc.fillOval(bodyCenterX + eyeSpacing * 1.6 - cheekSize/2, bodyCenterY + eyeSize * 0.4, cheekSize, cheekSize * 0.6);
        
        // Mouth — expression-aware
        double mouthY = bodyCenterY + eyeSize * 0.8;
        gc.setStroke(Constants.OCTOPUS_PINK.darker().darker());
        gc.setLineWidth(2.5);

        if (currentExpression == Expression.HAPPY_STAR) {
            // Wide open happy mouth
            gc.strokeArc(bodyCenterX - 11, mouthY - 5, 22, 13, 200, 140, ArcType.OPEN);
            // Small tooth gap
            gc.setFill(Color.rgb(255, 255, 255, 0.7));
            gc.fillOval(bodyCenterX - 4, mouthY - 1, 8, 5);
        } else if (currentExpression == Expression.PAIN || currentExpression == Expression.DEAD) {
            // Sad arc — rotate 180° around mouth center
            gc.save();
            gc.translate(bodyCenterX, mouthY + 2);
            gc.rotate(180);
            gc.strokeArc(-8, -4, 16, 10, 200, 140, ArcType.OPEN);
            gc.restore();
        } else {
            // Normal happy arc
            gc.strokeArc(bodyCenterX - 8, mouthY - 4, 16, 10, 200, 140, ArcType.OPEN);
        }
    }
    
    private void drawEye(GraphicsContext gc, double cx, double cy, double size, double pupilSize) {
        double effectiveSize = (currentExpression == Expression.HAPPY_STAR) ? size * 1.2 : size;

        // Eye white with soft gradient
        RadialGradient eyeGrad = new RadialGradient(
            0, 0, 0.35, 0.35, 0.65, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.WHITE),
            new Stop(1, Color.rgb(220, 230, 245))
        );
        gc.setFill(eyeGrad);
        gc.fillOval(cx - effectiveSize/2, cy - effectiveSize/2, effectiveSize, effectiveSize);
        gc.setStroke(Color.rgb(80, 80, 80, 0.4));
        gc.setLineWidth(1);
        gc.strokeOval(cx - effectiveSize/2, cy - effectiveSize/2, effectiveSize, effectiveSize);

        double lookOffset = Math.sin(animTime * 0.5) * 2;
        double px = cx + lookOffset;
        double py = cy;

        if (currentExpression == Expression.PAIN || currentExpression == Expression.DEAD) {
            // X-cross pupils + angry V-brow
            double xs = effectiveSize * 0.35;
            gc.setStroke(Color.rgb(40, 20, 50));
            gc.setLineWidth(2.5);
            gc.strokeLine(px - xs, py - xs, px + xs, py + xs);
            gc.strokeLine(px + xs, py - xs, px - xs, py + xs);
            // Angry V-brow
            gc.setStroke(Color.rgb(40, 20, 50));
            gc.setLineWidth(2.0);
            gc.strokeLine(cx - effectiveSize * 0.5, cy - effectiveSize * 0.65,
                          cx,                        cy - effectiveSize * 0.45);
        } else if (currentExpression == Expression.HAPPY_STAR) {
            // 5-point star pupils
            double hs = pupilSize * 0.65;
            gc.setFill(Color.rgb(255, 200, 0));
            drawStarPupil(gc, px, py, hs);
            // Shine
            gc.setFill(Color.rgb(255, 255, 255, 0.9));
            gc.fillOval(px - hs * 0.55, py - hs * 0.7, hs * 0.35, hs * 0.3);
        } else {
            // NORMAL — heart pupil
            double hs = pupilSize * 0.55;
            gc.setFill(Color.rgb(40, 20, 50));
            drawHeart(gc, px, py, hs);
            // Eye shine
            gc.setFill(Color.rgb(255, 255, 255, 0.9));
            double shineSize = pupilSize * 0.35;
            gc.fillOval(px - hs * 0.55, py - hs * 0.7, shineSize, shineSize);
            gc.setFill(Color.rgb(255, 255, 255, 0.6));
            gc.fillOval(px + hs * 0.1, py - hs * 0.4, shineSize * 0.5, shineSize * 0.5);
        }

        // Blink eyelid (NORMAL / HAPPY_STAR only)
        if (blinking && blinkProgress > 0) {
            double lidFraction = blinkProgress <= 1.0 ? blinkProgress : 2.0 - blinkProgress;
            double lidH = effectiveSize * lidFraction;
            gc.setFill(Constants.OCTOPUS_PINK);
            gc.fillRect(cx - effectiveSize / 2 - 1, cy - effectiveSize / 2 - 1,
                        effectiveSize + 2, lidH + 1);
        }
    }

    private void drawStarPupil(GraphicsContext gc, double cx, double cy, double size) {
        int points = 5;
        gc.beginPath();
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI * i / points - Math.PI / 2;
            double r = (i % 2 == 0) ? size : size * 0.42;
            double sx = cx + Math.cos(angle) * r;
            double sy = cy + Math.sin(angle) * r;
            if (i == 0) gc.moveTo(sx, sy); else gc.lineTo(sx, sy);
        }
        gc.closePath();
        gc.fill();
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
        currentExpression = Expression.NORMAL;
        blinking = false;
        blinkProgress = 0;
        blinkTimer = 0;
        nextBlinkTime = 2.5;
    }
}
