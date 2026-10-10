package com.eneelkant.bubbleshooter.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Pure game logic for Bubble Shooter. UI/rendering lives outside this class.
 */
public final class GameEngine {
    public static final int POINTS_PER_BUBBLE = 10;
    public static final int POINTS_PER_FLOATER = 20;

    private final Random random;
    private LevelConfig config;
    private BubbleGrid grid;
    private GamePhase phase;
    private int score;
    private int shotsRemaining;
    private int level;
    private BubbleColor currentBubble;
    private BubbleColor nextBubble;
    private GamePhase phaseBeforePause = GamePhase.READY;

    // Flying bubble state in normalized playfield coords (0..1 width/height).
    private float flyerX;
    private float flyerY;
    private float flyerVx;
    private float flyerVy;
    private boolean flyerActive;

    public GameEngine(long seed) {
        this.random = new Random(seed);
        startLevel(1);
    }

    public GameEngine() {
        this(System.currentTimeMillis());
    }

    public void startLevel(int levelNumber) {
        this.level = Math.max(1, levelNumber);
        this.config = LevelConfig.forLevel(level);
        this.grid = new BubbleGrid(config.rows, config.columns);
        this.grid.fillTopRows(config.filledRows, config.colorCount, random);
        this.shotsRemaining = config.shotLimit;
        this.phase = GamePhase.READY;
        this.flyerActive = false;
        refillShooter();
    }

    public void restartLevel() {
        int keepScore = score;
        startLevel(level);
        this.score = keepScore;
    }

    public void fullRestart() {
        this.score = 0;
        startLevel(1);
    }

    private void refillShooter() {
        if (currentBubble == null) {
            currentBubble = BubbleColor.randomFrom(random, config.colorCount);
        }
        nextBubble = BubbleColor.randomFrom(random, availableColors().size());
        // Prefer colors still on the board when possible.
        List<BubbleColor> available = availableColors();
        if (!available.isEmpty()) {
            nextBubble = available.get(random.nextInt(available.size()));
            if (!available.contains(currentBubble)) {
                currentBubble = available.get(random.nextInt(available.size()));
            }
        }
    }

