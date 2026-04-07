package com.coraldrift.entity;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;

/**
 * Coral obstacle that the player must avoid.
 * Features procedural generation for visual variety.
 */
public class CoralObstacle extends Entity {
    
    public enum CoralType {
        SHORT,      // Low, wide coral
        TALL,       // High, narrow coral
        BRANCHING,  // Multi-branched coral
        CLUSTER     // Group of small corals
    }
    
    private final CoralType type;
    private final Color primaryColor;
    private final Color secondaryColor;
    private final double[] branchOffsets; // For visual variation
    private final int branchCount;
    
    public CoralObstacle(double x, CoralType type) {
        super(x, 0, 0, 0); // Will set proper dimensions based on type
        this.type = type;
        
        // Randomize colors from coral palette
        Color[] colors = { Constants.CORAL_PINK, Constants.CORAL_LIGHT, 
                          Constants.CORAL_ORANGE, Constants.CORAL_PURPLE };
        this.primaryColor = MathUtil.randomElement(colors);
        this.secondaryColor = primaryColor.brighter();
        
        // Set dimensions based on type - ALL heights are jumpable
        switch (type) {
            case SHORT:
                this.width = MathUtil.randomRange(45, 65);
                this.height = MathUtil.randomRange(40, 60);
                break;
            case TALL:
                this.width = MathUtil.randomRange(30, 45);
                this.height = MathUtil.randomRange(70, 95);  // Reduced from 100-140
                break;
            case BRANCHING:
                this.width = MathUtil.randomRange(55, 75);
                this.height = MathUtil.randomRange(60, 85);  // Reduced from 80-120
                break;
            case CLUSTER:
                this.width = MathUtil.randomRange(70, 100);
                this.height = MathUtil.randomRange(50, 70);
                break;
        }
        
        // Position at ground level
        this.y = Constants.GROUND_Y - this.height;
        
        // Generate branch offsets for visual variation
        this.branchCount = MathUtil.randomInt(3, 6);
        this.branchOffsets = new double[branchCount * 2]; // x, height pairs
        for (int i = 0; i < branchCount; i++) {
            branchOffsets[i * 2] = MathUtil.randomRange(-0.3, 0.3);
            branchOffsets[i * 2 + 1] = MathUtil.randomRange(0.5, 1.0);
        }
    }
    
    @Override
    public void update(double deltaTime) {
        // Coral doesn't animate, but scrolling is handled externally
    }
    
    /**
     * Move coral left (called by game engine during scroll).
     */
    public void scroll(double amount) {
        x -= amount;
    }
    
    @Override
    public void render(GraphicsContext gc) {
        gc.save();
        
        switch (type) {
            case SHORT:
                renderShortCoral(gc);
                break;
            case TALL:
                renderTallCoral(gc);
                break;
            case BRANCHING:
                renderBranchingCoral(gc);
                break;
            case CLUSTER:
                renderClusterCoral(gc);
                break;
        }
        
        gc.restore();
    }
    
