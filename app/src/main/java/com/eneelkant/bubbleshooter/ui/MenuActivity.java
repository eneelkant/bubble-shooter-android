package com.eneelkant.bubbleshooter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.eneelkant.bubbleshooter.R;
import com.eneelkant.bubbleshooter.ads.PrefsHighScoreStore;
import com.google.android.material.button.MaterialButton;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        PrefsHighScoreStore scores = new PrefsHighScoreStore(this);
        TextView highScore = findViewById(R.id.text_high_score);
        highScore.setText(getString(R.string.high_score_fmt, scores.getHighScore()));
        TextView bestLevel = findViewById(R.id.text_best_level);
        bestLevel.setText(getString(R.string.best_level_fmt, scores.getHighestLevel()));

        MaterialButton play = findViewById(R.id.btn_play);
        play.setOnClickListener(v ->
                startActivity(new Intent(this, GameActivity.class)));
    }
}
