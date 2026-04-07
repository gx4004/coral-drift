package com.coraldrift.util;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Central configuration for all game tuning values.
 * Modify these to adjust gameplay feel, difficulty, and visuals.
 */
public final class Constants {
    
    private Constants() {} // Prevent instantiation
    
    // ═══════════════════════════════════════════════════════════════════════════
    // WINDOW & DISPLAY
    // ═══════════════════════════════════════════════════════════════════════════
    public static final double WINDOW_WIDTH = 1280;
    public static final double WINDOW_HEIGHT = 720;
    public static final String GAME_TITLE = "Coral Drift";
    public static final int TARGET_FPS = 60;
    public static final double FRAME_TIME = 1.0 / TARGET_FPS;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PLAYER PHYSICS (tune these for game feel)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final double GRAVITY = 2200.0;           // Pixels per second squared (lowered for floatier jump)
    public static final double JUMP_VELOCITY = -920.0;     // Initial upward velocity (stronger jump)
    public static final double MAX_FALL_SPEED = 900.0;     // Terminal velocity (slower fall)
    public static final double GROUND_Y = 580.0;           // Y position of ground level (lower ground = more jump room)
    public static final double PLAYER_X = 200.0;           // Fixed X position of player
    public static final double PLAYER_WIDTH = 80.0;        // Visual width of octopus
    public static final double PLAYER_HEIGHT = 70.0;       // Visual height of octopus
    public static final double HITBOX_SCALE = 0.6;         // Hitbox is 60% of visual size (more forgiving)
    
    // Jump feel improvements
    public static final double COYOTE_TIME_MS = 150.0;     // Grace period to jump after leaving ground (extended)
    public static final double JUMP_BUFFER_MS = 200.0;     // Buffer jump input before landing (extended)
    public static final double FALL_MULTIPLIER = 1.5;      // Extra gravity when falling (reduced for floatier feel)
    
    // Squash and stretch
    public static final double SQUASH_AMOUNT = 0.15;       // Max squash on landing
    public static final double STRETCH_AMOUNT = 0.2;       // Max stretch during jump
    
    // ═══════════════════════════════════════════════════════════════════════════
    // SCROLLING & DIFFICULTY
    // ═══════════════════════════════════════════════════════════════════════════
    public static final double BASE_SCROLL_SPEED = 300.0;  // Starting pixels per second (slower start)
    public static final double MAX_SCROLL_SPEED = 650.0;   // Maximum speed cap (lower max)
    public static final double SPEED_INCREMENT = 8.0;      // Speed increase per interval (gentler curve)
    public static final double SPEED_INCREASE_INTERVAL = 15.0; // Seconds between speed increases (slower ramp)
    
    // ═══════════════════════════════════════════════════════════════════════════
    // OBSTACLE SPAWNING
    // ═══════════════════════════════════════════════════════════════════════════
    public static final double MIN_OBSTACLE_GAP = 450.0;   // Minimum pixels between obstacles (more breathing room)
    public static final double MAX_OBSTACLE_GAP = 800.0;   // Maximum pixels between obstacles
    public static final double GAP_REDUCTION_RATE = 0.99;  // Multiply gaps by this as difficulty increases (slower)
    public static final double MIN_GAP_FLOOR = 380.0;      // Gaps never go below this (higher floor)
    
    // Coral dimensions - REDUCED heights so all are jumpable
    public static final double CORAL_MIN_HEIGHT = 45.0;
    public static final double CORAL_MAX_HEIGHT = 95.0;    // Max height reduced so player can always clear
    public static final double CORAL_MIN_WIDTH = 35.0;
    public static final double CORAL_MAX_WIDTH = 70.0;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // HEARTS & SCORING
    // ═══════════════════════════════════════════════════════════════════════════
    public static final double HEART_SPAWN_CHANCE = 0.5;   // Chance to spawn heart with obstacle (more hearts)
    public static final double HEART_SIZE = 38.0;          // Heart visual size (slightly bigger)
    public static final double HEART_MIN_HEIGHT = 80.0;    // Minimum height above ground (lower = easier to grab)
    public static final double HEART_MAX_HEIGHT = 220.0;   // Maximum height above ground (capped to reachable jump arc)
    public static final int HEART_SCORE_VALUE = 50;        // Base score per heart
    public static final double CHAIN_MULTIPLIER = 0.5;     // Bonus multiplier per chain level
    public static final int MAX_CHAIN = 10;                // Maximum chain level
    
    // Score accumulation
    public static final double SCORE_PER_SECOND = 10.0;    // Base score per second survived
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PARTICLES & EFFECTS
    // ═══════════════════════════════════════════════════════════════════════════
    public static final int MAX_BUBBLES = 30;              // Background bubble count
    public static final double BUBBLE_MIN_SIZE = 3.0;
    public static final double BUBBLE_MAX_SIZE = 12.0;
    public static final double BUBBLE_SPEED = 80.0;        // Rise speed
    
