package com.coraldrift;

import com.coraldrift.audio.AudioManager;
import com.coraldrift.scene.SceneManager;
import com.coraldrift.util.Constants;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for Coral Drift game.
 * 
 * A cute endless runner featuring a pink octopus swimming through
 * a dreamy underwater world, avoiding coral and collecting hearts.
 * 
 * Controls:
 *   SPACE / UP / W - Jump
 *   P / ESC        - Pause
 *   R              - Restart (after game over)
 * 
 * @author Coral Drift Team
 * @version 1.0.0
 */
public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize audio system
        AudioManager.getInstance().initialize();
        
        // Create scene manager and show menu
        SceneManager sceneManager = new SceneManager(primaryStage);
        sceneManager.showMenu();
        
        // Configure stage
        primaryStage.setTitle(Constants.GAME_TITLE);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
        
        // Cleanup on close
        primaryStage.setOnCloseRequest(e -> {
            AudioManager.getInstance().dispose();
        });
    }
    
    @Override
    public void stop() {
        AudioManager.getInstance().dispose();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
