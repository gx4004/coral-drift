package com.coraldrift.scene;

import com.coraldrift.graphics.OctopusRenderer;
import com.coraldrift.ui.StyledButton;
import com.coraldrift.ui.UIFactory;
import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
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
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Game over overlay with slide-up entrance, count-up stats, confetti on new best, and animated sad Ping.
 */
public class GameOverOverlay extends StackPane {

    private Runnable onRestart;
    private Runnable onMenu;

    private AnimationTimer animTimer;
    private final Label scoreValueLabel;
    private final Label heartsValueLabel;
    private final int finalScore;
    private final int finalHearts;

    // Count-up
    private double countUpProgress = 0;
    private static final double COUNT_UP_DURATION = 1.5;

    // Confetti
    private final Canvas confettiCanvas;
    private final List<ConfettiPiece> confetti = new ArrayList<>();

    // Octopus
    private final Canvas octopusCanvas;
    private final OctopusRenderer octopusRenderer;

    public GameOverOverlay(int score, int hearts, int bestScore, int maxChain,
                           boolean newBestScore, boolean newBestHearts) {
        this.finalScore  = score;
        this.finalHearts = hearts;
        setAlignment(Pos.CENTER);

        // Dim background
        Rectangle dimBg = new Rectangle(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        dimBg.setFill(Color.rgb(0, 0, 0, 0.7));

        // Confetti canvas (full window, mouse-transparent)
        confettiCanvas = new Canvas(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        confettiCanvas.setMouseTransparent(true);

        // Panel
        StackPane panel = UIFactory.createPanel(420, 490);

        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28));

        // Animated sad octopus
        octopusCanvas = new Canvas(100, 100);
        octopusRenderer = new OctopusRenderer();
        octopusRenderer.setExpression(OctopusRenderer.Expression.DEAD);

        // Title
        Label title = UIFactory.createTitle("Game Over", 42);

        // Stats
        VBox stats = new VBox(11);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(8, 0, 8, 0));

        scoreValueLabel = new Label("0");
        scoreValueLabel.setTextFill(Constants.SPARKLE_GOLD);
        scoreValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        stats.getChildren().add(buildStatRow("Score:", scoreValueLabel));

        if (newBestScore) {
            stats.getChildren().add(UIFactory.createNewBestBadge());
        }

        heartsValueLabel = new Label("0");
        heartsValueLabel.setTextFill(Constants.HEART_PINK);
        heartsValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        stats.getChildren().add(buildStatRow("Hearts:", heartsValueLabel));

        if (newBestHearts && hearts > 0) {
            Label badge = new Label("New heart record! \uD83D\uDC95");
            badge.setTextFill(Constants.HEART_PINK);
            badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            stats.getChildren().add(badge);
        }

        stats.getChildren().add(UIFactory.createStatRow("Best Chain:", "x" + maxChain));

        Rectangle sep = UIFactory.createSeparator(280);
        HBox bestRow = UIFactory.createStatRow("Best Score:", String.valueOf(bestScore));

        // Buttons
        VBox buttons = new VBox(12);
        buttons.setAlignment(Pos.CENTER);

        StyledButton restartButton = new StyledButton("Play Again", 200, 50);
        restartButton.setOnAction(() -> { stopTimer(); if (onRestart != null) onRestart.run(); });

        StyledButton menuButton = new StyledButton("Main Menu", 200, 50);
        menuButton.setSecondaryStyle();
        menuButton.setOnAction(() -> { stopTimer(); if (onMenu != null) onMenu.run(); });

        Label hint = UIFactory.createText("Press R to restart", 14);
        hint.setTextFill(Constants.UI_TEXT_SECONDARY);

        buttons.getChildren().addAll(restartButton, menuButton, hint);

        content.getChildren().addAll(octopusCanvas, title, stats, sep, bestRow, buttons);
        panel.getChildren().add(content);

        getChildren().addAll(dimBg, confettiCanvas, panel);

