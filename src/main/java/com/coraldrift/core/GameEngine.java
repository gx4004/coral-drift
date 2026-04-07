package com.coraldrift.core;

import com.coraldrift.audio.AudioManager;
import com.coraldrift.entity.CoralObstacle;
import com.coraldrift.entity.HeartCollectible;
import com.coraldrift.entity.Player;
import com.coraldrift.graphics.*;
import com.coraldrift.spawner.HeartSpawner;
import com.coraldrift.spawner.ObstacleSpawner;
import com.coraldrift.util.Constants;
import com.coraldrift.util.SaveManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * Main game engine that orchestrates all gameplay systems.
 */
public class GameEngine implements CollisionSystem.HeartCollectCallback {
    
    // Core systems
    private final GameState state;
    private final CollisionSystem collisionSystem;
    
    // Entities
    private final Player player;
    
    // Spawners
    private final ObstacleSpawner obstacleSpawner;
    private final HeartSpawner heartSpawner;
    
    // Graphics systems
    private final ParallaxBackground background;
    private final BubbleEmitter bubbleEmitter;
    private final ParticleSystem particleSystem;
    private final ScreenEffects screenEffects;
    
    // Chain trail timing
    private double chainTrailTimer = 0;
    private static final double CHAIN_TRAIL_INTERVAL = 0.05;
    
    // Landing detection
    private boolean wasInAir = false;
    
    public GameEngine() {
        state = new GameState(Constants.BASE_SCROLL_SPEED);
        collisionSystem = new CollisionSystem();
        
        player = new Player();
        
        obstacleSpawner = new ObstacleSpawner();
        heartSpawner = new HeartSpawner();
        
        background = new ParallaxBackground();
        bubbleEmitter = new BubbleEmitter();
        particleSystem = new ParticleSystem();
        screenEffects = new ScreenEffects();
        
        // Load best scores
        state.setBestScores(
            SaveManager.getInstance().getBestScore(),
            SaveManager.getInstance().getBestHearts()
        );
    }
    
    /**
     * Update game logic.
     */
    public void update(double deltaTime) {
        // Always update visual effects
        screenEffects.update(deltaTime);
        bubbleEmitter.update(deltaTime);
        particleSystem.update(deltaTime);
        
        if (!state.isPlaying()) {
            // Still update background for menu ambiance
            background.update(deltaTime, Constants.BASE_SCROLL_SPEED * 0.3);
            return;
        }
        
        // Update game state
        state.update(deltaTime);
        state.addDistanceScore(deltaTime, Constants.SCORE_PER_SECOND);
        
        // Update scroll speed based on difficulty
        state.updateScrollSpeed(
            Constants.BASE_SCROLL_SPEED,
            Constants.MAX_SCROLL_SPEED,
            Constants.SPEED_INCREMENT,
            Constants.SPEED_INCREASE_INTERVAL
        );
        
        double scrollSpeed = state.getScrollSpeed();
        double scrollAmount = scrollSpeed * deltaTime;
        
        // Update background
        background.update(deltaTime, scrollSpeed);
        
        // Track if player was in air (for landing effect)
        boolean inAirNow = !player.isOnGround();
        
        // Update player
        player.update(deltaTime);
        
        // Landing detection
        if (wasInAir && player.isOnGround()) {
            particleSystem.spawnLandingEffect(player.getCenterX(), Constants.GROUND_Y);
            bubbleEmitter.burstAt(player.getCenterX(), Constants.GROUND_Y, 4);
        }
        wasInAir = inAirNow;
        
        // Update spawners
        obstacleSpawner.update(scrollAmount, state.getDifficultyFactor());
        heartSpawner.update(scrollAmount, obstacleSpawner, state.getDifficultyFactor());
        
        // Chain glow effect
        player.setGlowing(state.getChainCount() >= 5);
        
        // Spawn chain trail particles
        if (state.isChainActive()) {
            chainTrailTimer += deltaTime;
            if (chainTrailTimer >= CHAIN_TRAIL_INTERVAL) {
                chainTrailTimer = 0;
                particleSystem.spawnChainTrailParticle(
                    player.getX() + player.getWidth() * 0.3,
                    player.getCenterY(),
                    state.getChainCount()
                );
            }
        }
        
        // Collision detection
        checkCollisions();
    }
    
