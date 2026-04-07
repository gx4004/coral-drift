package com.coraldrift.graphics;

import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Particle system for various visual effects:
 * - Heart collection sparkles
 * - Jump/landing effects
 * - Chain combo trail
 */
public class ParticleSystem {
    
    private final List<Particle> particles;
    private final int poolSize;
    
    public ParticleSystem() {
        this.poolSize = Constants.PARTICLE_POOL_SIZE;
        this.particles = new ArrayList<>(poolSize);
    }
    
    /**
     * Update all active particles.
     */
    public void update(double deltaTime) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.lifetime -= deltaTime;
            
            if (p.lifetime <= 0) {
                particles.remove(i);
                continue;
            }
            
            // Physics update
            p.x += p.vx * deltaTime;
            p.y += p.vy * deltaTime;
            p.vy += p.gravity * deltaTime;
            
            // Rotation
            p.rotation += p.rotationSpeed * deltaTime;
            
            // Size decay
            double lifeRatio = p.lifetime / p.maxLifetime;
            p.currentSize = p.size * lifeRatio;
            p.currentAlpha = p.alpha * lifeRatio;
        }
    }
    
    /**
     * Render all active particles.
     */
    public void render(GraphicsContext gc) {
        for (Particle p : particles) {
            renderParticle(gc, p);
        }
    }
    
    private void renderParticle(GraphicsContext gc, Particle p) {
        gc.save();
        gc.setGlobalAlpha(p.currentAlpha);
        
        gc.translate(p.x, p.y);
        gc.rotate(p.rotation);
        
        switch (p.type) {
            case SPARKLE:
                renderSparkle(gc, p);
                break;
            case CIRCLE:
                renderCircle(gc, p);
                break;
            case STAR:
                renderStar(gc, p);
                break;
            case HEART_MINI:
                renderMiniHeart(gc, p);
                break;
        }
        
        gc.restore();
    }
    
    private void renderSparkle(GraphicsContext gc, Particle p) {
        double size = p.currentSize;
        gc.setFill(p.color);
        
        // 4-point star sparkle
        gc.beginPath();
        gc.moveTo(0, -size);
        gc.lineTo(size * 0.2, -size * 0.2);
        gc.lineTo(size, 0);
        gc.lineTo(size * 0.2, size * 0.2);
        gc.lineTo(0, size);
        gc.lineTo(-size * 0.2, size * 0.2);
        gc.lineTo(-size, 0);
        gc.lineTo(-size * 0.2, -size * 0.2);
        gc.closePath();
        gc.fill();
    }
    
    private void renderCircle(GraphicsContext gc, Particle p) {
        double size = p.currentSize;
        gc.setFill(p.color);
        gc.fillOval(-size/2, -size/2, size, size);
    }
    
    private void renderStar(GraphicsContext gc, Particle p) {
        double size = p.currentSize;
        int points = 5;
        gc.setFill(p.color);
        
        gc.beginPath();
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI * i / points - Math.PI / 2;
            double r = (i % 2 == 0) ? size : size * 0.4;
            double sx = Math.cos(angle) * r;
            double sy = Math.sin(angle) * r;
            if (i == 0) gc.moveTo(sx, sy);
            else gc.lineTo(sx, sy);
        }
        gc.closePath();
        gc.fill();
    }
    
    private void renderMiniHeart(GraphicsContext gc, Particle p) {
        double size = p.currentSize;
        gc.setFill(p.color);
        
        // Simple heart shape
        gc.beginPath();
        gc.moveTo(0, size * 0.3);
        gc.bezierCurveTo(-size * 0.5, -size * 0.2, -size * 0.5, -size * 0.5, 0, -size * 0.2);
        gc.bezierCurveTo(size * 0.5, -size * 0.5, size * 0.5, -size * 0.2, 0, size * 0.3);
        gc.closePath();
        gc.fill();
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // EFFECT SPAWNERS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Spawn sparkles when collecting a heart.
     */
    public void spawnHeartCollectEffect(double x, double y) {
        int count = Constants.SPARKLES_PER_HEART;
        
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.type = MathUtil.randomChance(0.5) ? ParticleType.SPARKLE : ParticleType.HEART_MINI;
            p.x = x;
            p.y = y;
            p.size = MathUtil.randomRange(6, 14);
            p.currentSize = p.size;
            
            // Burst outward
            double angle = MathUtil.randomRange(0, Math.PI * 2);
            double speed = MathUtil.randomRange(100, 250);
            p.vx = Math.cos(angle) * speed;
            p.vy = Math.sin(angle) * speed - 50; // Slight upward bias
            p.gravity = MathUtil.randomRange(100, 200);
            
            p.rotation = MathUtil.randomRange(0, 360);
            p.rotationSpeed = MathUtil.randomRange(-180, 180);
            
            p.maxLifetime = Constants.SPARKLE_LIFETIME;
            p.lifetime = p.maxLifetime;
            p.alpha = 1.0;
            p.currentAlpha = 1.0;
            
            // Gold/pink colors
            p.color = MathUtil.randomChance(0.6) ? Constants.SPARKLE_GOLD : Constants.HEART_PINK;
            
            particles.add(p);
        }
    }
    
    /**
     * Spawn landing dust/splash effect.
     */
    public void spawnLandingEffect(double x, double y) {
        int count = 8;
        
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.type = ParticleType.CIRCLE;
            p.x = x + MathUtil.randomRange(-20, 20);
            p.y = y;
            p.size = MathUtil.randomRange(4, 10);
            p.currentSize = p.size;
            
            // Spread outward and up
            double angle = MathUtil.randomRange(-Math.PI * 0.8, -Math.PI * 0.2);
            double speed = MathUtil.randomRange(80, 180);
            p.vx = Math.cos(angle) * speed * (i % 2 == 0 ? 1 : -1);
            p.vy = Math.sin(angle) * speed;
            p.gravity = 300;
            
            p.rotation = 0;
            p.rotationSpeed = 0;
            
            p.maxLifetime = 0.4;
            p.lifetime = p.maxLifetime;
            p.alpha = 0.6;
            p.currentAlpha = p.alpha;
            
            p.color = Color.rgb(180, 200, 220, 0.7);
            
            particles.add(p);
        }
    }
    
    /**
     * Spawn chain combo trail sparkle.
     */
    public void spawnChainTrailParticle(double x, double y, int chainLevel) {
        Particle p = new Particle();
        p.type = ParticleType.STAR;
        p.x = x + MathUtil.randomRange(-10, 10);
        p.y = y + MathUtil.randomRange(-10, 10);
        p.size = 5 + chainLevel * 0.5;
        p.currentSize = p.size;
        
        p.vx = MathUtil.randomRange(-30, 30);
        p.vy = MathUtil.randomRange(-50, -20);
        p.gravity = 50;
        
        p.rotation = MathUtil.randomRange(0, 360);
        p.rotationSpeed = MathUtil.randomRange(-90, 90);
        
        p.maxLifetime = 0.5;
        p.lifetime = p.maxLifetime;
        p.alpha = 0.9;
        p.currentAlpha = p.alpha;
        
        // Color intensifies with chain
        double hue = 45 + chainLevel * 5; // Gold shifting toward orange
        p.color = Color.hsb(hue, 0.9, 1.0);
        
        particles.add(p);
    }
    
    /**
     * Spawn collision effect.
     */
    public void spawnHitEffect(double x, double y) {
        int count = 15;
        
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.type = ParticleType.CIRCLE;
            p.x = x;
            p.y = y;
            p.size = MathUtil.randomRange(3, 8);
            p.currentSize = p.size;
            
            double angle = MathUtil.randomRange(0, Math.PI * 2);
            double speed = MathUtil.randomRange(150, 350);
            p.vx = Math.cos(angle) * speed;
            p.vy = Math.sin(angle) * speed;
            p.gravity = 200;
            
            p.rotation = 0;
            p.rotationSpeed = 0;
            
            p.maxLifetime = 0.5;
            p.lifetime = p.maxLifetime;
            p.alpha = 1.0;
            p.currentAlpha = p.alpha;
            
            p.color = MathUtil.randomChance(0.5) ? 
                Constants.CORAL_PINK : Constants.OCTOPUS_PINK_LIGHT;
            
            particles.add(p);
        }
    }
    
    /**
     * Teal/cyan sparks for near-miss moments.
     */
    public void spawnNearMissEffect(double x, double y) {
        for (int i = 0; i < 8; i++) {
            Particle p = new Particle();
            p.type = ParticleType.SPARKLE;
            p.x = x + MathUtil.randomRange(-12, 12);
            p.y = y + MathUtil.randomRange(-12, 12);
            p.size = MathUtil.randomRange(4, 9);
            p.currentSize = p.size;
            double angle = MathUtil.randomRange(0, Math.PI * 2);
            double speed = MathUtil.randomRange(90, 200);
            p.vx = Math.cos(angle) * speed;
            p.vy = Math.sin(angle) * speed - 40;
            p.gravity = 150;
            p.rotation = MathUtil.randomRange(0, 360);
            p.rotationSpeed = MathUtil.randomRange(-120, 120);
            p.maxLifetime = 0.4;
            p.lifetime = p.maxLifetime;
            p.alpha = 1.0;
            p.currentAlpha = 1.0;
            p.color = Color.hsb(MathUtil.randomRange(170, 200), 0.85, 1.0);
            particles.add(p);
        }
    }

    /**
     * Gold explosion for golden heart collection.
     */
    public void spawnGoldenHeartEffect(double x, double y) {
        for (int i = 0; i < 22; i++) {
            Particle p = new Particle();
            boolean isStar = MathUtil.randomChance(0.4);
            boolean isHeart = !isStar && MathUtil.randomChance(0.3);
            p.type = isStar ? ParticleType.STAR : (isHeart ? ParticleType.HEART_MINI : ParticleType.SPARKLE);
            p.x = x;
            p.y = y;
            p.size = MathUtil.randomRange(8, 18);
            p.currentSize = p.size;
            double angle = MathUtil.randomRange(0, Math.PI * 2);
            double speed = MathUtil.randomRange(150, 420);
            p.vx = Math.cos(angle) * speed;
            p.vy = Math.sin(angle) * speed - 80;
            p.gravity = MathUtil.randomRange(100, 240);
            p.rotation = MathUtil.randomRange(0, 360);
            p.rotationSpeed = MathUtil.randomRange(-200, 200);
            p.maxLifetime = 0.8;
            p.lifetime = p.maxLifetime;
            p.alpha = 1.0;
            p.currentAlpha = 1.0;
            p.color = Color.hsb(MathUtil.randomRange(38, 55), 0.95, 1.0);
            particles.add(p);
        }
    }

    public void clear() {
        particles.clear();
    }
    
    public int getActiveCount() {
        return particles.size();
    }
    
    // Inner classes
    
    private enum ParticleType {
        SPARKLE, CIRCLE, STAR, HEART_MINI
    }
    
    private static class Particle {
        ParticleType type;
        double x, y;
        double vx, vy;
        double gravity;
        double size, currentSize;
        double rotation, rotationSpeed;
        double lifetime, maxLifetime;
        double alpha, currentAlpha;
        Color color;
    }
}
