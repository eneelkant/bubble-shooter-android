package com.eneelkant.bubbleshooter.game;

/**
 * Playable bubble colors. Drawn programmatically — no third-party art assets.
 */
public enum BubbleColor {
    RED(0xFFE53935),
    BLUE(0xFF1E88E5),
    GREEN(0xFF43A047),
    YELLOW(0xFFFDD835),
    PURPLE(0xFF8E24AA),
    ORANGE(0xFFFB8C00);

    public final int argb;

    BubbleColor(int argb) {
        this.argb = argb;
    }

    public static BubbleColor fromOrdinal(int ordinal) {
        BubbleColor[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException("Invalid color ordinal: " + ordinal);
        }
        return values[ordinal];
    }

    public static BubbleColor randomFrom(java.util.Random random, int colorCount) {
        int count = Math.max(2, Math.min(colorCount, values().length));
        return values()[random.nextInt(count)];
    }
}
