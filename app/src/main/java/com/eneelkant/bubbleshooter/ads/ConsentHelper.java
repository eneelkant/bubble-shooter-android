package com.eneelkant.bubbleshooter.ads;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

/**
 * Google User Messaging Platform (UMP) consent for ads privacy requirements.
 * For a general audience; if the app targets children, configure AdMob/Play
 * for Families and child-directed treatment before production release.
 */
public final class ConsentHelper {
    public interface Listener {
        void onConsentFinished(boolean canRequestAds);
    }

    private static final String TAG = "ConsentHelper";

    private final Activity activity;
    private final ConsentInformation consentInformation;

    public ConsentHelper(Activity activity) {
        this.activity = activity;
        this.consentInformation = UserMessagingPlatform.getConsentInformation(activity);
    }

    public void gatherConsent(Listener listener) {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity,
                        (@Nullable FormError formError) -> {
                            if (formError != null) {
                                Log.w(TAG, "Consent form error: " + formError.getMessage());
                            }
                            listener.onConsentFinished(consentInformation.canRequestAds());
                        }),
                formError -> {
                    Log.w(TAG, "Consent info update failed: " + formError.getMessage());
                    // Fail open for offline/dev: still allow ad init; SDK handles no-fill.
                    listener.onConsentFinished(true);
                });
    }

    public boolean canRequestAds() {
        return consentInformation.canRequestAds();
    }
}
