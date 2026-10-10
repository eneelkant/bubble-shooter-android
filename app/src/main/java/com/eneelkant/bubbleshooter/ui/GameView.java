package com.eneelkant.bubbleshooter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eneelkant.bubbleshooter.game.BubbleColor;
import com.eneelkant.bubbleshooter.game.BubbleGrid;
import com.eneelkant.bubbleshooter.game.GameEngine;
import com.eneelkant.bubbleshooter.game.GamePhase;

/**
 * Portrait SurfaceView renderer with aiming guide and touch controls.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    public interface Listener {
        void onScoreChanged(int score, int shots, int level);

        void onPhaseChanged(GamePhase phase);
    }

    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint aimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint();

    private GameEngine engine;
    private Listener listener;
    private Thread loopThread;
    private volatile boolean running;
    private float aimAngle;
    private boolean aiming;
    private long lastFrameNs;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        setFocusable(true);
        aimPaint.setStyle(Paint.Style.STROKE);
        aimPaint.setStrokeWidth(4f);
        aimPaint.setColor(0xCCFFFFFF);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(36f);
        textPaint.setFakeBoldText(true);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(3f);
        highlightPaint.setColor(0x66FFFFFF);
    }

    public void bind(GameEngine engine, Listener listener) {
        this.engine = engine;
        this.listener = listener;
    }

    public GameEngine getEngine() {
        return engine;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        running = true;
        lastFrameNs = System.nanoTime();
        loopThread = new Thread(this, "BubbleLoop");
        loopThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // no-op
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        running = false;
        if (loopThread != null) {
            try {
                loopThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            loopThread = null;
        }
    }

    @Override
    public void run() {
        SurfaceHolder holder = getHolder();
        while (running) {
            if (engine == null) {
                continue;
            }
            long now = System.nanoTime();
            float dt = Math.min(0.033f, (now - lastFrameNs) / 1_000_000_000f);
            lastFrameNs = now;

            GamePhase before = engine.getPhase();
            if (before != GamePhase.PAUSED) {
                engine.update(dt);
            }
            GamePhase after = engine.getPhase();
            if (listener != null) {
                listener.onScoreChanged(engine.getScore(), engine.getShotsRemaining(), engine.getLevel());
                if (before != after) {
                    post(() -> listener.onPhaseChanged(after));
                }
            }

            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    drawGame(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            try {
                Thread.sleep(12);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void drawGame(Canvas canvas) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                0xFF0B1C2C, 0xFF163A5F, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        BubbleGrid grid = engine.getGrid();
        float cellW = w / (float) grid.getColumns();
        float playTop = h * 0.06f;
        float playHeight = h * 0.72f;
        float cellH = playHeight / grid.getRows();
        float radius = Math.min(cellW, cellH) * 0.42f;

        for (int r = 0; r < grid.getRows(); r++) {
            float rowOffset = (r % 2 == 1) ? cellW * 0.5f : 0f;
            for (int c = 0; c < grid.maxColumnsForRow(r); c++) {
                BubbleColor color = grid.get(r, c);
                if (color == null) {
                    continue;
                }
                float cx = rowOffset + (c + 0.5f) * cellW;
                float cy = playTop + (r + 0.5f) * cellH;
                drawBubble(canvas, cx, cy, radius, color.argb);
            }
        }

        // Shooter
        float shooterX = w * 0.5f;
        float shooterY = h * 0.92f;
        if (engine.getCurrentBubble() != null) {
            drawBubble(canvas, shooterX, shooterY, radius, engine.getCurrentBubble().argb);
        }
        if (engine.getNextBubble() != null) {
            drawBubble(canvas, shooterX + radius * 2.4f, shooterY, radius * 0.7f,
                    engine.getNextBubble().argb);
        }

        if (aiming && engine.canShoot()) {
            float len = h * 0.35f;
            float angle = engine.clampAimAngle(aimAngle);
            float ex = shooterX + (float) Math.sin(angle) * len;
            float ey = shooterY - (float) Math.cos(angle) * len;
            // Dotted aim guide
            float dx = ex - shooterX;
            float dy = ey - shooterY;
            for (int i = 1; i <= 12; i++) {
                float t = i / 12f;
                canvas.drawCircle(shooterX + dx * t, shooterY + dy * t, 5f, aimPaint);
            }
        }

        if (engine.isFlyerActive()) {
            float fx = engine.getFlyerX() * w;
            float fy = engine.getFlyerY() * h;
            drawBubble(canvas, fx, fy, radius, engine.getCurrentBubble().argb);
        }
    }

    private void drawBubble(Canvas canvas, float cx, float cy, float radius, int argb) {
        bubblePaint.setShader(new RadialGradient(
                cx - radius * 0.3f, cy - radius * 0.35f, radius * 1.2f,
                lighten(argb), argb, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, radius, bubblePaint);
        canvas.drawCircle(cx, cy, radius, highlightPaint);
    }

    private static int lighten(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 70);
        int g = Math.min(255, ((argb >> 8) & 0xFF) + 70);
        int b = Math.min(255, (argb & 0xFF) + 70);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (engine == null) {
            return false;
        }
        if (engine.getPhase() == GamePhase.PAUSED
                || engine.getPhase() == GamePhase.GAME_OVER
                || engine.getPhase() == GamePhase.LEVEL_COMPLETE) {
            return true;
        }
        float x = event.getX();
        float y = event.getY();
        float shooterX = getWidth() * 0.5f;
        float shooterY = getHeight() * 0.92f;
        aimAngle = (float) Math.atan2(x - shooterX, shooterY - y);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                aiming = true;
                engine.setAiming();
                return true;
            case MotionEvent.ACTION_UP:
                aiming = false;
                engine.shoot(aimAngle);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
}
