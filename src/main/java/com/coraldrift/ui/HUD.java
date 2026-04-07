package com.coraldrift.ui;

import com.coraldrift.core.GameState;
import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Heads-up display showing score, hearts, and combo chain.
 */
public class HUD extends StackPane {
    
    private final Label scoreLabel;
    private final Label heartsLabel;
    private final Label bestLabel;
    private final Label chainLabel;
    private final Canvas chainCanvas;
    private final HBox chainContainer;
    
    private double animTime = 0;
    
    public HUD() {
        setAlignment(Pos.TOP_LEFT);
        setPadding(new Insets(20));
        setPickOnBounds(false);
        
        // Main HUD container
        VBox hudBox = new VBox(8);
        hudBox.setAlignment(Pos.TOP_LEFT);
        hudBox.setPadding(new Insets(15, 20, 15, 20));
        hudBox.setMaxWidth(220);
        
        // Background
        Rectangle bg = new Rectangle(220, 140);
        bg.setArcWidth(15);
        bg.setArcHeight(15);
        bg.setFill(Color.rgb(10, 22, 40, 0.75));
        bg.setStroke(Constants.TEAL_ACCENT.deriveColor(0, 1, 1, 0.2));
        bg.setStrokeWidth(1);
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        bg.setEffect(shadow);
        
        // Score row
        HBox scoreRow = new HBox(10);
        scoreRow.setAlignment(Pos.CENTER_LEFT);
        Label scoreIcon = new Label("⭐");
        scoreIcon.setFont(Font.font(18));
        scoreLabel = new Label("0");
        scoreLabel.setTextFill(Constants.SPARKLE_GOLD);
        scoreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        scoreRow.getChildren().addAll(scoreIcon, scoreLabel);
        
        // Hearts row
        HBox heartsRow = new HBox(10);
        heartsRow.setAlignment(Pos.CENTER_LEFT);
        Label heartIcon = new Label("❤");
        heartIcon.setTextFill(Constants.HEART_RED);
        heartIcon.setFont(Font.font(18));
        heartsLabel = new Label("0");
        heartsLabel.setTextFill(Constants.HEART_PINK);
        heartsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heartsRow.getChildren().addAll(heartIcon, heartsLabel);
        
        // Best score row
        HBox bestRow = new HBox(10);
        bestRow.setAlignment(Pos.CENTER_LEFT);
        Label bestIcon = new Label("👑");
        bestIcon.setFont(Font.font(14));
        bestLabel = new Label("Best: 0");
        bestLabel.setTextFill(Constants.UI_TEXT_SECONDARY);
        bestLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        bestRow.getChildren().addAll(bestIcon, bestLabel);
        
        // Chain indicator
        chainContainer = new HBox(8);
        chainContainer.setAlignment(Pos.CENTER_LEFT);
        Label chainIcon = new Label("🔥");
        chainIcon.setFont(Font.font(16));
        chainLabel = new Label("");
        chainLabel.setTextFill(Constants.SPARKLE_GOLD);
        chainLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        chainCanvas = new Canvas(60, 8);
        chainContainer.getChildren().addAll(chainIcon, chainLabel, chainCanvas);
        chainContainer.setVisible(false);
        
        hudBox.getChildren().addAll(scoreRow, heartsRow, bestRow, chainContainer);
        
        StackPane hudPanel = new StackPane(bg, hudBox);
        hudPanel.setAlignment(Pos.TOP_LEFT);
        
        getChildren().add(hudPanel);
    }
    
    /**
     * Update HUD with current game state.
     */
    public void update(GameState state, double deltaTime) {
        animTime += deltaTime;
        
        scoreLabel.setText(String.valueOf(state.getDisplayScore()));
        heartsLabel.setText(String.valueOf(state.getHeartsCollected()));
        bestLabel.setText("Best: " + state.getBestScore());
        
        // Chain display
        if (state.isChainActive()) {
            chainContainer.setVisible(true);
            int chain = state.getChainCount();
            double multiplier = state.getChainMultiplier();
            chainLabel.setText(String.format("x%.1f", multiplier));
            
            // Draw chain progress bar
            drawChainProgress(state.getChainProgress(), chain);
            
            // Pulse effect on chain label
            double pulse = 1.0 + Math.sin(animTime * 8) * 0.05;
            chainLabel.setScaleX(pulse);
            chainLabel.setScaleY(pulse);
        } else {
            chainContainer.setVisible(false);
        }
    }
    
    private void drawChainProgress(double progress, int chainLevel) {
        GraphicsContext gc = chainCanvas.getGraphicsContext2D();
        double w = chainCanvas.getWidth();
        double h = chainCanvas.getHeight();
        
        gc.clearRect(0, 0, w, h);
        
        // Background
        gc.setFill(Color.rgb(40, 40, 60, 0.5));
        gc.fillRoundRect(0, 0, w, h, h, h);
        
        // Progress fill with gradient based on chain level
        double hue = 45 + chainLevel * 8; // Gold -> Orange -> Red
        Color barColor = Color.hsb(Math.min(hue, 60), 0.9, 1.0);
        
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, barColor.brighter()),
            new Stop(1, barColor)
        );
        
        gc.setFill(gradient);
        gc.fillRoundRect(0, 0, w * progress, h, h, h);
    }
    
    /**
     * Show pause indicator.
     */
    public void showPaused(boolean paused) {
        // Could add a "PAUSED" overlay here if desired
    }
}
