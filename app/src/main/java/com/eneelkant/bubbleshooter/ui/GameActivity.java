package com.eneelkant.bubbleshooter.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.eneelkant.bubbleshooter.R;
import com.eneelkant.bubbleshooter.ads.BannerAdController;
import com.eneelkant.bubbleshooter.ads.ConsentHelper;
import com.eneelkant.bubbleshooter.ads.PrefsHighScoreStore;
import com.eneelkant.bubbleshooter.game.GameEngine;
import com.eneelkant.bubbleshooter.game.GamePhase;
import com.google.android.material.button.MaterialButton;

public class GameActivity extends AppCompatActivity implements GameView.Listener {
    private GameEngine engine;
    private GameView gameView;
    private TextView scoreView;
    private TextView shotsView;
    private TextView levelView;
    private BannerAdController bannerAdController;
    private PrefsHighScoreStore scores;
    private AlertDialog activeDialog;
    private boolean adsStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        View root = findViewById(R.id.game_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            FrameLayout adContainer = findViewById(R.id.ad_view_container);
            adContainer.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });

        scores = new PrefsHighScoreStore(this);
        engine = new GameEngine();
        gameView = findViewById(R.id.game_view);
        gameView.bind(engine, this);

        scoreView = findViewById(R.id.text_score);
        shotsView = findViewById(R.id.text_shots);
        levelView = findViewById(R.id.text_level);

        MaterialButton pause = findViewById(R.id.btn_pause);
        pause.setOnClickListener(v -> showPauseDialog());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showPauseDialog();
            }
        });

        FrameLayout adContainer = findViewById(R.id.ad_view_container);
        bannerAdController = new BannerAdController(this, adContainer);

        ConsentHelper consentHelper = new ConsentHelper(this);
        consentHelper.gatherConsent(canRequestAds -> {
            if (!adsStarted) {
                adsStarted = true;
                bannerAdController.initializeAndLoad(canRequestAds);
            }
        });

        refreshHud();
    }

    private void refreshHud() {
        scoreView.setText(getString(R.string.score_fmt, engine.getScore()));
        shotsView.setText(getString(R.string.shots_fmt, engine.getShotsRemaining()));
        levelView.setText(getString(R.string.level_fmt, engine.getLevel()));
    }

    @Override
    public void onScoreChanged(int score, int shots, int level) {
        runOnUiThread(() -> {
            scoreView.setText(getString(R.string.score_fmt, score));
            shotsView.setText(getString(R.string.shots_fmt, shots));
            levelView.setText(getString(R.string.level_fmt, level));
            scores.setHighScore(score);
            scores.setHighestLevel(level);
        });
    }

    @Override
    public void onPhaseChanged(GamePhase phase) {
        if (phase == GamePhase.LEVEL_COMPLETE) {
            showLevelCompleteDialog();
        } else if (phase == GamePhase.GAME_OVER) {
            showGameOverDialog();
        }
    }

    private void showPauseDialog() {
        if (engine.getPhase() == GamePhase.GAME_OVER
                || engine.getPhase() == GamePhase.LEVEL_COMPLETE) {
            return;
        }
        engine.pause();
        dismissDialog();
        activeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pause_title)
                .setMessage(R.string.pause_message)
                .setPositiveButton(R.string.resume, (d, w) -> engine.resume())
                .setNeutralButton(R.string.restart, (d, w) -> {
                    engine.restartLevel();
                    refreshHud();
                })
                .setNegativeButton(R.string.quit, (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showLevelCompleteDialog() {
        dismissDialog();
        activeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.level_complete_title)
                .setMessage(getString(R.string.level_complete_message, engine.getScore()))
                .setPositiveButton(R.string.next_level, (d, w) -> {
                    engine.advanceToNextLevel();
                    refreshHud();
                })
                .setNegativeButton(R.string.quit, (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showGameOverDialog() {
        dismissDialog();
        scores.setHighScore(engine.getScore());
        activeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.game_over_title)
                .setMessage(getString(R.string.game_over_message,
                        engine.getScore(), scores.getHighScore()))
                .setPositiveButton(R.string.restart, (d, w) -> {
                    engine.fullRestart();
                    refreshHud();
                })
                .setNegativeButton(R.string.quit, (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void dismissDialog() {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
        }
        activeDialog = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (engine != null && engine.getPhase() != GamePhase.GAME_OVER
                && engine.getPhase() != GamePhase.LEVEL_COMPLETE
                && engine.getPhase() != GamePhase.PAUSED) {
            engine.pause();
        }
        if (bannerAdController != null) {
            bannerAdController.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerAdController != null) {
            bannerAdController.resume();
        }
    }

    @Override
    protected void onDestroy() {
        dismissDialog();
        if (bannerAdController != null) {
            bannerAdController.destroy();
        }
        super.onDestroy();
    }

}
