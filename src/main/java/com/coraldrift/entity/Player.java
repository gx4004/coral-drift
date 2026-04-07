package com.coraldrift.entity;

import com.coraldrift.graphics.OctopusRenderer;
import com.coraldrift.util.Constants;
import javafx.scene.canvas.GraphicsContext;

/**
 * The player character - a cute pink octopus.
 * Handles physics, jumping, and animation state.
 */
public class Player extends Entity {
    
    // Physics state
    private double velocityY = 0;
    private boolean onGround = true;
    private boolean isJumping = false;
    
    // Jump feel improvements
    private double coyoteTimer = 0;       // Time since leaving ground
    private double jumpBufferTimer = 0;    // Time since jump was pressed
    private boolean jumpHeld = false;      // Is jump button being held
    
    // Visual
    private final OctopusRenderer renderer;
    
    // State flags
    private boolean isDead = false;
    private double deathFlashTimer = 0;
    private boolean showDeathFlash = false;
    
    // Shield state
    private boolean hasShield = false;
    private double shieldTimer = 0;

    // Expression timer
    private double expressionTimer = 0;
    
    public Player() {
        super(Constants.PLAYER_X, Constants.GROUND_Y - Constants.PLAYER_HEIGHT, 
              Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        renderer = new OctopusRenderer();
    }
    
    @Override
    public void update(double deltaTime) {
        if (isDead) {
            updateDeathAnimation(deltaTime);
            return;
        }
        
        // Update shield timer
        if (hasShield) {
            shieldTimer -= deltaTime;
            if (shieldTimer <= 0) {
                hasShield = false;
                shieldTimer = 0;
            }
        }

        // Expression timer — revert to NORMAL when timer expires
        if (expressionTimer > 0) {
            expressionTimer -= deltaTime;
            if (expressionTimer <= 0) {
                renderer.setExpression(com.coraldrift.graphics.OctopusRenderer.Expression.NORMAL);
            }
        }
        
        // Update jump buffer timer
        if (jumpBufferTimer > 0) {
            jumpBufferTimer -= deltaTime * 1000;
        }
        
        // Update coyote timer
        if (!onGround) {
            coyoteTimer += deltaTime * 1000;
        } else {
            coyoteTimer = 0;
        }
        
        // Check for buffered jump
        if (jumpBufferTimer > 0 && canJump()) {
            executeJump();
            jumpBufferTimer = 0;
        }
        
        // Apply gravity
        if (!onGround) {
            // Apply extra gravity when falling for snappier feel
            double gravityMultiplier = (velocityY > 0) ? Constants.FALL_MULTIPLIER : 1.0;
            
            // Also apply extra gravity if jump released early (variable jump height)
            if (velocityY < 0 && !jumpHeld) {
                gravityMultiplier = Constants.FALL_MULTIPLIER;
            }
            
            velocityY += Constants.GRAVITY * gravityMultiplier * deltaTime;
            velocityY = Math.min(velocityY, Constants.MAX_FALL_SPEED);
        }
        
        // Apply velocity
        y += velocityY * deltaTime;
        
        // Ground collision
        if (y >= Constants.GROUND_Y - height) {
            y = Constants.GROUND_Y - height;
            if (!onGround && velocityY > 100) {
                renderer.onLand();
            }
            velocityY = 0;
            onGround = true;
            isJumping = false;
        } else {
            onGround = false;
        }
        
        // Update renderer
        renderer.update(deltaTime, isJumping, onGround, velocityY);
        renderer.setShielded(hasShield);
    }
    
    private void updateDeathAnimation(double deltaTime) {
        deathFlashTimer += deltaTime;
        // Flash effect
        showDeathFlash = ((int)(deathFlashTimer * 10) % 2) == 0;
    }
    
    /**
     * Request a jump. Handles buffering if not currently able to jump.
     */
    public void jump() {
        if (canJump()) {
            executeJump();
        } else {
            // Buffer the jump input
            jumpBufferTimer = Constants.JUMP_BUFFER_MS;
        }
        jumpHeld = true;
    }
    
    /**
     * Called when jump button is released.
     */
    public void releaseJump() {
        jumpHeld = false;
    }
    
    /**
     * Check if player can currently jump (on ground or within coyote time).
     */
    private boolean canJump() {
        return onGround || coyoteTimer < Constants.COYOTE_TIME_MS;
    }
    
    /**
     * Execute the actual jump.
     */
    private void executeJump() {
        velocityY = Constants.JUMP_VELOCITY;
        onGround = false;
        isJumping = true;
        coyoteTimer = Constants.COYOTE_TIME_MS; // Prevent double-jump
        renderer.onJump();
    }
    
    @Override
    public void render(GraphicsContext gc) {
        if (isDead && !showDeathFlash) {
            return; // Flash effect when dead
        }
        renderer.render(gc, x, y, width, height);
    }
    
    @Override
    public double[] getHitbox() {
        // Return a smaller, more forgiving hitbox
        double scale = Constants.HITBOX_SCALE;
        double hitW = width * scale;
        double hitH = height * scale;
        double hitX = x + (width - hitW) / 2;
        double hitY = y + (height - hitH) / 2;
        return new double[] { hitX, hitY, hitW, hitH };
    }
    
    /**
     * Set glow state for chain combo visual.
     */
    public void setGlowing(boolean glowing) {
        renderer.setGlowing(glowing);
    }
    
    /**
     * Activate shield for specified duration.
     */
    public void activateShield(double duration) {
        hasShield = true;
        shieldTimer = duration;
    }
    
    /**
     * Use shield to block one hit. Returns true if shield was active.
     */
    public boolean useShield() {
        if (hasShield) {
            hasShield = false;
            shieldTimer = 0;
            return true;
        }
        return false;
    }
    
    public boolean hasShield() {
        return hasShield;
    }
    
    public double getShieldTimeRemaining() {
        return shieldTimer;
    }
    
    /**
     * Trigger happy star expression for given duration.
     */
    public void triggerHappyExpression(double duration) {
        expressionTimer = duration;
        renderer.setExpression(com.coraldrift.graphics.OctopusRenderer.Expression.HAPPY_STAR);
    }

    /**
     * Called when player collides with obstacle.
     */
    public void die() {
        isDead = true;
        deathFlashTimer = 0;
        renderer.setExpression(com.coraldrift.graphics.OctopusRenderer.Expression.DEAD);
    }
    
    /**
     * Reset player to initial state.
     */
    @Override
    public void reset() {
        super.reset();
        x = Constants.PLAYER_X;
        y = Constants.GROUND_Y - height;
        velocityY = 0;
        onGround = true;
        isJumping = false;
        coyoteTimer = 0;
        jumpBufferTimer = 0;
        jumpHeld = false;
        isDead = false;
        deathFlashTimer = 0;
        showDeathFlash = false;
        hasShield = false;
        shieldTimer = 0;
        expressionTimer = 0;
        renderer.reset();
    }
    
    // Getters
    public boolean isOnGround() { return onGround; }
    public boolean isJumping() { return isJumping; }
    public boolean isDead() { return isDead; }
    public double getVelocityY() { return velocityY; }
}
