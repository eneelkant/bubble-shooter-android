package com.eneelkant.bubbleshooter.ads;

import android.app.Activity;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eneelkant.bubbleshooter.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

/**
 * Anchored adaptive banner helper. Keeps a single AdView, handles load failures,
 * and destroys cleanly to avoid leaks / duplicate requests.
 */
public final class BannerAdController {
    private static final String TAG = "BannerAdController";

    private final Activity activity;
    private final FrameLayout container;
    @Nullable
    private AdView adView;
    private boolean sdkReady;
    private boolean destroyed;
    private boolean layoutRequested;

    public BannerAdController(Activity activity, FrameLayout container) {
        this.activity = activity;
        this.container = container;
    }

    public void initializeAndLoad(boolean canRequestAds) {
        if (destroyed) {
            return;
        }
        if (!canRequestAds) {
            Log.i(TAG, "Ads not requested — consent not granted. Gameplay continues.");
            container.setVisibility(android.view.View.GONE);
            return;
        }
        new Thread(() -> MobileAds.initialize(activity, status ->
                activity.runOnUiThread(() -> {
                    sdkReady = true;
                    loadBanner();
                }))).start();
    }

    public void loadBanner() {
        if (destroyed || !sdkReady) {
            return;
        }
        if (container.getWidth() <= 0 && !layoutRequested) {
            layoutRequested = true;
            container.post(this::loadBanner);
            return;
        }
        destroyAdViewOnly();

        adView = new AdView(activity);
        adView.setAdUnitId(activity.getString(R.string.admob_banner_unit_id));
        AdSize adSize = adaptiveSize();
        adView.setAdSize(adSize);
        container.setVisibility(android.view.View.VISIBLE);
        container.removeAllViews();
        container.addView(adView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.w(TAG, "Banner failed: " + loadAdError.getMessage());
                // Hide empty ad chrome; gameplay must continue offline.
                container.setVisibility(android.view.View.GONE);
            }

            @Override
            public void onAdLoaded() {
                container.setVisibility(android.view.View.VISIBLE);
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }

    private AdSize adaptiveSize() {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int widthPixels = container.getWidth();
        if (widthPixels <= 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            widthPixels = activity.getWindowManager().getCurrentWindowMetrics().getBounds().width();
        }
        if (widthPixels <= 0) {
            widthPixels = metrics.widthPixels;
        }
        int adWidth = Math.max(1, (int) (widthPixels / metrics.density));
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    public void resume() {
        if (adView != null) {
            adView.resume();
        }
    }

    public void pause() {
        if (adView != null) {
            adView.pause();
        }
    }

    public void destroy() {
        destroyed = true;
        destroyAdViewOnly();
    }

    private void destroyAdViewOnly() {
        if (adView != null) {
            ViewGroup parent = (ViewGroup) adView.getParent();
            if (parent != null) {
                parent.removeView(adView);
            }
            adView.destroy();
            adView = null;
        }
        container.removeAllViews();
    }
}
