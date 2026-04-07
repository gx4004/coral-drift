package com.coraldrift.graphics;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-layered parallax scrolling background.
 * Creates a dreamy underwater atmosphere with distant reefs, sea plants, and light rays.
 */
public class ParallaxBackground {
    
    // Background elements
    private final List<BackgroundElement>[] layers;
    private final double[] layerOffsets;
    private double scrollPosition = 0;

    // Falling stars (bioluminescent motes)
    private final List<StarMote> starMotes;

    // Light rays
    private final List<LightRay> lightRays;

    // Ocean floor
    private final List<Double> floorPoints;
    
    @SuppressWarnings("unchecked")
    public ParallaxBackground() {
        // Initialize layers
        int numLayers = Constants.PARALLAX_SPEEDS.length;
        layers = new ArrayList[numLayers];
        layerOffsets = new double[numLayers];
        
        for (int i = 0; i < numLayers; i++) {
            layers[i] = new ArrayList<>();
            layerOffsets[i] = 0;
            generateLayerElements(i);
        }
        
        // Initialize star motes
        starMotes = new ArrayList<>();
        generateStarMotes();

        // Initialize light rays
        lightRays = new ArrayList<>();
        generateLightRays();
        
        // Generate ocean floor points
        floorPoints = new ArrayList<>();
        generateFloorPoints();
    }
    
    private void generateLayerElements(int layerIndex) {
        double spacing;
        int count;
        
        switch (layerIndex) {
            case 0: // Deepest - distant rocks/reefs
                spacing = 400;
                count = 8;
                for (int i = 0; i < count; i++) {
                    layers[layerIndex].add(new BackgroundElement(
                        i * spacing + MathUtil.randomRange(-50, 50),
                        MathUtil.randomRange(400, 550),
                        MathUtil.randomRange(80, 200),
                        MathUtil.randomRange(100, 250),
                        ElementType.DISTANT_REEF
                    ));
                }
                break;
                
            case 1: // Mid-distant - large sea plants
                spacing = 300;
                count = 10;
                for (int i = 0; i < count; i++) {
                    layers[layerIndex].add(new BackgroundElement(
                        i * spacing + MathUtil.randomRange(-30, 30),
                        Constants.GROUND_Y - MathUtil.randomRange(50, 150),
                        MathUtil.randomRange(40, 80),
                        MathUtil.randomRange(100, 200),
                        ElementType.SEAWEED_TALL
                    ));
                }
                break;
                
            case 2: // Mid-near - medium plants
                spacing = 200;
                count = 15;
                for (int i = 0; i < count; i++) {
                    layers[layerIndex].add(new BackgroundElement(
                        i * spacing + MathUtil.randomRange(-40, 40),
                        Constants.GROUND_Y - MathUtil.randomRange(20, 80),
                        MathUtil.randomRange(30, 60),
                        MathUtil.randomRange(60, 120),
                        MathUtil.randomChance(0.5) ? ElementType.SEAWEED_SHORT : ElementType.SEA_PLANT
                    ));
                }
                break;
                
            case 3: // Nearest - small foreground plants
                spacing = 150;
                count = 18;
                for (int i = 0; i < count; i++) {
                    layers[layerIndex].add(new BackgroundElement(
                        i * spacing + MathUtil.randomRange(-20, 20),
                        Constants.GROUND_Y - MathUtil.randomRange(10, 40),
                        MathUtil.randomRange(20, 40),
                        MathUtil.randomRange(30, 70),
                        ElementType.SMALL_PLANT
                    ));
                }
                break;
        }
    }
    
    private void generateStarMotes() {
        for (int i = 0; i < 40; i++) {
            StarMote s = new StarMote();
            s.x = MathUtil.randomRange(0, Constants.WINDOW_WIDTH);
            s.y = MathUtil.randomRange(0, Constants.GROUND_Y);
            s.size = MathUtil.randomRange(1.5, 4.0);
            s.fallSpeed = MathUtil.randomRange(15, 50);
            s.driftX = MathUtil.randomRange(-15, 15);
            s.twinklePhase = MathUtil.randomRange(0, Math.PI * 2);
            s.twinkleSpeed = MathUtil.randomRange(1.5, 4.0);
            // Mostly white/cyan stars, occasionally a warm gold
            double roll = Math.random();
            if (roll < 0.5) {
                s.r = 180; s.g = 230; s.b = 255; // cool blue-white
            } else if (roll < 0.8) {
                s.r = 255; s.g = 255; s.b = 255; // pure white
            } else {
                s.r = 255; s.g = 220; s.b = 100; // warm gold
            }
            starMotes.add(s);
        }
    }

