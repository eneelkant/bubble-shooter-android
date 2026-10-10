package com.eneelkant.bubbleshooter.game;

/** Abstraction over local high-score persistence. */
public interface HighScoreStore {
    int getHighScore();

    void setHighScore(int score);

    int getHighestLevel();

    void setHighestLevel(int level);
}
