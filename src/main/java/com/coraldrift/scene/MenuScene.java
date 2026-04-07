package com.coraldrift.scene;

import com.coraldrift.audio.AudioManager;
import com.coraldrift.graphics.BubbleEmitter;
import com.coraldrift.graphics.OctopusRenderer;
import com.coraldrift.graphics.ParallaxBackground;
import com.coraldrift.ui.StyledButton;
import com.coraldrift.ui.UIFactory;
import com.coraldrift.util.Constants;
import com.coraldrift.util.SaveManager;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Main menu scene with title, buttons, and animated background.
 */
public class MenuScene {
    
    private final SceneManager sceneManager;
    private final StackPane root;
    
    // Background animation
    private final Canvas backgroundCanvas;
    private final ParallaxBackground background;
    private final BubbleEmitter bubbles;
    private AnimationTimer animationTimer;

    // Menu octopus
    private Canvas octopusCanvas;
    private OctopusRenderer menuOctopus;
    
    // UI elements
    private VBox menuPanel;
    private VBox howToPlayPanel;
    private boolean showingHowToPlay = false;
    
    public MenuScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.root = new StackPane();
        root.setStyle("-fx-background-color: #0a1628;");
        
        // Background canvas
        backgroundCanvas = new Canvas(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        background = new ParallaxBackground();
        bubbles = new BubbleEmitter();
        
        root.getChildren().add(backgroundCanvas);
        
        // Create UI panels
        createMenuPanel();
        createHowToPlayPanel();
        
        root.getChildren().add(menuPanel);
        
        // Start background animation
        startBackgroundAnimation();
    }
    
    private void createMenuPanel() {
        menuPanel = new VBox(30);
        menuPanel.setAlignment(Pos.CENTER);
        menuPanel.setPadding(new Insets(50));
        
        // Title
        VBox titleBox = UIFactory.createGameTitle();
        
        // Animated octopus
        octopusCanvas = new Canvas(120, 110);
        menuOctopus = new OctopusRenderer();
        
        // Buttons
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        StyledButton startButton = new StyledButton("Start Game");
        startButton.setOnAction(this::startGame);
        
        StyledButton howToPlayButton = new StyledButton("How to Play");
        howToPlayButton.setOnAction(this::showHowToPlay);
        howToPlayButton.setSecondaryStyle();
        
        StyledButton soundButton = new StyledButton(getSoundButtonText());
        soundButton.setOnAction(() -> {
            AudioManager.getInstance().toggleSound();
            soundButton.setText(getSoundButtonText());
        });
        soundButton.setSecondaryStyle();
        
        StyledButton exitButton = new StyledButton("Exit");
        exitButton.setOnAction(() -> sceneManager.getStage().close());
        exitButton.setDangerStyle();
        
        buttonBox.getChildren().addAll(startButton, howToPlayButton, soundButton, exitButton);
        
        // Best score display
        int bestScore = SaveManager.getInstance().getBestScore();
        int bestHearts = SaveManager.getInstance().getBestHearts();
        Label bestLabel = UIFactory.createText(
            String.format("Best Score: %d  |  Best Hearts: %d", bestScore, bestHearts), 
            16
        );
        bestLabel.setTextFill(Constants.UI_TEXT_SECONDARY);
        
        menuPanel.getChildren().addAll(titleBox, octopusCanvas, buttonBox, bestLabel);
    }
    
    private void createHowToPlayPanel() {
        howToPlayPanel = new VBox(20);
        howToPlayPanel.setAlignment(Pos.CENTER);
        howToPlayPanel.setPadding(new Insets(40));
        howToPlayPanel.setVisible(false);
        
        // Panel background
        StackPane panel = UIFactory.createPanel(500, 450);
        
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        
        Label title = UIFactory.createTitle("How to Play", 36);
        
        VBox instructions = new VBox(12);
        instructions.setAlignment(Pos.CENTER_LEFT);
        instructions.setPadding(new Insets(0, 30, 0, 30));
        
        instructions.getChildren().addAll(
            createInstructionRow("🎮", "SPACE or UP", "Jump"),
            createInstructionRow("⏸", "P", "Pause"),
            createInstructionRow("🔄", "R", "Restart (after game over)")
        );
        
        Rectangle separator = UIFactory.createSeparator(300);
        
        VBox tips = new VBox(10);
        tips.setAlignment(Pos.CENTER);
        
        Label tipsTitle = UIFactory.createAccentText("Tips", 20);
        Label tip1 = UIFactory.createText("• Collect hearts to build your Harmony Chain", 14);
        Label tip2 = UIFactory.createText("• Chain multiplies your score: x1.5, x2, x2.5...", 14);
        Label tip3 = UIFactory.createText("• Higher hearts = bigger risk, bigger reward!", 14);
        Label tip4 = UIFactory.createText("• Speed increases over time - stay focused!", 14);
        
        tips.getChildren().addAll(tipsTitle, tip1, tip2, tip3, tip4);
        
        StyledButton backButton = new StyledButton("Back", 150, 45);
        backButton.setOnAction(this::hideHowToPlay);
        
        content.getChildren().addAll(title, instructions, separator, tips, backButton);
        panel.getChildren().add(content);
        
        howToPlayPanel.getChildren().add(panel);
        root.getChildren().add(howToPlayPanel);
    }
    
    private HBox createInstructionRow(String icon, String key, String action) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        iconLabel.setMinWidth(30);
        
        Label keyLabel = new Label(key);
        keyLabel.setTextFill(Constants.UI_ACCENT);
        keyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        keyLabel.setMinWidth(120);
        
        Label actionLabel = UIFactory.createText(action, 16);
        
        row.getChildren().addAll(iconLabel, keyLabel, actionLabel);
        return row;
    }
    
    private String getSoundButtonText() {
        return AudioManager.getInstance().isSoundEnabled() ? "Sound: ON" : "Sound: OFF";
    }
    
    private void startGame() {
        stopBackgroundAnimation();
        sceneManager.showGame();
    }
    
    private void showHowToPlay() {
        showingHowToPlay = true;
        menuPanel.setVisible(false);
        howToPlayPanel.setVisible(true);
    }
    
    private void hideHowToPlay() {
        showingHowToPlay = false;
        howToPlayPanel.setVisible(false);
        menuPanel.setVisible(true);
    }
    
    private void startBackgroundAnimation() {
        animationTimer = new AnimationTimer() {
            private long lastTime = 0;
            
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                
                // Update
                background.update(deltaTime, Constants.BASE_SCROLL_SPEED * 0.3);
                bubbles.update(deltaTime);
                menuOctopus.update(deltaTime, false, true, 0);

                // Render background
                GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
                background.render(gc);
                bubbles.render(gc);

                // Render octopus onto its canvas
                GraphicsContext octGc = octopusCanvas.getGraphicsContext2D();
                octGc.clearRect(0, 0, 120, 110);
                menuOctopus.render(octGc, 0, 0, 120, 110);
            }
        };
        animationTimer.start();
    }
    
    private void stopBackgroundAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
    
    public StackPane getRoot() {
        return root;
    }
}
