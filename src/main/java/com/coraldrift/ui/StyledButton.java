package com.coraldrift.ui;

import com.coraldrift.audio.AudioManager;
import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Stylish custom button with hover and press animations.
 */
public class StyledButton extends StackPane {
    
    private final Rectangle background;
    private final Label label;
    private final ScaleTransition hoverScale;
    private final ScaleTransition pressScale;
    
    private Runnable onClick;
    private boolean isHovered = false;
    
    public StyledButton(String text) {
        this(text, Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT);
    }
    
    public StyledButton(String text, double width, double height) {
        setPrefSize(width, height);
        setMaxSize(width, height);
        setAlignment(Pos.CENTER);
        setCursor(Cursor.HAND);
        
        // Background rectangle
        background = new Rectangle(width, height);
        background.setArcWidth(Constants.BUTTON_CORNER_RADIUS * 2);
        background.setArcHeight(Constants.BUTTON_CORNER_RADIUS * 2);
        
        // Default gradient
        setDefaultGradient();
        
        // Drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setOffsetY(4);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        background.setEffect(shadow);
        
        // Label
        label = new Label(text);
        label.setTextFill(Constants.UI_TEXT);
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI', sans-serif;");
        
        getChildren().addAll(background, label);
        
        // Animations
        hoverScale = new ScaleTransition(Duration.millis(100), this);
        hoverScale.setToX(Constants.BUTTON_HOVER_SCALE);
        hoverScale.setToY(Constants.BUTTON_HOVER_SCALE);
        
        pressScale = new ScaleTransition(Duration.millis(50), this);
        pressScale.setToX(Constants.BUTTON_PRESS_SCALE);
        pressScale.setToY(Constants.BUTTON_PRESS_SCALE);
        
        // Event handlers
        setOnMouseEntered(e -> onHoverEnter());
        setOnMouseExited(e -> onHoverExit());
        setOnMousePressed(e -> onPress());
        setOnMouseReleased(e -> onRelease());
    }
    
    private void setDefaultGradient() {
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Constants.TEAL_ACCENT),
            new Stop(1, Constants.TEAL_DARK)
        );
        background.setFill(gradient);
    }
    
    private void setHoverGradient() {
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Constants.TEAL_ACCENT.brighter()),
            new Stop(1, Constants.TEAL_ACCENT)
        );
        background.setFill(gradient);
        
        // Add glow
        Glow glow = new Glow(0.3);
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setOffsetY(4);
        shadow.setColor(Color.rgb(78, 205, 196, 0.5));
        shadow.setInput(glow);
        background.setEffect(shadow);
    }
    
    private void onHoverEnter() {
        isHovered = true;
        setHoverGradient();
        hoverScale.stop();
        hoverScale.setToX(Constants.BUTTON_HOVER_SCALE);
        hoverScale.setToY(Constants.BUTTON_HOVER_SCALE);
        hoverScale.playFromStart();
    }
    
    private void onHoverExit() {
        isHovered = false;
        setDefaultGradient();
        
        // Reset shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setOffsetY(4);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        background.setEffect(shadow);
        
        hoverScale.stop();
        hoverScale.setToX(1.0);
        hoverScale.setToY(1.0);
        hoverScale.playFromStart();
    }
    
    private void onPress() {
        pressScale.playFromStart();
        AudioManager.getInstance().playClick();
    }
    
    private void onRelease() {
        ScaleTransition release = new ScaleTransition(Duration.millis(100), this);
        release.setToX(isHovered ? Constants.BUTTON_HOVER_SCALE : 1.0);
        release.setToY(isHovered ? Constants.BUTTON_HOVER_SCALE : 1.0);
        release.play();
        
        if (onClick != null && isHovered) {
            onClick.run();
        }
    }
    
    public void setOnAction(Runnable action) {
        this.onClick = action;
    }
    
    public void setText(String text) {
        label.setText(text);
    }
    
    /**
     * Create a secondary style button (less prominent).
     */
    public void setSecondaryStyle() {
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(60, 80, 100)),
            new Stop(1, Color.rgb(40, 60, 80))
        );
        background.setFill(gradient);
        label.setTextFill(Constants.UI_TEXT_SECONDARY);
    }
    
    /**
     * Create a danger/exit style button.
     */
    public void setDangerStyle() {
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(200, 80, 80)),
            new Stop(1, Color.rgb(150, 50, 50))
        );
        background.setFill(gradient);
    }
}
