package com.coraldrift.entity;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;

/**
 * Coral obstacle — 4 visually distinct types with premium silhouettes.
 * Now includes near-miss detection support.
 */
public class CoralObstacle extends Entity {

    public enum CoralType {
        SHORT,      // Barnacle ridge — wide, flat, covered in bumps
        TALL,       // Narrow spire with 3 symmetric branch pairs
        BRANCHING,  // Fan coral spread — wide arc of polyp-tipped arms
        CLUSTER     // Pyramid of bubble-spheres with neck connectors
    }

    private final CoralType type;
    private final Color primaryColor;
    private final Color secondaryColor;
    private final double[] branchOffsets;
    private final int branchCount;

    // Near-miss system
    private boolean nearMissChecked = false;
    private boolean nearMissFlash   = false;
    private double  nearMissFlashTimer = 0;
    private static final double FLASH_DURATION = 0.25;

    public CoralObstacle(double x, CoralType type) {
        super(x, 0, 0, 0);
        this.type = type;

        Color[] colors = { Constants.CORAL_PINK, Constants.CORAL_LIGHT,
                           Constants.CORAL_ORANGE, Constants.CORAL_PURPLE };
        this.primaryColor   = MathUtil.randomElement(colors);
        this.secondaryColor = primaryColor.brighter();

        switch (type) {
            case SHORT:
                this.width  = MathUtil.randomRange(50, 70);
                this.height = MathUtil.randomRange(38, 58);
                break;
            case TALL:
                this.width  = MathUtil.randomRange(28, 42);
                this.height = MathUtil.randomRange(70, 95);
                break;
            case BRANCHING:
                this.width  = MathUtil.randomRange(60, 82);
                this.height = MathUtil.randomRange(62, 88);
                break;
            case CLUSTER:
                this.width  = MathUtil.randomRange(72, 105);
                this.height = MathUtil.randomRange(48, 68);
                break;
        }

        this.y = Constants.GROUND_Y - this.height;

        this.branchCount   = MathUtil.randomInt(3, 6);
        this.branchOffsets = new double[branchCount * 2];
        for (int i = 0; i < branchCount; i++) {
            branchOffsets[i * 2]     = MathUtil.randomRange(-0.3, 0.3);
            branchOffsets[i * 2 + 1] = MathUtil.randomRange(0.5, 1.0);
        }
    }

    // ─── Near-miss API ────────────────────────────────────────────────────────

    public boolean isNearMissChecked()          { return nearMissChecked; }
    public void setNearMissChecked(boolean v)   { nearMissChecked = v; }

    public void triggerNearMissFlash() {
        nearMissFlash      = true;
        nearMissFlashTimer = FLASH_DURATION;
    }

    // ─── Update / scroll ──────────────────────────────────────────────────────

    @Override
    public void update(double deltaTime) {
        if (nearMissFlash) {
            nearMissFlashTimer -= deltaTime;
            if (nearMissFlashTimer <= 0) nearMissFlash = false;
        }
    }

    public void scroll(double amount) { x -= amount; }

