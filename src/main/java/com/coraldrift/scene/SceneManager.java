package com.coraldrift.scene;

import com.coraldrift.util.Constants;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Manages scene transitions with fade effects.
 */
public class SceneManager {
    
    private final Stage stage;
    private final StackPane rootContainer;
    private StackPane currentScene;
    
    private MenuScene menuScene;
    private GameScene gameScene;
    
    public SceneManager(Stage stage) {
        this.stage = stage;
        this.rootContainer = new StackPane();
        rootContainer.setStyle("-fx-background-color: #0a1628;");
        
        Scene scene = new Scene(rootContainer, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        
        // Apply CSS if available
        try {
            String css = getClass().getResource("/com/coraldrift/styles/game.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            // CSS not found, continue without it
        }
        
        stage.setScene(scene);
        stage.setTitle(Constants.GAME_TITLE);
        stage.setResizable(false);
    }
    
    /**
     * Initialize and show the menu scene.
     */
    public void showMenu() {
        if (menuScene == null) {
            menuScene = new MenuScene(this);
        }
        transitionTo(menuScene.getRoot());
    }
    
    /**
     * Initialize and show the game scene.
     */
    public void showGame() {
        if (gameScene == null) {
            gameScene = new GameScene(this);
        }
        gameScene.startNewGame();
        transitionTo(gameScene.getRoot());
        gameScene.requestFocus();
    }
    
    /**
     * Restart the current game.
     */
    public void restartGame() {
        if (gameScene != null) {
            gameScene.startNewGame();
            gameScene.requestFocus();
        }
    }
    
    /**
     * Transition to a new scene with fade effect.
     */
    private void transitionTo(StackPane newScene) {
        if (currentScene == null) {
            rootContainer.getChildren().add(newScene);
            currentScene = newScene;
            return;
        }
        
        // Fade out current, fade in new
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentScene);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        StackPane oldScene = currentScene;
        currentScene = newScene;
        newScene.setOpacity(0);
        rootContainer.getChildren().add(newScene);
        
        fadeOut.setOnFinished(e -> {
            rootContainer.getChildren().remove(oldScene);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newScene);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    /**
     * Get the game scene (for HUD updates etc).
     */
    public GameScene getGameScene() {
        return gameScene;
    }
    
    /**
     * Get the primary stage.
     */
    public Stage getStage() {
        return stage;
    }
}
