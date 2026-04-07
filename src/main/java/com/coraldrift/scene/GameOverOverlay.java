package com.coraldrift.scene;

import com.coraldrift.ui.StyledButton;
import com.coraldrift.ui.UIFactory;
import com.coraldrift.util.Constants;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Game over overlay showing final stats and options.
 */
public class GameOverOverlay extends StackPane {
    
    private Runnable onRestart;
    private Runnable onMenu;
    
    public GameOverOverlay(int score, int hearts, int bestScore, int maxChain,
                          boolean newBestScore, boolean newBestHearts) {
        setAlignment(Pos.CENTER);
        
        // Dim background
        Rectangle dimBg = new Rectangle(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        dimBg.setFill(Color.rgb(0, 0, 0, 0.7));
        
        // Panel
        StackPane panel = UIFactory.createPanel(420, 480);
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        
        // Sad octopus
        Label sadOctopus = new Label("🐙");
        sadOctopus.setFont(javafx.scene.text.Font.font(64));
        sadOctopus.setStyle("-fx-opacity: 0.8;");
        
        // Title
        Label title = UIFactory.createTitle("Game Over", 42);
        
        // Stats
        VBox stats = new VBox(12);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(10, 0, 10, 0));
        
        HBox scoreRow = UIFactory.createStatRow("Score:", String.valueOf(score));
        stats.getChildren().add(scoreRow);
        
        if (newBestScore) {
            Label badge = UIFactory.createNewBestBadge();
            stats.getChildren().add(badge);
        }
        
        stats.getChildren().add(UIFactory.createStatRow("Hearts:", String.valueOf(hearts)));
        
        if (newBestHearts && hearts > 0) {
            Label badge = new Label("New heart record! 💕");
            badge.setTextFill(Constants.HEART_PINK);
            badge.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 14));
            stats.getChildren().add(badge);
        }
        
        stats.getChildren().add(UIFactory.createStatRow("Best Chain:", "x" + maxChain));
        
        Rectangle sep = UIFactory.createSeparator(280);
        
        HBox bestRow = UIFactory.createStatRow("Best Score:", String.valueOf(bestScore));
        
        // Buttons
        VBox buttons = new VBox(12);
        buttons.setAlignment(Pos.CENTER);
        
        StyledButton restartButton = new StyledButton("Play Again", 200, 50);
        restartButton.setOnAction(() -> {
            if (onRestart != null) onRestart.run();
        });
        
        StyledButton menuButton = new StyledButton("Main Menu", 200, 50);
        menuButton.setSecondaryStyle();
        menuButton.setOnAction(() -> {
            if (onMenu != null) onMenu.run();
        });
        
        Label hint = UIFactory.createText("Press R to restart", 14);
        hint.setTextFill(Constants.UI_TEXT_SECONDARY);
        
        buttons.getChildren().addAll(restartButton, menuButton, hint);
        
        content.getChildren().addAll(sadOctopus, title, stats, sep, bestRow, buttons);
        panel.getChildren().add(content);
        
        getChildren().addAll(dimBg, panel);
        
        // Entrance animation
        playEntranceAnimation(panel);
    }
    
    private void playEntranceAnimation(StackPane panel) {
        // Fade in
        FadeTransition fade = new FadeTransition(Duration.millis(300), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        // Scale pop
        panel.setScaleX(0.8);
        panel.setScaleY(0.8);
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), panel);
        scale.setToX(1);
        scale.setToY(1);
        
        fade.play();
        scale.play();
    }
    
    public void setOnRestart(Runnable handler) {
        this.onRestart = handler;
    }
    
    public void setOnMenu(Runnable handler) {
        this.onMenu = handler;
    }
}
