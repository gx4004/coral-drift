package com.coraldrift.core;

import com.coraldrift.util.Constants;
import javafx.animation.AnimationTimer;

/**
 * Fixed-timestep game loop using JavaFX AnimationTimer.
 * Provides consistent physics updates regardless of frame rate.
 */
public class GameLoop extends AnimationTimer {
    
    private final GameLoopCallback callback;
    
    private long lastTime = 0;
    private double accumulator = 0;
    private boolean running = false;
    
    // For FPS tracking (debug)
    private int frameCount = 0;
    private long fpsStartTime = 0;
    private double currentFps = 0;
    
    public interface GameLoopCallback {
        void update(double deltaTime);
        void render();
    }
    
    public GameLoop(GameLoopCallback callback) {
        this.callback = callback;
    }
    
    @Override
    public void handle(long now) {
        if (lastTime == 0) {
            lastTime = now;
            fpsStartTime = now;
            return;
        }
        
        // Calculate delta time in seconds
        double deltaTime = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;
        
        // Cap delta time to prevent spiral of death
        if (deltaTime > 0.1) {
            deltaTime = 0.1;
        }
        
        // Fixed timestep with accumulator
        accumulator += deltaTime;
        
        while (accumulator >= Constants.FRAME_TIME) {
            callback.update(Constants.FRAME_TIME);
            accumulator -= Constants.FRAME_TIME;
        }
        
        // Render at display rate
        callback.render();
        
        // FPS tracking
        frameCount++;
        if (now - fpsStartTime >= 1_000_000_000) {
            currentFps = frameCount;
            frameCount = 0;
            fpsStartTime = now;
        }
    }
    
    @Override
    public void start() {
        lastTime = 0;
        accumulator = 0;
        running = true;
        super.start();
    }
    
    @Override
    public void stop() {
        running = false;
        super.stop();
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public double getCurrentFps() {
        return currentFps;
    }
}
