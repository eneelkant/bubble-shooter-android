package com.eneelkant.bubbleshooter.game;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameEngineTest {

    @Test
    public void newGameStartsReadyWithShots() {
        GameEngine engine = new GameEngine(1L);
        assertEquals(GamePhase.READY, engine.getPhase());
        assertEquals(1, engine.getLevel());
        assertTrue(engine.getShotsRemaining() > 0);
        assertTrue(engine.canShoot());
    }

    @Test
    public void aimAngleIsClamped() {
        GameEngine engine = new GameEngine(1L);
        float steep = engine.clampAimAngle((float) Math.toRadians(120));
        assertTrue(steep <= Math.toRadians(75) + 0.0001f);
        float steepLeft = engine.clampAimAngle((float) Math.toRadians(-120));
        assertTrue(steepLeft >= Math.toRadians(-75) - 0.0001f);
    }

    @Test
    public void shootConsumesShotAndStartsFlying() {
        GameEngine engine = new GameEngine(2L);
        int before = engine.getShotsRemaining();
        assertTrue(engine.shoot(0f));
        assertEquals(before - 1, engine.getShotsRemaining());
        assertEquals(GamePhase.FLYING, engine.getPhase());
        assertTrue(engine.isFlyerActive());
    }

    @Test
    public void pauseAndResumeRestorePhase() {
        GameEngine engine = new GameEngine(3L);
        engine.pause();
        assertEquals(GamePhase.PAUSED, engine.getPhase());
        assertFalse(engine.canShoot());
        engine.resume();
        assertEquals(GamePhase.READY, engine.getPhase());
        assertTrue(engine.canShoot());
    }

    @Test
    public void debugPlaceMatchIncreasesScore() {
        GameEngine engine = new GameEngine(4L);
        // Clear board manually via grid access for a controlled match.
        BubbleGrid grid = engine.getGrid();
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.maxColumnsForRow(r); c++) {
                grid.clear(r, c);
            }
        }
        grid.set(0, 0, BubbleColor.RED);
        grid.set(0, 1, BubbleColor.RED);
        int removed = engine.debugPlace(1, 0, BubbleColor.RED);
        assertTrue(removed >= 3);
        assertTrue(engine.getScore() >= 3 * GameEngine.POINTS_PER_BUBBLE);
    }

    @Test
    public void levelConfigScalesDifficulty() {
        LevelConfig easy = LevelConfig.forLevel(1);
        LevelConfig harder = LevelConfig.forLevel(6);
        assertTrue(harder.colorCount >= easy.colorCount);
        assertTrue(harder.filledRows >= easy.filledRows);
        assertTrue(harder.shotLimit <= easy.shotLimit);
    }

    @Test
    public void cannotShootWhenPausedOrOutOfShots() {
        GameEngine engine = new GameEngine(5L);
        engine.pause();
        assertFalse(engine.shoot(0f));
        engine.resume();
        // Drain shots
        while (engine.getShotsRemaining() > 0 && engine.canShoot()) {
            engine.shoot(0f);
            // Force end flying without waiting for physics
            while (engine.getPhase() == GamePhase.FLYING) {
                engine.update(1f);
            }
            if (engine.getPhase() == GamePhase.GAME_OVER
                    || engine.getPhase() == GamePhase.LEVEL_COMPLETE) {
                break;
            }
        }
        if (engine.getPhase() == GamePhase.READY || engine.getPhase() == GamePhase.AIMING) {
            // If still playable, explicitly verify canShoot tracks remaining shots
            if (engine.getShotsRemaining() == 0) {
                assertFalse(engine.canShoot());
            }
        }
    }
}
