package com.eneelkant.bubbleshooter;

import android.app.Application;

/**
 * Application entry. Mobile Ads SDK is initialized from activities after
 * consent is gathered (UMP), not eagerly here.
 */
public class BubbleShooterApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