    public List<BubbleColor> availableColors() {
        Set<BubbleColor> set = new HashSet<>();
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.maxColumnsForRow(r); c++) {
                BubbleColor color = grid.get(r, c);
                if (color != null) {
                    set.add(color);
                }
            }
        }
        if (set.isEmpty()) {
            for (int i = 0; i < config.colorCount; i++) {
                set.add(BubbleColor.fromOrdinal(i));
            }
        }
        return new ArrayList<>(set);
    }

    public void pause() {
        if (phase == GamePhase.PAUSED
                || phase == GamePhase.GAME_OVER
                || phase == GamePhase.LEVEL_COMPLETE) {
            return;
        }
        phaseBeforePause = phase;
        phase = GamePhase.PAUSED;
    }

    public void resume() {
        if (phase == GamePhase.PAUSED) {
            phase = phaseBeforePause == GamePhase.PAUSED ? GamePhase.READY : phaseBeforePause;
        }
    }

    public boolean canShoot() {
        return (phase == GamePhase.READY || phase == GamePhase.AIMING)
                && shotsRemaining > 0
                && currentBubble != null
                && !flyerActive;
    }

    /**
     * Aim angle in radians. 0 = straight up, positive = right.
     * Clamped to a safe upward cone.
     */
    public float clampAimAngle(float angleRadians) {
        float min = (float) Math.toRadians(-75);
        float max = (float) Math.toRadians(75);
        return Math.max(min, Math.min(max, angleRadians));
    }

    public void setAiming() {
        if (phase == GamePhase.READY) {
            phase = GamePhase.AIMING;
        }
    }

    public boolean shoot(float angleRadians) {
        if (!canShoot()) {
            return false;
        }
        float angle = clampAimAngle(angleRadians);
        float speed = 1.55f;
        flyerVx = (float) Math.sin(angle) * speed;
        flyerVy = (float) -Math.cos(angle) * speed;
        flyerX = 0.5f;
        flyerY = 0.92f;
        flyerActive = true;
        shotsRemaining--;
        phase = GamePhase.FLYING;
        return true;
    }

    /**
     * Advances the flying bubble. {@code dt} is seconds.
     * Wall bounce uses normalized width [0,1].
     */
    public void update(float dt) {
        if (phase != GamePhase.FLYING || !flyerActive) {
            return;
        }
        float radius = 0.045f;
        flyerX += flyerVx * dt;
        flyerY += flyerVy * dt;

        if (flyerX < radius) {
            flyerX = radius;
            flyerVx = Math.abs(flyerVx);
        } else if (flyerX > 1f - radius) {
            flyerX = 1f - radius;
            flyerVx = -Math.abs(flyerVx);
        }

        if (flyerY <= radius) {
            snapToCeiling();
            return;
        }

        GridPos hit = findCollision(radius);
        if (hit != null) {
            snapNear(hit);
        }
    }

    private GridPos findCollision(float radius) {
        float cellW = 1f / grid.getColumns();
        float cellH = 0.72f / grid.getRows();
        for (int r = 0; r < grid.getRows(); r++) {
            float rowOffset = (r % 2 == 1) ? cellW * 0.5f : 0f;
            for (int c = 0; c < grid.maxColumnsForRow(r); c++) {
                if (grid.get(r, c) == null) {
                    continue;
                }
                float cx = rowOffset + (c + 0.5f) * cellW;
                float cy = (r + 0.5f) * cellH + 0.06f;
                float dx = flyerX - cx;
                float dy = flyerY - cy;
                float minDist = radius * 2f * 0.92f;
                if (dx * dx + dy * dy <= minDist * minDist) {
                    return new GridPos(r, c);
                }
            }
        }
        return null;
    }

    private void snapToCeiling() {
        int col = Math.min(grid.maxColumnsForRow(0) - 1,
                Math.max(0, Math.round(flyerX * grid.getColumns() - 0.5f)));
        GridPos target = new GridPos(0, col);
        if (grid.get(0, col) != null) {
            target = grid.findSnapCell(0, col, flyerX * grid.getColumns());
        }
        placeAndResolve(target);
    }

    private void snapNear(GridPos hit) {
        GridPos target = grid.findSnapCell(hit.row, hit.col, flyerX * grid.getColumns());
        placeAndResolve(target);
    }

    private void placeAndResolve(GridPos target) {
        flyerActive = false;
        phase = GamePhase.RESOLVING;
        if (target == null || !grid.inBounds(target.row, target.col) || grid.get(target.row, target.col) != null) {
            // No valid snap — treat as lost shot, check end conditions.
            advanceShooterQueue();
            evaluateEndState();
            return;
        }
        grid.set(target.row, target.col, currentBubble);
        int matched = 0;
        List<GridPos> cluster = grid.findCluster(target.row, target.col);
        if (cluster.size() >= BubbleGrid.MIN_MATCH) {
            matched = cluster.size();
            for (GridPos p : cluster) {
                grid.clear(p.row, p.col);
            }
            int floaters = grid.dropFloatingBubbles();
            score += matched * POINTS_PER_BUBBLE + floaters * POINTS_PER_FLOATER;
        }
        advanceShooterQueue();
        evaluateEndState();
    }

    private void advanceShooterQueue() {
        currentBubble = nextBubble;
        nextBubble = null;
        refillShooter();
    }

    private void evaluateEndState() {
        if (grid.isCleared()) {
            score += config.clearBonus;
            phase = GamePhase.LEVEL_COMPLETE;
            return;
        }
        if (grid.reachesCeiling() || shotsRemaining <= 0) {
            phase = GamePhase.GAME_OVER;
            return;
        }
        phase = GamePhase.READY;
    }

    public void advanceToNextLevel() {
        if (phase == GamePhase.LEVEL_COMPLETE) {
            startLevel(level + 1);
        }
    }

    // --- accessors used by UI / tests ---

    public BubbleGrid getGrid() {
        return grid;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public int getScore() {
        return score;
    }

    public int getShotsRemaining() {
        return shotsRemaining;
    }

    public int getLevel() {
        return level;
    }

    public LevelConfig getConfig() {
        return config;
    }

    public BubbleColor getCurrentBubble() {
        return currentBubble;
    }

    public BubbleColor getNextBubble() {
        return nextBubble;
    }

    public boolean isFlyerActive() {
        return flyerActive;
    }

    public float getFlyerX() {
        return flyerX;
    }

    public float getFlyerY() {
        return flyerY;
    }

    /** Test helper: place without physics. */
    public int debugPlace(int row, int col, BubbleColor color) {
        grid.set(row, col, color);
        currentBubble = color;
        int before = grid.countFilled();
        int removed = grid.resolveAfterSnap(row, col);
        // resolveAfterSnap already removed; keep score consistent for tests
        score += removed * POINTS_PER_BUBBLE;
        if (grid.isCleared()) {
            phase = GamePhase.LEVEL_COMPLETE;
        }
        return before - grid.countFilled();
    }

    public void debugSetCurrent(BubbleColor color) {
        this.currentBubble = color;
    }
}
