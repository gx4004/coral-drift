package com.coraldrift.core;

import com.coraldrift.audio.AudioManager;
import com.coraldrift.entity.CoralObstacle;
import com.coraldrift.entity.GoldenHeart;
import com.coraldrift.entity.HeartCollectible;
import com.coraldrift.entity.Player;
import com.coraldrift.entity.ShieldBubble;
import com.coraldrift.graphics.*;
import com.coraldrift.spawner.HeartSpawner;
import com.coraldrift.spawner.ObstacleSpawner;
import com.coraldrift.util.Constants;
import com.coraldrift.util.MathUtil;
import com.coraldrift.util.SaveManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * Main game engine that orchestrates all gameplay systems.
 */
public class GameEngine implements CollisionSystem.HeartCollectCallback,
                                   CollisionSystem.NearMissCallback {

    // ─── Observer for HUD score pops ──────────────────────────────────────────
    public interface HeartCollectObserver {
        void onHeartCollected(double worldX, double worldY, int points, boolean isGolden);
    }

    private HeartCollectObserver heartObserver;

    public void setHeartCollectObserver(HeartCollectObserver obs) {
        this.heartObserver = obs;
    }

    // ─── Observer for jump blup bubble ────────────────────────────────────────
    public interface JumpObserver {
        void onJump(double x, double y);
    }

    private JumpObserver jumpObserver;

    public void setJumpObserver(JumpObserver obs) {
        this.jumpObserver = obs;
    }

    // ─── Core systems ─────────────────────────────────────────────────────────
    private final GameState state;
    private final CollisionSystem collisionSystem;

    // Entities
    private final Player player;

    // Spawners
    private final ObstacleSpawner obstacleSpawner;
    private final HeartSpawner heartSpawner;

    // Graphics systems
    private final ParallaxBackground background;
    private final BubbleEmitter bubbleEmitter;
    private final ParticleSystem particleSystem;
    private final ScreenEffects screenEffects;
    private final FishSwarm fishSwarm;
    private final CrabWalker crabWalker;

    // Shield bubbles
    private final List<ShieldBubble> shieldBubbles = new ArrayList<>();
    private CoralObstacle lastShieldCheckCoral = null;
    private static final double SHIELD_SPAWN_CHANCE = 0.07;

    // Floating text (world-space HUD elements)
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    // Chain trail timing
    private double chainTrailTimer = 0;
    private static final double CHAIN_TRAIL_INTERVAL = 0.05;

    // Landing detection
    private boolean wasInAir = false;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public GameEngine() {
        state = new GameState(Constants.BASE_SCROLL_SPEED);
        collisionSystem = new CollisionSystem();

        player = new Player();

        obstacleSpawner = new ObstacleSpawner();
        heartSpawner = new HeartSpawner();

        background = new ParallaxBackground();
        bubbleEmitter = new BubbleEmitter();
        particleSystem = new ParticleSystem();
        screenEffects = new ScreenEffects();
        fishSwarm = new FishSwarm();
        crabWalker = new CrabWalker();

        AudioManager.getInstance().initialize();

        state.setBestScores(
            SaveManager.getInstance().getBestScore(),
            SaveManager.getInstance().getBestHearts()
        );
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    public void update(double deltaTime) {
        screenEffects.update(deltaTime);
        bubbleEmitter.update(deltaTime);
        particleSystem.update(deltaTime);
        updateFloatingTexts(deltaTime);

        if (!state.isPlaying()) {
            background.update(deltaTime, Constants.BASE_SCROLL_SPEED * 0.3);
            return;
        }

        state.update(deltaTime);
        state.addDistanceScore(deltaTime, Constants.SCORE_PER_SECOND);
        state.updateScrollSpeed(
            Constants.BASE_SCROLL_SPEED, Constants.MAX_SCROLL_SPEED,
            Constants.SPEED_INCREMENT, Constants.SPEED_INCREASE_INTERVAL
        );

        double scrollSpeed  = state.getScrollSpeed();
        double scrollAmount = scrollSpeed * deltaTime;

        background.update(deltaTime, scrollSpeed);
        fishSwarm.update(deltaTime, scrollSpeed);
        crabWalker.update(deltaTime, scrollSpeed);

        // Shield bubbles
        for (ShieldBubble sb : shieldBubbles) {
            sb.scroll(scrollAmount);
            sb.update(deltaTime);
        }
        shieldBubbles.removeIf(sb -> sb.isOffScreen() || sb.isCollected());

        CoralObstacle latestCoral = obstacleSpawner.getLastSpawnedCoral();
        if (latestCoral != null && latestCoral != lastShieldCheckCoral) {
            lastShieldCheckCoral = latestCoral;
            if (MathUtil.randomChance(SHIELD_SPAWN_CHANCE)) {
                double sbX = latestCoral.getX() + MathUtil.randomRange(-80, 150);
                double sbY = MathUtil.randomRange(150, Constants.GROUND_Y - 150);
                shieldBubbles.add(new ShieldBubble(sbX, sbY));
            }
        }

        boolean inAirNow = !player.isOnGround();
        player.update(deltaTime);

        if (wasInAir && player.isOnGround()) {
            particleSystem.spawnLandingEffect(player.getCenterX(), Constants.GROUND_Y);
            bubbleEmitter.burstAt(player.getCenterX(), Constants.GROUND_Y, 4);
        }
        wasInAir = inAirNow;

        obstacleSpawner.update(scrollAmount, state.getDifficultyFactor());
        heartSpawner.update(scrollAmount, obstacleSpawner, state.getDifficultyFactor());

        player.setGlowing(state.getChainCount() >= 5);

        if (state.isChainActive()) {
            chainTrailTimer += deltaTime;
            if (chainTrailTimer >= CHAIN_TRAIL_INTERVAL) {
                chainTrailTimer = 0;
                particleSystem.spawnChainTrailParticle(
                    player.getX() + player.getWidth() * 0.3,
                    player.getCenterY(),
                    state.getChainCount()
                );
            }
        }

        checkCollisions();
    }

    // ─── Floating text ────────────────────────────────────────────────────────

    private void updateFloatingTexts(double deltaTime) {
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.lifetime -= deltaTime;
            if (ft.lifetime <= 0) { floatingTexts.remove(i); continue; }
            ft.y += ft.vy * deltaTime;
            ft.alpha = ft.lifetime / ft.maxLifetime;
        }
    }

    private void spawnFloatingText(String text, double x, double y, Color color, double lifetime) {
        FloatingText ft = new FloatingText();
        ft.text = text; ft.x = x; ft.y = y;
        ft.vy = -75; ft.alpha = 1.0;
        ft.maxLifetime = lifetime; ft.lifetime = lifetime;
        ft.color = color;
        floatingTexts.add(ft);
    }

    // ─── Collisions ───────────────────────────────────────────────────────────

    private void checkCollisions() {
        CoralObstacle hitCoral = collisionSystem.checkCoralCollision(
            player, obstacleSpawner.getCorals());

        if (hitCoral != null) {
            if (player.useShield()) {
                hitCoral.setActive(false);
                screenEffects.triggerCollectEffect();
                screenEffects.triggerChromaticAberration();
                particleSystem.spawnHitEffect(player.getCenterX(), player.getCenterY());
                AudioManager.getInstance().playShieldAbsorb();
            } else {
                handleCoralHit(hitCoral);
                return;
            }
        }

        collisionSystem.checkHeartCollisions(player, heartSpawner.getHearts(), this);
        collisionSystem.checkNearMisses(player, obstacleSpawner.getCorals(), this);

        double[] playerBox = player.getHitbox();
        for (ShieldBubble sb : shieldBubbles) {
            if (sb.isCollected()) continue;
            double[] sbBox = sb.getHitbox();
            if (boxesIntersect(playerBox, sbBox)) {
                sb.collect();
                player.activateShield(ShieldBubble.SHIELD_DURATION);
                screenEffects.triggerCollectEffect();
                bubbleEmitter.burstAt(sb.getCenterX(), sb.getCenterY(), 8);
                AudioManager.getInstance().playCollect();
                spawnFloatingText("SHIELD!", sb.getCenterX(), sb.getCenterY() - 30,
                    Color.web("#4ecdc4"), 1.1);
            }
        }
    }

    private boolean boxesIntersect(double[] a, double[] b) {
        return a[0] < b[0] + b[2] && a[0] + a[2] > b[0] &&
               a[1] < b[1] + b[3] && a[1] + a[3] > b[1];
    }

    private void handleCoralHit(CoralObstacle coral) {
        player.die();
        state.breakChain();
        state.setStatus(GameState.Status.GAME_OVER);

        screenEffects.triggerHitEffect();
        screenEffects.triggerChromaticAberration();
        particleSystem.spawnHitEffect(player.getCenterX(), player.getCenterY());
        AudioManager.getInstance().playHit();
        AudioManager.getInstance().playGameOver();

        state.finalizeScore();
        SaveManager.getInstance().updateBestScore(state.getScore());
        SaveManager.getInstance().updateBestHearts(state.getHeartsCollected());
    }

    // ─── Heart callback ───────────────────────────────────────────────────────

    @Override
    public void onHeartCollected(HeartCollectible heart) {
        boolean isGolden = heart instanceof GoldenHeart;
        int scoreValue = isGolden
            ? Constants.HEART_SCORE_VALUE * Constants.GOLDEN_HEART_SCORE_MULTIPLIER
            : Constants.HEART_SCORE_VALUE;

        heart.collect();
        state.collectHeart(scoreValue);

        screenEffects.triggerCollectEffect();

        if (isGolden) {
            particleSystem.spawnGoldenHeartEffect(heart.getCenterX(), heart.getCenterY());
            AudioManager.getInstance().playGoldenCollect();
            spawnFloatingText("✦ GOLDEN! +" + scoreValue,
                heart.getCenterX(), heart.getCenterY() - 30,
                Constants.GOLDEN_HEART_COLOR, 1.3);
        } else {
            particleSystem.spawnHeartCollectEffect(heart.getCenterX(), heart.getCenterY());
            AudioManager.getInstance().playCollect();
            AudioManager.getInstance().playChainCombo(state.getChainCount());
            int pts = scoreValue;
            spawnFloatingText("+" + pts,
                heart.getCenterX(), heart.getCenterY() - 25,
                Constants.SPARKLE_GOLD, Constants.SCORE_POP_LIFETIME);
        }

        bubbleEmitter.burstAt(heart.getCenterX(), heart.getCenterY(), isGolden ? 10 : 5);

        // Trigger happy star expression at chain >= 5
        if (state.getChainCount() >= 5) {
            player.triggerHappyExpression(1.5);
        }

        // Notify HUD observer
        if (heartObserver != null) {
            heartObserver.onHeartCollected(heart.getCenterX(), heart.getCenterY(),
                scoreValue, isGolden);
        }
    }

    // ─── Near-miss callback ───────────────────────────────────────────────────

    @Override
    public void onNearMiss(CoralObstacle coral, double playerCX, double playerCY) {
        state.recordNearMiss();
        particleSystem.spawnNearMissEffect(playerCX, playerCY);
        AudioManager.getInstance().playNearMiss();
        spawnFloatingText("+NEAR MISS!",
            playerCX, playerCY - 40,
            Constants.NEAR_MISS_COLOR, Constants.NEAR_MISS_TEXT_LIFETIME);
    }

    // ─── Render ───────────────────────────────────────────────────────────────

    public void render(GraphicsContext gc) {
        gc.save();
        screenEffects.applyPreRender(gc);

        background.render(gc);
        fishSwarm.render(gc);
        bubbleEmitter.render(gc);

        for (CoralObstacle coral : obstacleSpawner.getCorals()) coral.render(gc);
        for (HeartCollectible heart : heartSpawner.getHearts()) heart.render(gc);
        for (ShieldBubble sb : shieldBubbles) sb.render(gc);

        particleSystem.render(gc);
        crabWalker.render(gc);
        player.render(gc);

        renderFloatingTexts(gc);

        gc.restore();
        screenEffects.renderOverlay(gc);
    }

    private void renderFloatingTexts(GraphicsContext gc) {
        gc.save();
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        for (FloatingText ft : floatingTexts) {
            gc.setGlobalAlpha(ft.alpha);
            gc.setFill(Color.BLACK);
            gc.fillText(ft.text, ft.x - 1, ft.y + 1); // shadow
            gc.setFill(ft.color);
            gc.fillText(ft.text, ft.x, ft.y);
        }
        gc.restore();
    }

    // ─── Input / lifecycle ────────────────────────────────────────────────────

    public void jump() {
        if (state.isPlaying() && !player.isDead()) {
            player.jump();
            AudioManager.getInstance().playJump();
            if (jumpObserver != null) {
                jumpObserver.onJump(player.getCenterX(), player.getY());
            }
        }
    }

    public void releaseJump() { player.releaseJump(); }

    public void startGame() {
        player.reset();
        obstacleSpawner.reset();
        heartSpawner.reset();
        background.reset();
        particleSystem.clear();
        screenEffects.reset();
        fishSwarm.reset();
        crabWalker.reset();
        shieldBubbles.clear();
        floatingTexts.clear();
        lastShieldCheckCoral = null;

        state.reset(Constants.BASE_SCROLL_SPEED);
        state.setBestScores(
            SaveManager.getInstance().getBestScore(),
            SaveManager.getInstance().getBestHearts()
        );

        wasInAir = false;
        chainTrailTimer = 0;
    }

    public void pause()  { if (state.isPlaying())  state.setStatus(GameState.Status.PAUSED); }
    public void resume() { if (state.isPaused())   state.setStatus(GameState.Status.PLAYING); }

    public void togglePause() {
        if (state.isPlaying()) pause(); else if (state.isPaused()) resume();
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public GameState      getState()          { return state; }
    public Player         getPlayer()         { return player; }
    public ParticleSystem getParticleSystem() { return particleSystem; }
    public ScreenEffects  getScreenEffects()  { return screenEffects; }

    // ─── Inner classes ────────────────────────────────────────────────────────

    private static class FloatingText {
        String text;
        double x, y, vy, alpha, lifetime, maxLifetime;
        Color color;
    }
}
