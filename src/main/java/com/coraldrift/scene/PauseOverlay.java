package com.coraldrift.scene;

import com.coraldrift.ui.StyledButton;
import com.coraldrift.ui.UIFactory;
import com.coraldrift.util.Constants;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Pause overlay shown when game is paused.
 */
public class PauseOverlay extends StackPane {
    
    private Runnable onResume;
    private Runnable onMenu;
    
    public PauseOverlay() {
        setAlignment(Pos.CENTER);
        
        // Dim background
        Rectangle dimBg = new Rectangle(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        dimBg.setFill(Color.rgb(0, 0, 0, 0.6));
        
        // Panel
        StackPane panel = UIFactory.createPanel(350, 300);
        
        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        
        // Title
        Label title = UIFactory.createTitle("Paused", 42);
        
        // Octopus resting
        Label octopus = new Label("🐙💤");
        octopus.setFont(javafx.scene.text.Font.font(48));
        
        // Buttons
        VBox buttons = new VBox(12);
        buttons.setAlignment(Pos.CENTER);
        
        StyledButton resumeButton = new StyledButton("Resume", 180, 45);
        resumeButton.setOnAction(() -> {
            if (onResume != null) onResume.run();
        });
        
        StyledButton menuButton = new StyledButton("Main Menu", 180, 45);
        menuButton.setSecondaryStyle();
        menuButton.setOnAction(() -> {
            if (onMenu != null) onMenu.run();
        });
        
        Label hint = UIFactory.createText("Press P or ESC to resume", 12);
        hint.setTextFill(Constants.UI_TEXT_SECONDARY);
        
        buttons.getChildren().addAll(resumeButton, menuButton, hint);
        
        content.getChildren().addAll(title, octopus, buttons);
        panel.getChildren().add(content);
        
        getChildren().addAll(dimBg, panel);
        
        // Fade in
        FadeTransition fade = new FadeTransition(Duration.millis(150), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
    
    public void setOnResume(Runnable handler) {
        this.onResume = handler;
    }
    
    public void setOnMenu(Runnable handler) {
        this.onMenu = handler;
    }
}
