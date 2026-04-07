package com.coraldrift.ui;

import com.coraldrift.util.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Factory for creating consistent UI elements.
 */
public class UIFactory {
    
    private UIFactory() {}
    
    /**
     * Create a styled title label.
     */
    public static Label createTitle(String text, double fontSize) {
        Label label = new Label(text);
        label.setTextFill(Constants.UI_TEXT);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setOffsetY(3);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        label.setEffect(shadow);
        
        return label;
    }
    
    /**
     * Create a styled subtitle label.
     */
    public static Label createSubtitle(String text) {
        Label label = new Label(text);
        label.setTextFill(Constants.UI_TEXT_SECONDARY);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        return label;
    }
    
    /**
     * Create a styled text label.
     */
    public static Label createText(String text, double fontSize) {
        Label label = new Label(text);
        label.setTextFill(Constants.UI_TEXT);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, fontSize));
        return label;
    }
    
    /**
     * Create accent-colored text.
     */
    public static Label createAccentText(String text, double fontSize) {
        Label label = new Label(text);
        label.setTextFill(Constants.UI_ACCENT);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
        return label;
    }
    
    /**
     * Create a semi-transparent panel background.
     */
    public static StackPane createPanel(double width, double height) {
        StackPane panel = new StackPane();
        panel.setPrefSize(width, height);
        panel.setMaxSize(width, height);
        panel.setAlignment(Pos.CENTER);
        
        Rectangle bg = new Rectangle(width, height);
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        bg.setFill(Constants.UI_PANEL_BG);
        
        // Border
        bg.setStroke(Constants.TEAL_ACCENT.deriveColor(0, 1, 1, 0.3));
        bg.setStrokeWidth(2);
        
        // Shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setOffsetY(5);
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        bg.setEffect(shadow);
        
        panel.getChildren().add(bg);
        
        return panel;
    }
    
    /**
     * Create a panel with content.
     */
    public static StackPane createPanelWithContent(double width, double height, VBox content) {
        StackPane panel = createPanel(width, height);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        panel.getChildren().add(content);
        return panel;
    }
    
    /**
     * Create a horizontal separator line.
     */
    public static Rectangle createSeparator(double width) {
        Rectangle sep = new Rectangle(width, 2);
        sep.setFill(Constants.UI_TEXT_SECONDARY.deriveColor(0, 1, 1, 0.3));
        return sep;
    }
    
    /**
     * Create a stat display (label: value).
     */
    public static HBox createStatRow(String labelText, String valueText) {
        Label label = createText(labelText, 18);
        label.setTextFill(Constants.UI_TEXT_SECONDARY);
        
        Label value = createText(valueText, 18);
        value.setTextFill(Constants.UI_TEXT);
        value.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox row = new HBox(10, label, spacer, value);
        row.setAlignment(Pos.CENTER);
        row.setMinWidth(250);
        
        return row;
    }
    
    /**
     * Create a "NEW BEST!" badge.
     */
    public static Label createNewBestBadge() {
        Label badge = new Label("✨ NEW BEST! ✨");
        badge.setTextFill(Constants.SPARKLE_GOLD);
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        DropShadow glow = new DropShadow();
        glow.setRadius(10);
        glow.setColor(Constants.SPARKLE_GOLD);
        badge.setEffect(glow);
        
        return badge;
    }
    
    /**
     * Create the game title with fancy styling.
     */
    public static VBox createGameTitle() {
        Label title = new Label("Coral Drift");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 72));
        
        // Gradient text effect via style
        title.setStyle(
            "-fx-font-size: 72px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Glow effect
        DropShadow glow = new DropShadow();
        glow.setRadius(20);
        glow.setColor(Constants.TEAL_ACCENT);
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setOffsetY(5);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setInput(glow);
        
        title.setEffect(shadow);
        
        // Subtitle
        Label subtitle = createSubtitle("~ A Dreamy Ocean Adventure ~");
        
        VBox box = new VBox(10, title, subtitle);
        box.setAlignment(Pos.CENTER);
        
        return box;
    }
    
    /**
     * Create heart icon for HUD.
     */
    public static Label createHeartIcon() {
        Label heart = new Label("❤");
        heart.setTextFill(Constants.HEART_RED);
        heart.setFont(Font.font(24));
        return heart;
    }
}
