package com.coraldrift.scene;

import com.coraldrift.core.GameEngine;
import com.coraldrift.core.GameLoop;
import com.coraldrift.core.GameState;
import com.coraldrift.ui.HUD;
import com.coraldrift.util.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

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
            }
        });
        
        // HUD
        hud = new HUD();
        
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
    }
    
    /**
     * Hide pause overlay.
     */
    private void hidePause() {
        if (pauseOverlay != null) {
            root.getChildren().remove(pauseOverlay);
            pauseOverlay = null;
            engine.resume();
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