    private void checkCollisions() {
        // Check coral collision
        CoralObstacle hitCoral = collisionSystem.checkCoralCollision(
            player, obstacleSpawner.getCorals()
        );
        
        if (hitCoral != null) {
            handleCoralHit(hitCoral);
            return;
        }
        
        // Check heart collection
        collisionSystem.checkHeartCollisions(
            player, heartSpawner.getHearts(), this
        );
    }
    
    private void handleCoralHit(CoralObstacle coral) {
        player.die();
        state.breakChain();
        state.setStatus(GameState.Status.GAME_OVER);
        
        // Effects
        screenEffects.triggerHitEffect();
        particleSystem.spawnHitEffect(player.getCenterX(), player.getCenterY());
        AudioManager.getInstance().playHit();
        AudioManager.getInstance().playGameOver();
        
        // Finalize and save
        state.finalizeScore();
        SaveManager.getInstance().updateBestScore(state.getScore());
        SaveManager.getInstance().updateBestHearts(state.getHeartsCollected());
    }
    
    @Override
    public void onHeartCollected(HeartCollectible heart) {
        heart.collect();
        state.collectHeart(Constants.HEART_SCORE_VALUE);
        
        // Effects
        screenEffects.triggerCollectEffect();
        particleSystem.spawnHeartCollectEffect(heart.getCenterX(), heart.getCenterY());
        bubbleEmitter.burstAt(heart.getCenterX(), heart.getCenterY(), 5);
        AudioManager.getInstance().playCollect();
        AudioManager.getInstance().playChainCombo(state.getChainCount());
    }
    
    /**
     * Render the game.
     */
    public void render(GraphicsContext gc) {
        gc.save();
        
        // Apply screen effects (shake, etc.)
        screenEffects.applyPreRender(gc);
        
        // Background layers
        background.render(gc);
        
        // Bubbles (behind entities)
        bubbleEmitter.render(gc);
        
        // Obstacles
        for (CoralObstacle coral : obstacleSpawner.getCorals()) {
            coral.render(gc);
        }
        
        // Hearts
        for (HeartCollectible heart : heartSpawner.getHearts()) {
            heart.render(gc);
        }
        
        // Particles (behind player)
        particleSystem.render(gc);
        
        // Player
        player.render(gc);
        
        gc.restore();
        
        // Screen overlay effects (flash, etc.)
        screenEffects.renderOverlay(gc);
    }
    
    /**
     * Handle jump input.
     */
    public void jump() {
        if (state.isPlaying() && !player.isDead()) {
            player.jump();
            AudioManager.getInstance().playJump();
        }
    }
    
    /**
     * Handle jump release.
     */
    public void releaseJump() {
        player.releaseJump();
    }
    
    /**
     * Start a new game.
     */
    public void startGame() {
        player.reset();
        obstacleSpawner.reset();
        heartSpawner.reset();
        background.reset();
        particleSystem.clear();
        screenEffects.reset();
        
        state.reset(Constants.BASE_SCROLL_SPEED);
        state.setBestScores(
            SaveManager.getInstance().getBestScore(),
            SaveManager.getInstance().getBestHearts()
        );
        
        wasInAir = false;
        chainTrailTimer = 0;
    }
    
    /**
     * Pause the game.
     */
    public void pause() {
        if (state.isPlaying()) {
            state.setStatus(GameState.Status.PAUSED);
        }
    }
    
    /**
     * Resume the game.
     */
    public void resume() {
        if (state.isPaused()) {
            state.setStatus(GameState.Status.PLAYING);
        }
    }
    
    /**
     * Toggle pause state.
     */
    public void togglePause() {
        if (state.isPlaying()) {
            pause();
        } else if (state.isPaused()) {
            resume();
        }
    }
    
    // Getters
    public GameState getState() { return state; }
    public Player getPlayer() { return player; }
    public ParticleSystem getParticleSystem() { return particleSystem; }
    public ScreenEffects getScreenEffects() { return screenEffects; }
}