    private void generateLightRays() {
        int rayCount = 6;
        for (int i = 0; i < rayCount; i++) {
            lightRays.add(new LightRay(
                MathUtil.randomRange(0, Constants.WINDOW_WIDTH * 2),
                MathUtil.randomRange(50, 200),
                MathUtil.randomRange(0.03, 0.1)
            ));
        }
    }
    
    private void generateFloorPoints() {
        double x = 0;
        while (x < Constants.WINDOW_WIDTH * 3) {
            floorPoints.add(x);
            floorPoints.add(Constants.GROUND_Y + MathUtil.randomRange(-5, 15));
            x += MathUtil.randomRange(30, 80);
        }
    }
    
    /**
     * Update background scroll and animations.
     */
    public void update(double deltaTime, double scrollSpeed) {
        scrollPosition += scrollSpeed * deltaTime;
        
        // Update layer offsets
        for (int i = 0; i < layers.length; i++) {
            layerOffsets[i] += scrollSpeed * Constants.PARALLAX_SPEEDS[i] * deltaTime;
            
            // Wrap elements that go off-screen
            for (BackgroundElement elem : layers[i]) {
                double effectiveX = elem.x - layerOffsets[i];
                if (effectiveX + elem.width < -100) {
                    // Reset to right side
                    elem.x += Constants.WINDOW_WIDTH + 500 + MathUtil.randomRange(0, 200);
                }
            }
        }
        
        // Update star motes
        for (StarMote s : starMotes) {
            s.y += s.fallSpeed * deltaTime;
            s.x += s.driftX * deltaTime - scrollSpeed * 0.05 * deltaTime;
            s.twinklePhase += s.twinkleSpeed * deltaTime;
            // Wrap when off-screen
            if (s.y > Constants.GROUND_Y) s.y = MathUtil.randomRange(-20, 0);
            if (s.x < -10) s.x = Constants.WINDOW_WIDTH + MathUtil.randomRange(0, 50);
            if (s.x > Constants.WINDOW_WIDTH + 10) s.x = MathUtil.randomRange(-50, 0);
        }

        // Update light ray positions
        for (LightRay ray : lightRays) {
            ray.x -= scrollSpeed * 0.05 * deltaTime;
            if (ray.x + ray.width < 0) {
                ray.x = Constants.WINDOW_WIDTH + MathUtil.randomRange(0, 200);
            }
            ray.animTime += deltaTime;
        }
    }
    
    /**
     * Render all background layers.
     */
    public void render(GraphicsContext gc) {
        // Ocean gradient background
        renderOceanGradient(gc);
        
        // Falling star motes (bioluminescent particles)
        renderStarMotes(gc);

        // Light rays (behind everything)
        renderLightRays(gc);
        
        // Render parallax layers from back to front
        for (int i = 0; i < layers.length; i++) {
            renderLayer(gc, i);
        }
        
        // Ocean floor
        renderOceanFloor(gc);
    }
    
