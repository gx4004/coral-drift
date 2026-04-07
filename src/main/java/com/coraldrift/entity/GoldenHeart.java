package com.coraldrift.entity;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;

/**
 * Rare golden heart collectible worth 3× the normal score.
 * Features a crown, multi-ring sparkle orbit, and golden glow.
 */
public class GoldenHeart extends HeartCollectible {

    private double crownAnimTime = 0;
    private final double[] orbitPhases = new double[12];
    private boolean collected = false;
    private double collectAnimTime = 0;

    private static final Color GOLD_BRIGHT  = Color.web("#FFD700");
    private static final Color GOLD_MID     = Color.web("#ffb300");
    private static final Color GOLD_DEEP    = Color.web("#e65100");
    private static final Color GOLD_GLOW    = Color.web("#FFD700", 0.55);

    public GoldenHeart(double x, double y) {
        super(x, y);
        for (int i = 0; i < orbitPhases.length; i++) {
            orbitPhases[i] = MathUtil.randomRange(0, Math.PI * 2);
        }
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        crownAnimTime += deltaTime;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (collected) {
            renderCollectAnim(gc);
            return;
        }

        gc.save();

        double size   = width * (1.0 + Math.sin(animTime * Constants.HEART_PULSE_SPEED * 2 * Math.PI) * Constants.HEART_PULSE_AMOUNT);
        double cx     = x + width / 2;
        double cy     = y + height / 2;

        // ── Outer glow ───────────────────────────────────────────────────────
        RadialGradient outerGlow = new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.TRANSPARENT),
            new Stop(0.5, Color.web("#FFD700", 0.15)),
            new Stop(1, Color.web("#FFD700", 0.4))
        );
        gc.setFill(outerGlow);
        gc.fillOval(cx - size, cy - size, size * 2, size * 2);

        // ── 12-sparkle orbit (3 rings) ────────────────────────────────────────
        for (int i = 0; i < 12; i++) {
            int ring = i / 4;
            double radiusFactor = 0.55 + ring * 0.18;
            double speed = 1.0 - ring * 0.2;
            double angle = animTime * speed + orbitPhases[i] + i * (Math.PI * 2 / 4);
            double r = size * radiusFactor;
            double sx = cx + Math.cos(angle) * r;
            double sy = cy + Math.sin(angle) * r;
            double alpha = 0.5 + 0.5 * Math.abs(Math.sin(animTime * 3 + orbitPhases[i]));
            double sSize = 2.5 + 1.5 * Math.abs(Math.sin(animTime * 2 + i));
            gc.setFill(Color.hsb(50 + i * 5, 0.9, 1.0, alpha));
            gc.fillOval(sx - sSize / 2, sy - sSize / 2, sSize, sSize);
            gc.setStroke(Color.web("#FFD700", alpha * 0.5));
            gc.setLineWidth(0.8);
            gc.strokeLine(sx - sSize, sy, sx + sSize, sy);
            gc.strokeLine(sx, sy - sSize, sx, sy + sSize);
        }

        // ── Heart body ────────────────────────────────────────────────────────
        drawGoldenHeart(gc, cx, cy, size);

        // ── Crown ─────────────────────────────────────────────────────────────
        double crownY = cy - size * 0.9 - Math.sin(crownAnimTime * 1.5) * 3;
        drawCrown(gc, cx, crownY, size * 0.55);

        gc.restore();
    }

    private void drawGoldenHeart(GraphicsContext gc, double cx, double cy, double size) {
        // Rich golden glow
        RadialGradient glowGrad = new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, GOLD_GLOW),
            new Stop(0.6, Color.web("#FFD700", 0.2)),
            new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(glowGrad);
        gc.fillOval(cx - size * 0.75, cy - size * 0.75, size * 1.5, size * 1.5);

        // Heart gradient — gold
        RadialGradient heartGrad = new RadialGradient(
            0, 0, 0.3, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#fff0a0")),
            new Stop(0.35, GOLD_BRIGHT),
            new Stop(0.7, GOLD_MID),
            new Stop(1, GOLD_DEEP)
        );
        gc.setFill(heartGrad);

        double w = size, h = size;
        double topY = cy - h * 0.35, bottomY = cy + h * 0.45;

        gc.beginPath();
        gc.moveTo(cx, bottomY);
        gc.bezierCurveTo(cx - w * 0.5, cy + h * 0.1, cx - w * 0.5, topY, cx, topY + h * 0.15);
        gc.bezierCurveTo(cx + w * 0.5, topY, cx + w * 0.5, cy + h * 0.1, cx, bottomY);
        gc.closePath();
        gc.fill();

        gc.setStroke(GOLD_MID.darker());
        gc.setLineWidth(1.5);
        gc.stroke();

        // Shine
        gc.setFill(Color.rgb(255, 255, 255, 0.5));
        gc.fillOval(cx - w * 0.2, topY + h * 0.15, w * 0.25, h * 0.2);
    }

    private void drawCrown(GraphicsContext gc, double cx, double cy, double size) {
        double h = size * 0.6;
        double w = size;

        LinearGradient crownGrad = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#fff176")),
            new Stop(1, GOLD_MID)
        );
        gc.setFill(crownGrad);
        gc.setStroke(GOLD_DEEP);
        gc.setLineWidth(1.2);

        // Crown base rectangle
        gc.fillRect(cx - w / 2, cy, w, h * 0.45);
        gc.strokeRect(cx - w / 2, cy, w, h * 0.45);

        // Three crown points using filled triangles
        double baseY = cy;
        // Left point
        gc.beginPath();
        gc.moveTo(cx - w / 2, baseY);
        gc.lineTo(cx - w * 0.22, baseY - h * 0.55);
        gc.lineTo(cx - w * 0.05, baseY);
        gc.closePath();
        gc.fill(); gc.stroke();

        // Center point (tallest)
        gc.beginPath();
        gc.moveTo(cx - w * 0.12, baseY);
        gc.lineTo(cx, baseY - h * 0.85);
        gc.lineTo(cx + w * 0.12, baseY);
        gc.closePath();
        gc.fill(); gc.stroke();

        // Right point
        gc.beginPath();
        gc.moveTo(cx + w * 0.05, baseY);
        gc.lineTo(cx + w * 0.22, baseY - h * 0.55);
        gc.lineTo(cx + w / 2, baseY);
        gc.closePath();
        gc.fill(); gc.stroke();

        // Small gem dots on crown
        gc.setFill(Color.web("#ff6b6b"));
        gc.fillOval(cx - 3, cy + 2, 6, 6);
        gc.setFill(Color.web("#74b9ff"));
        gc.fillOval(cx - w * 0.3, cy + 2, 5, 5);
        gc.fillOval(cx + w * 0.3 - 5, cy + 2, 5, 5);
    }

    private void renderCollectAnim(GraphicsContext gc) {
        collectAnimTime += 0.016;
        if (collectAnimTime > 0.35) { active = false; return; }
        double t = collectAnimTime / 0.35;
        gc.save();
        gc.setGlobalAlpha(1 - t);
        double cx = x + width / 2, cy = y + height / 2;
        double size = width * (1 + t * 0.8);
        drawGoldenHeart(gc, cx, cy, size);
        gc.restore();
    }

    @Override
    public void collect() {
        if (!collected) {
            collected = true;
            collectAnimTime = 0;
            super.collect();
        }
    }
}