        if (newBestScore) spawnConfetti();
        startAnimTimer();
        playEntranceAnimation(panel);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private HBox buildStatRow(String labelText, Label valueLabel) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        Label key = new Label(labelText);
        key.setTextFill(Constants.UI_TEXT_SECONDARY);
        key.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        key.setMinWidth(100);
        row.getChildren().addAll(key, valueLabel);
        return row;
    }

    // ─── Confetti ─────────────────────────────────────────────────────────────

    private void spawnConfetti() {
        Color[] palette = {
            Constants.SPARKLE_GOLD, Constants.TEAL_ACCENT, Constants.HEART_PINK,
            Color.web("#7ec8e3"), Color.web("#f9c74f"), Color.web("#90be6d"),
            Color.web("#ff9f1c"), Color.web("#c77dff")
        };
        for (int i = 0; i < 80; i++) {
            ConfettiPiece p = new ConfettiPiece();
            p.x          = MathUtil.randomRange(0, Constants.WINDOW_WIDTH);
            p.y          = MathUtil.randomRange(-Constants.WINDOW_HEIGHT * 0.5, 0);
            p.vx         = MathUtil.randomRange(-70, 70);
            p.vy         = MathUtil.randomRange(90, 220);
            p.rotation   = MathUtil.randomRange(0, 360);
            p.rotSpeed   = MathUtil.randomRange(-200, 200);
            p.maxLifetime = MathUtil.randomRange(2.5, 4.5);
            p.lifetime   = p.maxLifetime;
            p.w          = MathUtil.randomRange(6, 13);
            p.h          = MathUtil.randomRange(4, 9);
            p.color      = palette[(int)(Math.random() * palette.length)];
            confetti.add(p);
        }
    }

    // ─── Animation timer ──────────────────────────────────────────────────────

    private void startAnimTimer() {
        long[] lastNano = {0};
        animTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNano[0] == 0) { lastNano[0] = now; return; }
                double dt = (now - lastNano[0]) / 1_000_000_000.0;
                lastNano[0] = now;

                tickCountUp(dt);
                tickOctopus(dt);
                tickConfetti(dt);
            }
        };
        animTimer.start();
    }

    private void tickCountUp(double dt) {
        if (countUpProgress >= 1.0) return;
        countUpProgress = Math.min(countUpProgress + dt / COUNT_UP_DURATION, 1.0);
        double eased = MathUtil.easeOutCubic(countUpProgress);
        scoreValueLabel.setText(String.valueOf((int)(finalScore * eased)));
        heartsValueLabel.setText(String.valueOf((int)(finalHearts * eased)));
    }

    private void tickOctopus(double dt) {
        octopusRenderer.update(dt, false, true, 0);
        GraphicsContext gc = octopusCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 100, 100);
        octopusRenderer.render(gc, 0, 0, 100, 100);
    }

    private void tickConfetti(double dt) {
        if (confetti.isEmpty()) return;
        GraphicsContext gc = confettiCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        for (int i = confetti.size() - 1; i >= 0; i--) {
            ConfettiPiece p = confetti.get(i);
            p.lifetime -= dt;
            if (p.lifetime <= 0) { confetti.remove(i); continue; }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vy += 80 * dt; // gravity
            p.rotation += p.rotSpeed * dt;
            gc.save();
            gc.setGlobalAlpha(p.lifetime / p.maxLifetime);
            gc.setFill(p.color);
            gc.translate(p.x, p.y);
            gc.rotate(p.rotation);
            gc.fillRect(-p.w / 2, -p.h / 2, p.w, p.h);
            gc.restore();
        }
    }

    private void stopTimer() {
        if (animTimer != null) { animTimer.stop(); animTimer = null; }
    }

    // ─── Entrance animation ───────────────────────────────────────────────────

    private void playEntranceAnimation(StackPane panel) {
        FadeTransition fade = new FadeTransition(Duration.millis(250), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        panel.setTranslateY(Constants.WINDOW_HEIGHT);
        TranslateTransition slide = new TranslateTransition(Duration.millis(350), panel);
        slide.setFromY(Constants.WINDOW_HEIGHT);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        slide.play();
    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setOnRestart(Runnable handler) { this.onRestart = handler; }
    public void setOnMenu(Runnable handler)    { this.onMenu    = handler; }

    // ─── Inner class ──────────────────────────────────────────────────────────

    private static class ConfettiPiece {
        double x, y, vx, vy, rotation, rotSpeed, lifetime, maxLifetime, w, h;
        Color color;
    }
}
