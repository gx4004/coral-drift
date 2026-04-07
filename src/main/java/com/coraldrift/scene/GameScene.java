package com.coraldrift.scene;

import com.coraldrift.core.GameEngine;
import com.coraldrift.core.GameLoop;
import com.coraldrift.core.GameState;
import com.coraldrift.ui.HUD;
import com.coraldrift.util.Constants;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.SnapshotParameters;

/**
 * Main gameplay scene with game rendering and input handling.
 */
public class GameScene {
    
    private final SceneManager sceneManager;
    private final StackPane root;
    
    // Game systems
    private final GameEngine engine;
    private final GameLoop gameLoop;
    
    // Rendering
    private final Canvas gameCanvas;
    private final GraphicsContext gc;
    
    // UI
    private final HUD hud;
    private GameOverOverlay gameOverOverlay;
    private PauseOverlay pauseOverlay;
    
    public GameScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.root = new StackPane();
        root.setStyle("-fx-background-color: #0a1628;");
        
        // Game canvas
        gameCanvas = new Canvas(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        gc = gameCanvas.getGraphicsContext2D();
        
        // Engine
        engine = new GameEngine();
        
        // Game loop
        gameLoop = new GameLoop(new GameLoop.GameLoopCallback() {
            @Override
            public void update(double deltaTime) {
                engine.update(deltaTime);
                hud.update(engine.getState(), deltaTime);
                
                // Check for game over
                if (engine.getState().isGameOver() && gameOverOverlay == null) {
                    showGameOver();
                }
            }
            
            @Override
            public void render() {
                gc.clearRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
                engine.render(gc);
                // Chromatic aberration pass (3 frames after a hit)
                if (engine.getScreenEffects().isChromaticActive()) {
                    applyChromaticAberration(gc);
                    engine.getScreenEffects().decrementChromatic();
                }
            }
        });
        
        // HUD
        hud = new HUD();

        // Wire HUD observer for score pops
        engine.setHeartCollectObserver((x, y, pts, gold) -> hud.spawnScorePop(x, y, pts));
        engine.setJumpObserver((x, y) -> hud.spawnBlup(x, y));

        // Layout
        root.getChildren().addAll(gameCanvas, hud);
        StackPane.setAlignment(hud, Pos.TOP_LEFT);
        
        // Input handling
        setupInput();
    }
    
    private void setupInput() {
        root.setFocusTraversable(true);
        
        root.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            
            if (code == KeyCode.SPACE || code == KeyCode.UP || code == KeyCode.W) {
                if (engine.getState().isPlaying()) {
                    engine.jump();
                }
            } else if (code == KeyCode.P || code == KeyCode.ESCAPE) {
                if (engine.getState().isPlaying()) {
                    showPause();
                } else if (engine.getState().isPaused()) {
                    hidePause();
                }
            } else if (code == KeyCode.R) {
                if (engine.getState().isGameOver()) {
                    restartGame();
                }
            }
        });
        
        root.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.SPACE || code == KeyCode.UP || code == KeyCode.W) {
                engine.releaseJump();
            }
        });

        // Touch / mouse click — for Android (tap anywhere to jump / restart)
        root.setOnMouseClicked(e -> {
            if (engine.getState().isPlaying()) {
                engine.jump();
            } else if (engine.getState().isGameOver()) {
                restartGame();
            }
        });
        root.setOnMouseReleased(e -> engine.releaseJump());
    }
    
    /**
     * Start a new game.
     */
    public void startNewGame() {
        // Clear overlays
        hideGameOver();
        hidePause();
        
        // Reset and start
        engine.startGame();
        gameLoop.start();
    }
    
    /**
     * Show game over overlay.
     */
    private void showGameOver() {
        GameState state = engine.getState();
        gameOverOverlay = new GameOverOverlay(
            state.getScore(),
            state.getHeartsCollected(),
            state.getBestScore(),
            state.getMaxChainThisRun(),
            state.isNewBestScore(),
            state.isNewBestHearts()
        );
        
        gameOverOverlay.setOnRestart(this::restartGame);
        gameOverOverlay.setOnMenu(() -> {
            gameLoop.stop();
            sceneManager.showMenu();
        });
        
        root.getChildren().add(gameOverOverlay);
    }
    
    /**
     * Hide game over overlay.
     */
    private void hideGameOver() {
        if (gameOverOverlay != null) {
            root.getChildren().remove(gameOverOverlay);
            gameOverOverlay = null;
        }
    }
    
    /**
     * Apply chromatic aberration to a region around the player area.
     * Reads snapshot pixels and offsets R/G/B channels horizontally.
     */
    private void applyChromaticAberration(GraphicsContext gc) {
        try {
            // Scope to player-area for performance
            int rx = (int) Math.max(0, Constants.PLAYER_X - 120);
            int ry = (int) Math.max(0, Constants.GROUND_Y - Constants.PLAYER_HEIGHT * 2 - 20);
            int rw = (int) Math.min(400, Constants.WINDOW_WIDTH - rx);
            int rh = (int) Math.min(220, Constants.WINDOW_HEIGHT - ry);

            SnapshotParameters params = new SnapshotParameters();
            params.setViewport(new javafx.geometry.Rectangle2D(rx, ry, rw, rh));
            WritableImage snap = gameCanvas.snapshot(params, null);
            PixelReader reader = snap.getPixelReader();
            WritableImage out  = new WritableImage(rw, rh);
            PixelWriter writer = out.getPixelWriter();

            int shift = 4;
            for (int y = 0; y < rh; y++) {
                for (int x = 0; x < rw; x++) {
                    int rSrc = Math.min(x + shift, rw - 1);
                    int bSrc = Math.max(x - shift, 0);

                    int argbR = reader.getArgb(rSrc, y);
                    int argbG = reader.getArgb(x,    y);
                    int argbB = reader.getArgb(bSrc, y);

                    int a = (argbG >> 24) & 0xFF;
                    int r = (argbR >> 16) & 0xFF;
                    int g = (argbG >>  8) & 0xFF;
                    int b = (argbB      ) & 0xFF;

                    writer.setArgb(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
            gc.drawImage(out, rx, ry);
        } catch (Exception ignored) {
            // Snapshot may fail on some platforms — silently skip
        }
    }

    /**
     * Show pause overlay.
     */
    private void showPause() {
        engine.pause();
        
        pauseOverlay = new PauseOverlay();
        pauseOverlay.setOnResume(this::hidePause);
        pauseOverlay.setOnMenu(() -> {
            gameLoop.stop();
            sceneManager.showMenu();
        });
        
        root.getChildren().add(pauseOverlay);
        engine.getScreenEffects().setPauseDim(0.45);
    }

    /**
     * Hide pause overlay.
     */
    private void hidePause() {
        if (pauseOverlay != null) {
            root.getChildren().remove(pauseOverlay);
            pauseOverlay = null;
            engine.resume();
            engine.getScreenEffects().setPauseDim(0.0);
        }
    }
    
    /**
     * Restart the game.
     */
    private void restartGame() {
        hideGameOver();
        hidePause();
        engine.startGame();
    }
    
    /**
     * Request focus for input handling.
     */
    public void requestFocus() {
        root.requestFocus();
    }
    
    public StackPane getRoot() {
        return root;
    }
    
    public GameEngine getEngine() {
        return engine;
    }
}
