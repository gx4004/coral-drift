package com.coraldrift.entity;

import javafx.scene.canvas.GraphicsContext;

/**
 * Base class for all game entities (player, obstacles, collectibles).
 */
public abstract class Entity {
    
    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected boolean active = true;
    
    public Entity(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Update entity state.
     * @param deltaTime Time since last frame in seconds
     */
    public abstract void update(double deltaTime);
    
    /**
     * Render the entity.
     * @param gc Graphics context to draw on
     */
    public abstract void render(GraphicsContext gc);
    
    /**
     * Get the hitbox for collision detection.
     * By default returns the full bounds, but subclasses can override
     * to provide more forgiving hitboxes.
     */
    public double[] getHitbox() {
        return new double[] { x, y, width, height };
    }
    
    /**
     * Check if this entity intersects with another using AABB collision.
     */
    public boolean intersects(Entity other) {
        double[] a = this.getHitbox();
        double[] b = other.getHitbox();
        
        return a[0] < b[0] + b[2] &&
               a[0] + a[2] > b[0] &&
               a[1] < b[1] + b[3] &&
               a[1] + a[3] > b[1];
    }
    
    /**
     * Check if a point is inside this entity.
     */
    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    // Getters and setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    
    public double getCenterX() { return x + width / 2; }
    public double getCenterY() { return y + height / 2; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    /**
     * Called when entity is about to be removed/recycled.
     */
    public void reset() {
        active = true;
    }
}