    private void renderOceanGradient(GraphicsContext gc) {
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Constants.OCEAN_SURFACE),
            new Stop(0.3, Constants.OCEAN_LIGHT),
            new Stop(0.6, Constants.OCEAN_MID),
            new Stop(1.0, Constants.OCEAN_DEEP)
        );
        gc.setFill(gradient);
        gc.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }
    
    private void renderLightRays(GraphicsContext gc) {
        for (LightRay ray : lightRays) {
            double alpha = ray.baseAlpha * (0.7 + 0.3 * Math.sin(ray.animTime * 0.5));
            
            // Gradient from top (brighter) to bottom (transparent)
            LinearGradient rayGradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 200, alpha)),
                new Stop(0.5, Color.rgb(255, 255, 180, alpha * 0.5)),
                new Stop(1, Color.TRANSPARENT)
            );
            
            gc.setFill(rayGradient);
            
            // Draw angled ray
            double topWidth = ray.width;
            double bottomWidth = ray.width * 2.5;
            double rayHeight = Constants.WINDOW_HEIGHT * 0.8;
            
            gc.beginPath();
            gc.moveTo(ray.x - topWidth/2, 0);
            gc.lineTo(ray.x + topWidth/2, 0);
            gc.lineTo(ray.x + bottomWidth/2, rayHeight);
            gc.lineTo(ray.x - bottomWidth/2, rayHeight);
            gc.closePath();
            gc.fill();
        }
    }
    
    private void renderLayer(GraphicsContext gc, int layerIndex) {
        double offset = layerOffsets[layerIndex];
        double depthFactor = 1.0 - layerIndex * 0.2; // Darker = further back
        
        for (BackgroundElement elem : layers[layerIndex]) {
            double drawX = elem.x - offset;
            
            // Skip if off-screen
            if (drawX > Constants.WINDOW_WIDTH + 100 || drawX + elem.width < -100) {
                continue;
            }
            
            renderElement(gc, elem, drawX, depthFactor, layerIndex);
        }
    }
    
    private void renderElement(GraphicsContext gc, BackgroundElement elem, double x, double depthFactor, int layer) {
        gc.save();
        gc.setGlobalAlpha(0.3 + depthFactor * 0.5);
        
        switch (elem.type) {
            case DISTANT_REEF:
                renderDistantReef(gc, x, elem.y, elem.width, elem.height, depthFactor);
                break;
            case SEAWEED_TALL:
            case SEAWEED_SHORT:
                renderSeaweed(gc, x, elem.y, elem.width, elem.height, depthFactor, layer);
                break;
            case SEA_PLANT:
                renderSeaPlant(gc, x, elem.y, elem.width, elem.height, depthFactor);
                break;
            case SMALL_PLANT:
                renderSmallPlant(gc, x, elem.y, elem.width, elem.height, depthFactor);
                break;
        }
        
        gc.restore();
    }
    
    private void renderDistantReef(GraphicsContext gc, double x, double y, double w, double h, double depth) {
        Color baseColor = Color.web("#1a3a5c").interpolate(Constants.OCEAN_DEEP, 1 - depth);
        
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, baseColor.brighter()),
            new Stop(1, baseColor.darker())
        );
        gc.setFill(gradient);
        
        // Draw organic reef shape
        gc.beginPath();
        gc.moveTo(x, y + h);
        gc.quadraticCurveTo(x + w * 0.2, y + h * 0.3, x + w * 0.3, y);
        gc.quadraticCurveTo(x + w * 0.5, y - h * 0.1, x + w * 0.7, y + h * 0.2);
        gc.quadraticCurveTo(x + w * 0.9, y + h * 0.5, x + w, y + h);
        gc.closePath();
        gc.fill();
    }
    
    private void renderSeaweed(GraphicsContext gc, double x, double y, double w, double h, double depth, int layer) {
        Color baseColor = Color.web("#2d5a3d").interpolate(Color.web("#1a4a6d"), depth * 0.5);
        
        // Swaying animation
        double sway = Math.sin(scrollPosition * 0.002 + x * 0.01 + layer) * 10 * (1 - depth * 0.5);
        
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, baseColor.brighter()),
            new Stop(1, baseColor)
        );
        gc.setFill(gradient);
        
        // Draw wavy seaweed strand
        gc.beginPath();
        gc.moveTo(x, y + h);
        gc.bezierCurveTo(
            x + sway * 0.5, y + h * 0.7,
            x - sway * 0.3, y + h * 0.4,
            x + sway, y
        );
        gc.bezierCurveTo(
            x + w + sway, y,
            x + w - sway * 0.3, y + h * 0.4,
            x + w + sway * 0.5, y + h * 0.7
        );
        gc.lineTo(x + w, y + h);
        gc.closePath();
        gc.fill();
    }
    
    private void renderSeaPlant(GraphicsContext gc, double x, double y, double w, double h, double depth) {
        Color leafColor = Color.web("#4a7c59").interpolate(Constants.OCEAN_LIGHT, 0.3);
        gc.setFill(leafColor);
        
        // Multiple leaf shapes
        int leaves = 3;
        for (int i = 0; i < leaves; i++) {
            double leafX = x + i * w / leaves;
            double leafW = w / 2;
            double leafH = h * (0.6 + i * 0.15);
            double angle = (i - 1) * 15;
            
            gc.save();
            gc.translate(leafX + leafW/2, y + h);
            gc.rotate(angle);
            gc.fillOval(-leafW/2, -leafH, leafW, leafH);
            gc.restore();
        }
    }
    
    private void renderSmallPlant(GraphicsContext gc, double x, double y, double w, double h, double depth) {
        Color plantColor = Constants.TEAL_DARK.interpolate(Constants.OCEAN_LIGHT, 0.4);
        gc.setFill(plantColor);
        
        // Simple small plant
        gc.fillOval(x, y, w, h);
        gc.fillOval(x + w * 0.3, y - h * 0.3, w * 0.7, h * 0.8);
    }
    
    private void renderStarMotes(GraphicsContext gc) {
        gc.save();
        for (StarMote s : starMotes) {
            double alpha = 0.25 + 0.45 * (0.5 + 0.5 * Math.sin(s.twinklePhase));
            gc.setGlobalAlpha(alpha);

            // Soft glow halo
            RadialGradient halo = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(s.r, s.g, s.b, 0.7)),
                new Stop(1, Color.TRANSPARENT)
            );
            gc.setFill(halo);
            double haloR = s.size * 2.5;
            gc.fillOval(s.x - haloR, s.y - haloR, haloR * 2, haloR * 2);

            // Core dot
            gc.setFill(Color.rgb(s.r, s.g, s.b));
            gc.fillOval(s.x - s.size / 2, s.y - s.size / 2, s.size, s.size);

            // Cross sparkle lines
            gc.setStroke(Color.rgb(s.r, s.g, s.b, 0.5));
            gc.setLineWidth(0.8);
            gc.strokeLine(s.x - s.size * 1.5, s.y, s.x + s.size * 1.5, s.y);
            gc.strokeLine(s.x, s.y - s.size * 1.5, s.x, s.y + s.size * 1.5);
        }
        gc.restore();
    }

    private void renderOceanFloor(GraphicsContext gc) {
        // Rich sandy gradient floor with multiple color stops
        LinearGradient floorGradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#4a6b7d")),
            new Stop(0.15, Color.web("#3d5a6b")),
            new Stop(0.4, Color.web("#2a4a5a")),
            new Stop(0.7, Color.web("#1a3a4a")),
            new Stop(1, Constants.OCEAN_DEEP)
        );
        
        gc.setFill(floorGradient);
        gc.beginPath();
        gc.moveTo(0, Constants.GROUND_Y);
        
        // Slightly wavy floor line
        for (int i = 0; i < floorPoints.size(); i += 2) {
            double fx = floorPoints.get(i) - (scrollPosition * 0.3) % (Constants.WINDOW_WIDTH * 3);
            double fy = floorPoints.get(i + 1);
            if (i == 0) {
                gc.moveTo(fx, fy);
            } else {
                gc.lineTo(fx, fy);
            }
        }
        
        gc.lineTo(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        gc.lineTo(0, Constants.WINDOW_HEIGHT);
        gc.closePath();
        gc.fill();
        
        // Draw decorative sand ripples
        gc.setStroke(Color.web("#5a7a8a", 0.25));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 8; i++) {
            double rippleY = Constants.GROUND_Y + 20 + i * 18;
            double rippleOffset = (scrollPosition * 0.1 + i * 30) % 80;
            gc.beginPath();
            for (double rx = -rippleOffset; rx < Constants.WINDOW_WIDTH + 40; rx += 40) {
                if (rx == -rippleOffset) {
                    gc.moveTo(rx, rippleY);
                }
                gc.quadraticCurveTo(rx + 10, rippleY - 3, rx + 20, rippleY);
                gc.quadraticCurveTo(rx + 30, rippleY + 3, rx + 40, rippleY);
            }
            gc.stroke();
        }
        
        // Subtle sparkle dots on floor (like sand glitter)
        gc.setFill(Color.web("#7a9aaa", 0.4));
        double sparkleTime = scrollPosition * 0.05;
        for (int i = 0; i < 25; i++) {
            double sx = ((i * 73 + sparkleTime) % Constants.WINDOW_WIDTH);
            double sy = Constants.GROUND_Y + 10 + (i * 17) % 80;
            double sparkleSize = 1.5 + Math.sin(sparkleTime + i) * 0.5;
            gc.fillOval(sx, sy, sparkleSize, sparkleSize);
        }
        
        // Top edge gradient highlight (soft glow line)
        LinearGradient edgeGlow = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#8abacc", 0.5)),
            new Stop(0.5, Color.web("#6a9aaa", 0.3)),
            new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(edgeGlow);
        gc.fillRect(0, Constants.GROUND_Y - 2, Constants.WINDOW_WIDTH, 8);
    }
    
    public void reset() {
        scrollPosition = 0;
        for (int i = 0; i < layerOffsets.length; i++) {
            layerOffsets[i] = 0;
        }
        starMotes.clear();
        generateStarMotes();
    }
    
    // Inner classes for background elements
    
    private enum ElementType {
        DISTANT_REEF, SEAWEED_TALL, SEAWEED_SHORT, SEA_PLANT, SMALL_PLANT
    }
    
    private static class BackgroundElement {
        double x, y, width, height;
        ElementType type;
        
        BackgroundElement(double x, double y, double width, double height, ElementType type) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
        }
    }
    
    private static class LightRay {
        double x, width, baseAlpha;
        double animTime = 0;

        LightRay(double x, double width, double baseAlpha) {
            this.x = x;
            this.width = width;
            this.baseAlpha = baseAlpha;
        }
    }

    private static class StarMote {
        double x, y, size;
        double fallSpeed, driftX;
        double twinklePhase, twinkleSpeed;
        int r, g, b;
    }
}