    // ─── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(GraphicsContext gc) {
        gc.save();
        switch (type) {
            case SHORT:     renderShortCoral(gc);     break;
            case TALL:      renderTallCoral(gc);      break;
            case BRANCHING: renderBranchingCoral(gc); break;
            case CLUSTER:   renderClusterCoral(gc);   break;
        }
        // Near-miss flash: yellow outline
        if (nearMissFlash) {
            double alpha = nearMissFlashTimer / FLASH_DURATION;
            gc.setStroke(Color.rgb(255, 240, 60, alpha));
            gc.setLineWidth(3.5);
            gc.strokeRect(x - 2, y - 2, width + 4, height + 4);
        }
        gc.restore();
    }

    // ─── SHORT: barnacle ridge ────────────────────────────────────────────────

    private void renderShortCoral(GraphicsContext gc) {
        double baseY = y + height * 0.35;

        // Base body — trapezoidal ridge
        LinearGradient bodyGrad = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, secondaryColor),
            new Stop(0.5, primaryColor),
            new Stop(1, primaryColor.darker())
        );
        gc.setFill(bodyGrad);
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(1.8);

        gc.beginPath();
        gc.moveTo(x, y + height);
        gc.lineTo(x + width * 0.06, baseY);
        gc.quadraticCurveTo(x + width * 0.5, baseY - height * 0.08, x + width * 0.94, baseY);
        gc.lineTo(x + width, y + height);
        gc.closePath();
        gc.fill();
        gc.stroke();

        // Barnacle bumps along top ridge
        int bumps = 5;
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(1.2);
        for (int i = 0; i < bumps; i++) {
            double t   = (i + 0.5) / bumps;
            double bx  = x + t * width;
            double by  = baseY - height * 0.07 * (1 - Math.abs(t - 0.5) * 1.2);
            double bw  = width * (0.12 + (i % 2) * 0.04);
            double bh  = bw * 0.7;
            RadialGradient bumpGrad = new RadialGradient(
                0, 0, 0.35, 0.3, 0.65, true, CycleMethod.NO_CYCLE,
                new Stop(0, secondaryColor.brighter()),
                new Stop(1, primaryColor)
            );
            gc.setFill(bumpGrad);
            gc.fillOval(bx - bw / 2, by - bh, bw, bh);
            gc.strokeOval(bx - bw / 2, by - bh, bw, bh);
        }

        // Face nodules
        gc.setFill(primaryColor.brighter());
        gc.fillOval(x + width * 0.2, baseY + height * 0.15, width * 0.12, height * 0.14);
        gc.fillOval(x + width * 0.55, baseY + height * 0.1, width * 0.10, height * 0.12);

        // Highlight
        gc.setFill(Color.rgb(255, 255, 255, 0.22));
        gc.fillOval(x + width * 0.12, baseY + height * 0.05, width * 0.22, height * 0.18);
    }

    // ─── TALL: spire with 3 symmetric branch pairs ────────────────────────────

    private void renderTallCoral(GraphicsContext gc) {
        LinearGradient spireGrad = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, primaryColor.darker()),
            new Stop(0.4, secondaryColor),
            new Stop(1, primaryColor.darker())
        );
        gc.setFill(spireGrad);
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(1.8);

        double baseW = width;
        double topW  = width * 0.22;
        double cx    = x + width / 2;

        // Main spire body
        gc.beginPath();
        gc.moveTo(x, y + height);
        gc.lineTo(cx - topW / 2 - 4, y + height * 0.08);
        gc.quadraticCurveTo(cx, y - 6, cx + topW / 2 + 4, y + height * 0.08);
        gc.lineTo(x + baseW, y + height);
        gc.closePath();
        gc.fill();
        gc.stroke();

        // Center ridge line
        gc.setStroke(secondaryColor.brighter());
        gc.setLineWidth(1.0);
        gc.strokeLine(cx, y + height * 0.92, cx, y + height * 0.08);

        // 3 symmetric branch pairs at heights 0.28, 0.50, 0.72
        double[] branchHeights = {0.28, 0.50, 0.72};
        double[] branchReach   = {22, 17, 12};

        gc.setFill(primaryColor);
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(1.5);

        for (int i = 0; i < 3; i++) {
            double by   = y + height * branchHeights[i];
            double bLen = branchReach[i];
            double bH   = 9 - i * 2;

            // Left branch
            gc.beginPath();
            gc.moveTo(cx - width * 0.15, by);
            gc.quadraticCurveTo(cx - bLen * 0.7, by - bH * 0.6, cx - bLen, by - bH);
            gc.quadraticCurveTo(cx - bLen * 0.8, by + bH * 0.3, cx - width * 0.15, by + 3);
            gc.closePath();
            gc.fill(); gc.stroke();

            // Right branch
            gc.beginPath();
            gc.moveTo(cx + width * 0.15, by);
            gc.quadraticCurveTo(cx + bLen * 0.7, by - bH * 0.6, cx + bLen, by - bH);
            gc.quadraticCurveTo(cx + bLen * 0.8, by + bH * 0.3, cx + width * 0.15, by + 3);
            gc.closePath();
            gc.fill(); gc.stroke();
        }

        // Highlight
        gc.setFill(Color.rgb(255, 255, 255, 0.2));
        gc.fillOval(cx - width * 0.15, y + height * 0.15, width * 0.18, height * 0.14);
    }

    // ─── BRANCHING: fan-coral spread ─────────────────────────────────────────

    private void renderBranchingCoral(GraphicsContext gc) {
        double cx   = x + width / 2;
        double base = y + height;

        // Thick root base
        LinearGradient baseGrad = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, primaryColor),
            new Stop(1, primaryColor.darker().darker())
        );
        gc.setFill(baseGrad);
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(1.8);
        gc.beginPath();
        gc.moveTo(cx - width * 0.18, base);
        gc.quadraticCurveTo(cx - width * 0.10, base - height * 0.22, cx, base - height * 0.18);
        gc.quadraticCurveTo(cx + width * 0.10, base - height * 0.22, cx + width * 0.18, base);
        gc.closePath();
        gc.fill(); gc.stroke();

        // Fan arms from -65° to +65° (7 arms)
        int arms = 7;
        for (int i = 0; i < arms; i++) {
            double t      = (double) i / (arms - 1);
            double angleDeg = -65 + t * 130;
            double angleRad = Math.toRadians(angleDeg - 90); // -90 = upward
            double armLen  = height * (0.65 + 0.2 * Math.abs(0.5 - t)); // center arms longer

            double tipX = cx + Math.cos(angleRad) * armLen;
            double tipY = (base - height * 0.18) + Math.sin(angleRad) * armLen;

            // Depth-based alpha/color
            double depthFactor = 1 - Math.abs(t - 0.5) * 1.0;
            Color armColor = primaryColor.interpolate(secondaryColor.brighter(), depthFactor * 0.5);
            gc.setFill(armColor);
            gc.setStroke(primaryColor.darker().darker());
            gc.setLineWidth(1.4);

            double bw = width * (0.08 + depthFactor * 0.04);
            gc.beginPath();
            gc.moveTo(cx - bw / 2, base - height * 0.16);
            gc.bezierCurveTo(
                cx - bw / 4 + Math.cos(angleRad) * armLen * 0.4,
                (base - height * 0.18) + Math.sin(angleRad) * armLen * 0.4,
                tipX - bw / 6 + Math.cos(angleRad + 0.2) * armLen * 0.2,
                tipY + Math.sin(angleRad + 0.2) * armLen * 0.2,
                tipX, tipY
            );
            gc.bezierCurveTo(
                tipX + bw / 6 + Math.cos(angleRad - 0.2) * armLen * 0.2,
                tipY + Math.sin(angleRad - 0.2) * armLen * 0.2,
                cx + bw / 4 + Math.cos(angleRad) * armLen * 0.4,
                (base - height * 0.18) + Math.sin(angleRad) * armLen * 0.4,
                cx + bw / 2, base - height * 0.16
            );
            gc.closePath();
            gc.fill(); gc.stroke();

            // Polyp blob at tip
            double polyp = bw * 0.9;
            gc.setFill(secondaryColor.brighter());
            gc.fillOval(tipX - polyp / 2, tipY - polyp / 2, polyp, polyp);
        }
    }

    // ─── CLUSTER: pyramid of bubble-spheres ───────────────────────────────────

    private void renderClusterCoral(GraphicsContext gc) {
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(1.4);

        // Pyramid layout: row0=3 spheres, row1=2 spheres, row2=1 sphere
        double unitR = Math.min(width, height) * 0.22;
        double baseY = y + height - unitR;

        double[][] centers = {
            // Bottom row (3)
            {x + width * 0.18, baseY},
            {x + width * 0.50, baseY},
            {x + width * 0.82, baseY},
            // Middle row (2)
            {x + width * 0.34, baseY - unitR * 1.5},
            {x + width * 0.66, baseY - unitR * 1.5},
            // Top (1)
            {x + width * 0.50, baseY - unitR * 3.0},
        };
        double[] radii = {unitR, unitR, unitR,
                          unitR * 0.88, unitR * 0.88,
                          unitR * 0.76};

        // Draw neck connectors first (behind spheres)
        gc.setStroke(primaryColor.darker());
        gc.setLineWidth(unitR * 0.45);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        int[][] pairs = {{0,1},{1,2},{0,3},{1,3},{1,4},{2,4},{3,5},{4,5}};
        for (int[] p : pairs) {
            gc.strokeLine(centers[p[0]][0], centers[p[0]][1],
                          centers[p[1]][0], centers[p[1]][1]);
        }

        // Draw spheres
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.SQUARE);
        for (int i = 0; i < centers.length; i++) {
            double cx = centers[i][0], cy = centers[i][1], r = radii[i];
            RadialGradient sphereGrad = new RadialGradient(
                0, 0, 0.35, 0.30, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, secondaryColor.brighter()),
                new Stop(0.55, primaryColor),
                new Stop(1, primaryColor.darker().darker())
            );
            gc.setFill(sphereGrad);
            gc.setStroke(primaryColor.darker().darker());
            gc.setLineWidth(1.2);
            gc.fillOval(cx - r, cy - r, r * 2, r * 2);
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

            // Sphere highlight
            gc.setFill(Color.rgb(255, 255, 255, 0.3));
            gc.fillOval(cx - r * 0.45, cy - r * 0.5, r * 0.45, r * 0.35);
        }
    }

    // ─── Hitbox / helpers ─────────────────────────────────────────────────────

    @Override
    public double[] getHitbox() {
        double scale = Constants.HITBOX_SCALE;
        double hitW  = width  * scale;
        double hitH  = height * scale;
        double hitX  = x + (width  - hitW)  / 2;
        double hitY  = y + (height - hitH) / 2;
        return new double[]{ hitX, hitY, hitW, hitH };
    }

    public boolean isOffScreen() { return x + width < -50; }
    public CoralType getType()   { return type; }

    public static CoralType randomType(double difficultyFactor) {
        double r          = MathUtil.randomRange(0, 1);
        double shortProb  = 0.38 - difficultyFactor * 0.12;
        double tallProb   = 0.25 + difficultyFactor * 0.10;
        double branchProb = 0.22 + difficultyFactor * 0.05;
        if (r < shortProb) return CoralType.SHORT;
        if (r < shortProb + tallProb) return CoralType.TALL;
        if (r < shortProb + tallProb + branchProb) return CoralType.BRANCHING;
        return CoralType.CLUSTER;
    }
}