    private void renderShortCoral(GraphicsContext gc) {
        // Wide, round coral formation
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, secondaryColor),
            new Stop(1, primaryColor.darker())
        );
        
        gc.setFill(gradient);
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(2);
        
        // Main blob
        gc.fillOval(x, y + height * 0.2, width, height * 0.8);
        gc.strokeOval(x, y + height * 0.2, width, height * 0.8);
        
        // Top bumps
        double bumpSize = width * 0.35;
        gc.fillOval(x + width * 0.1, y, bumpSize, bumpSize);
        gc.fillOval(x + width * 0.5, y + height * 0.05, bumpSize * 0.9, bumpSize * 0.9);
        
        // Highlight
        gc.setFill(Color.rgb(255, 255, 255, 0.25));
        gc.fillOval(x + width * 0.15, y + height * 0.25, width * 0.25, height * 0.2);
    }
    
    private void renderTallCoral(GraphicsContext gc) {
        // Tall, spiky coral
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, secondaryColor),
            new Stop(0.7, primaryColor),
            new Stop(1, primaryColor.darker())
        );
        
        gc.setFill(gradient);
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(2);
        
        // Main spike
        double baseWidth = width;
        double topWidth = width * 0.3;
        
        gc.beginPath();
        gc.moveTo(x, y + height);
        gc.lineTo(x + (baseWidth - topWidth) / 2, y + height * 0.1);
        gc.quadraticCurveTo(x + baseWidth / 2, y - 5, x + (baseWidth + topWidth) / 2, y + height * 0.1);
        gc.lineTo(x + baseWidth, y + height);
        gc.closePath();
        gc.fill();
        gc.stroke();
        
        // Side protrusions
        double prong1Y = y + height * 0.4;
        double prong2Y = y + height * 0.6;
        
        gc.beginPath();
        gc.moveTo(x + width * 0.2, prong1Y);
        gc.quadraticCurveTo(x - 10, prong1Y - 15, x + width * 0.15, prong1Y - 20);
        gc.quadraticCurveTo(x + width * 0.1, prong1Y - 5, x + width * 0.2, prong1Y);
        gc.fill();
        
        gc.beginPath();
        gc.moveTo(x + width * 0.8, prong2Y);
        gc.quadraticCurveTo(x + width + 10, prong2Y - 15, x + width * 0.85, prong2Y - 20);
        gc.quadraticCurveTo(x + width * 0.9, prong2Y - 5, x + width * 0.8, prong2Y);
        gc.fill();
        
        // Highlight
        gc.setFill(Color.rgb(255, 255, 255, 0.2));
        gc.fillOval(x + width * 0.3, y + height * 0.2, width * 0.2, height * 0.15);
    }
    
    private void renderBranchingCoral(GraphicsContext gc) {
        // Multi-branch coral tree
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(2);
        
        // Draw branches
        double centerX = x + width / 2;
        double baseY = y + height;
        
        for (int i = 0; i < branchCount; i++) {
            double offsetX = branchOffsets[i * 2] * width;
            double branchHeight = branchOffsets[i * 2 + 1] * height;
            
            LinearGradient branchGradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, secondaryColor),
                new Stop(1, primaryColor.darker())
            );
            gc.setFill(branchGradient);
            
            double branchWidth = width * 0.2;
            double tipX = centerX + offsetX + (i - branchCount/2.0) * (width / branchCount);
            double tipY = baseY - branchHeight;
            
            // Draw branch as tapered shape
            gc.beginPath();
            gc.moveTo(centerX - branchWidth/2, baseY);
            gc.quadraticCurveTo(tipX - branchWidth/4, tipY + branchHeight/2, tipX, tipY);
            gc.quadraticCurveTo(tipX + branchWidth/4, tipY + branchHeight/2, centerX + branchWidth/2, baseY);
            gc.closePath();
            gc.fill();
            gc.stroke();
            
            // Branch tip blob
            double blobSize = branchWidth * 0.8;
            gc.setFill(secondaryColor);
            gc.fillOval(tipX - blobSize/2, tipY - blobSize/2, blobSize, blobSize);
        }
    }
    
    private void renderClusterCoral(GraphicsContext gc) {
        // Cluster of small round corals
        int clusterCount = 4;
        double[] clusterX = { 0.1, 0.5, 0.3, 0.7 };
        double[] clusterY = { 0.3, 0.1, 0.5, 0.4 };
        double[] clusterSize = { 0.4, 0.5, 0.35, 0.3 };
        
        gc.setStroke(primaryColor.darker().darker());
        gc.setLineWidth(1.5);
        
        for (int i = 0; i < clusterCount; i++) {
            double cx = x + clusterX[i] * width;
            double size = clusterSize[i] * Math.min(width, height);
            double cy = y + height - size - clusterY[i] * (height - size);
            
            RadialGradient gradient = new RadialGradient(
                0, 0, 0.3, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, secondaryColor),
                new Stop(1, primaryColor.darker())
            );
            
            gc.setFill(gradient);
            gc.fillOval(cx, cy, size, size);
            gc.strokeOval(cx, cy, size, size);
            
            // Highlight
            gc.setFill(Color.rgb(255, 255, 255, 0.25));
            gc.fillOval(cx + size * 0.15, cy + size * 0.15, size * 0.3, size * 0.25);
        }
    }
    
    @Override
    public double[] getHitbox() {
        // Slightly smaller hitbox for fairness
        double scale = Constants.HITBOX_SCALE;
        double hitW = width * scale;
        double hitH = height * scale;
        double hitX = x + (width - hitW) / 2;
        double hitY = y + (height - hitH) / 2;
        return new double[] { hitX, hitY, hitW, hitH };
    }
    
    /**
     * Check if coral is off-screen to the left.
     */
    public boolean isOffScreen() {
        return x + width < -50;
    }
    
    public CoralType getType() {
        return type;
    }
    
    /**
     * Create a random coral type based on current difficulty.
     * Higher difficulty = more tall/branching corals.
     */
    public static CoralType randomType(double difficultyFactor) {
        double r = MathUtil.randomRange(0, 1);
        
        // Adjust probabilities based on difficulty
        double shortProb = 0.4 - difficultyFactor * 0.15;
        double tallProb = 0.25 + difficultyFactor * 0.1;
        double branchProb = 0.2 + difficultyFactor * 0.05;
        // cluster is remainder
        
        if (r < shortProb) return CoralType.SHORT;
        if (r < shortProb + tallProb) return CoralType.TALL;
        if (r < shortProb + tallProb + branchProb) return CoralType.BRANCHING;
        return CoralType.CLUSTER;
    }
}
