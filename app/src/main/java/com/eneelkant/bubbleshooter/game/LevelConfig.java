package com.eneelkant.bubbleshooter.game;

/** Difficulty parameters for a level number (1-based). */
public final class LevelConfig {
    public final int level;
    public final int rows;
    public final int columns;
    public final int filledRows;
    public final int colorCount;
    public final int shotLimit;
    public final int clearBonus;

    public LevelConfig(int level, int rows, int columns, int filledRows,
                       int colorCount, int shotLimit, int clearBonus) {
        this.level = level;
        this.rows = rows;
        this.columns = columns;
        this.filledRows = filledRows;
        this.colorCount = colorCount;
        this.shotLimit = shotLimit;
        this.clearBonus = clearBonus;
    }

    public static LevelConfig forLevel(int level) {
        int safeLevel = Math.max(1, level);
        int colorCount = Math.min(6, 3 + (safeLevel - 1) / 2);
        int filledRows = Math.min(8, 3 + (safeLevel - 1) / 2);
        int shotLimit = Math.max(12, 28 - safeLevel);
        int clearBonus = 250 + safeLevel * 50;
        return new LevelConfig(safeLevel, 12, 8, filledRows, colorCount, shotLimit, clearBonus);
    }
}
