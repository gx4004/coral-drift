package com.coraldrift.scene;

import com.coraldrift.util.Constants;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
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

        // Inner container — fixed 1280×720 logical viewport. Game logic
        // continues to use these coordinates everywhere.
        this.rootContainer = new StackPane();
        rootContainer.setStyle("-fx-background-color: #0a1628;");
        rootContainer.setMinSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        rootContainer.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        rootContainer.setMaxSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        // Scale transform — recomputed every time the actual scene size
        // changes (window resize on desktop, surface bind on Android).
        Scale scale = new Scale(1, 1, 0, 0);
        rootContainer.getTransforms().add(scale);

        // Outer root — fills the actual device screen with black bars
        // wherever the 16:9 logical viewport doesn't match the device aspect.
        StackPane outerRoot = new StackPane();
        outerRoot.setStyle("-fx-background-color: #000000;");
        outerRoot.setAlignment(Pos.CENTER);
        outerRoot.getChildren().add(rootContainer);

        Scene scene = new Scene(outerRoot, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        scene.setFill(Color.BLACK);

        // Letterbox-scale: fit 1280×720 inside whatever the actual scene is.
        // JavaFX automatically maps mouse/touch coords back through the
        // inverse Scale transform, so input handlers need no changes.
        Runnable rescale = () -> {
            double sx = scene.getWidth()  / Constants.WINDOW_WIDTH;
            double sy = scene.getHeight() / Constants.WINDOW_HEIGHT;
            double s  = Math.min(sx, sy);
            if (s > 0 && Double.isFinite(s)) {
                scale.setX(s);
                scale.setY(s);
            }
        };
        scene.widthProperty().addListener((o, ov, nv) -> rescale.run());
        scene.heightProperty().addListener((o, ov, nv) -> rescale.run());
        rescale.run();

        // Apply CSS if available
        try {
            String css = getClass().getResource("/com/coraldrift/styles/game.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            // CSS not found, continue without it
        }

        stage.setScene(scene);
        stage.setTitle(Constants.GAME_TITLE);
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
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), currentScene);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_BOTH);

        StackPane oldScene = currentScene;
        currentScene = newScene;
        newScene.setOpacity(0);
        rootContainer.getChildren().add(newScene);

        fadeOut.setOnFinished(e -> {
            rootContainer.getChildren().remove(oldScene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), newScene);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_BOTH);
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
