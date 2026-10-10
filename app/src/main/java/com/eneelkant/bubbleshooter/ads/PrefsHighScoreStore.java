package com.eneelkant.bubbleshooter.ads;

import android.content.Context;
import android.content.SharedPreferences;

import com.eneelkant.bubbleshooter.game.HighScoreStore;

public final class PrefsHighScoreStore implements HighScoreStore {
    private static final String PREFS = "bubble_shooter_scores";
    private static final String KEY_HIGH = "high_score";
    private static final String KEY_LEVEL = "highest_level";

    private final SharedPreferences prefs;

    public PrefsHighScoreStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public int getHighScore() {
        return prefs.getInt(KEY_HIGH, 0);
    }

    @Override
    public void setHighScore(int score) {
        if (score > getHighScore()) {
            prefs.edit().putInt(KEY_HIGH, score).apply();
        }
    }

    @Override
    public int getHighestLevel() {
        return prefs.getInt(KEY_LEVEL, 1);
    }

    @Override
    public void setHighestLevel(int level) {
        if (level > getHighestLevel()) {
            prefs.edit().putInt(KEY_LEVEL, level).apply();
        }
    }
}