    public static final int PARTICLE_POOL_SIZE = 100;      // Reusable particles
    public static final double SPARKLE_LIFETIME = 0.6;     // Seconds
    public static final int SPARKLES_PER_HEART = 12;       // Particles when collecting heart
    
    public static final double SCREEN_SHAKE_INTENSITY = 12.0;
    public static final double SCREEN_SHAKE_DURATION = 0.25;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PARALLAX LAYERS (depths and speeds)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final double[] PARALLAX_SPEEDS = {0.1, 0.25, 0.5, 0.75}; // Relative to scroll speed
    
    // ═══════════════════════════════════════════════════════════════════════════
    // COLOR PALETTE
    // ═══════════════════════════════════════════════════════════════════════════
    
    // Ocean background
    public static final Color OCEAN_DEEP = Color.web("#0a1628");
    public static final Color OCEAN_MID = Color.web("#1a2d4a");
    public static final Color OCEAN_LIGHT = Color.web("#1e4d6b");
    public static final Color OCEAN_SURFACE = Color.web("#2a6b8a");
    
    // Accent colors
    public static final Color TEAL_ACCENT = Color.web("#4ecdc4");
    public static final Color TEAL_DARK = Color.web("#45b7aa");
    
    // Coral colors
    public static final Color CORAL_PINK = Color.web("#ff6b8a");
    public static final Color CORAL_LIGHT = Color.web("#ff8fa3");
    public static final Color CORAL_ORANGE = Color.web("#ff9f7a");
    public static final Color CORAL_PURPLE = Color.web("#c77dff");
    
    // Heart colors
    public static final Color HEART_RED = Color.web("#ff4757");
    public static final Color HEART_PINK = Color.web("#ff6b7a");
    public static final Color HEART_GLOW = Color.web("#ff8fa3", 0.6);
    
    // Sparkle/Gold
    public static final Color SPARKLE_GOLD = Color.web("#ffd93d");
    public static final Color SPARKLE_LIGHT = Color.web("#ffec8b");
    
    // Pink Octopus (the protagonist!)
    public static final Color OCTOPUS_PINK = Color.web("#ff69b4");
    public static final Color OCTOPUS_PINK_LIGHT = Color.web("#ff85c1");
    public static final Color OCTOPUS_PINK_SOFT = Color.web("#ffb6d9");
    public static final Color OCTOPUS_CHEEK = Color.web("#ff9ecd");
    
    // UI colors
    public static final Color UI_PANEL_BG = Color.web("#0a1628", 0.85);
    public static final Color UI_TEXT = Color.web("#e8f4f8");
    public static final Color UI_TEXT_SECONDARY = Color.web("#a8d4e6");
    public static final Color UI_ACCENT = Color.web("#4ecdc4");
    
    // Light rays
    public static final Color LIGHT_RAY = Color.web("#ffffcc", 0.08);
    
    // ═══════════════════════════════════════════════════════════════════════════
    // GRADIENTS
    // ═══════════════════════════════════════════════════════════════════════════
    
    public static LinearGradient oceanGradient() {
        return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, OCEAN_SURFACE),
            new Stop(0.3, OCEAN_LIGHT),
            new Stop(0.7, OCEAN_MID),
            new Stop(1.0, OCEAN_DEEP)
        );
    }
    
    public static LinearGradient octopusGradient() {
        return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, OCTOPUS_PINK_LIGHT),
            new Stop(0.5, OCTOPUS_PINK),
            new Stop(1.0, OCTOPUS_PINK.darker())
        );
    }
    
    public static LinearGradient heartGradient() {
        return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, HEART_PINK),
            new Stop(0.5, HEART_RED),
            new Stop(1.0, HEART_RED.darker())
        );
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMATION TIMING
    // ═══════════════════════════════════════════════════════════════════════════
    public static final double IDLE_BOB_SPEED = 2.0;       // Oscillations per second
    public static final double IDLE_BOB_AMOUNT = 5.0;      // Pixels up/down
    public static final double TENTACLE_WAVE_SPEED = 3.0;  // Wave speed
    public static final double HEART_PULSE_SPEED = 2.5;    // Heart size oscillation
    public static final double HEART_PULSE_AMOUNT = 0.1;   // 10% size variation
    
    // ═══════════════════════════════════════════════════════════════════════════
    // UI STYLING
    // ═══════════════════════════════════════════════════════════════════════════
    public static final double BUTTON_WIDTH = 280.0;
    public static final double BUTTON_HEIGHT = 55.0;
    public static final double BUTTON_CORNER_RADIUS = 12.0;
    public static final double BUTTON_HOVER_SCALE = 1.05;
    public static final double BUTTON_PRESS_SCALE = 0.95;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // SAVE KEYS
    // ═══════════════════════════════════════════════════════════════════════════
    public static final String SAVE_BEST_SCORE = "bestScore";
    public static final String SAVE_BEST_HEARTS = "bestHearts";
    public static final String SAVE_SOUND_ENABLED = "soundEnabled";
}
