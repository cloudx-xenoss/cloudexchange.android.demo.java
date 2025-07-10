package com.cloudx.demoappjava;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import io.cloudx.sdk.CloudX;
import io.cloudx.sdk.CloudXInitializationListener;
import io.cloudx.sdk.CloudXPrivacy;
import io.cloudx.sdk.internal.CloudXLogger;

public final class CloudXInitializer {

    private InitializationState initState = InitializationState.NotInitialized;

    private static CloudXInitializer sInstance = null;

    private CloudXInitializer() {
    }

    private List<InitializationListener> listeners = new ArrayList<>();

    public static CloudXInitializer getInstance() {
        return (sInstance == null) ? sInstance = new CloudXInitializer() : sInstance;
    }

    public InitializationState getInitState() {
        return initState;
    }

    public void addListener(InitializationListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void initializeCloudX(Activity activity, Settings settings, String hashedUserId, String logTag, CloudXInitializationListener listener) {
        initState = InitializationState.InProgress;
        updateState();

        updateIabTcfGdprAppliesSharedPrefs(activity);

        CloudXPrivacy privacy = new CloudXPrivacy(settings.getGdprConsent(), settings.getAgeRestricted(), settings.getDoNotSell());
        CloudXLogger.INSTANCE.info(logTag, "CloudX privacy set: " + privacy, false);
        CloudX.setPrivacy(privacy);

        CloudX.initialize(activity, new CloudX.InitializationParams(settings.getAppKey(), settings.getInitUrl(), hashedUserId), result -> {
            if (result.getInitialized()) {
                initState = InitializationState.Initialized;
            } else {
                initState = InitializationState.FailedToInitialize;
            }
            updateState();
            if (listener != null) {
                listener.onCloudXInitializationStatus(result);
            }
        });
    }

    public void reset() {
        initState = InitializationState.NotInitialized;
        updateState();
    }

    private void updateState() {
        if (listeners != null) {
            for (InitializationListener listener : listeners) {
                listener.onCloudXInitializationStatus(initState);
            }
        }
    }

    // Static helper for preference writing
    private static void updateIabTcfGdprAppliesSharedPrefs(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean gdprApplies = toPrivacyFlag(prefs, context.getString(R.string.pref_iab_tcf_gdpr_applies));
        String iabGdprAppliesKey = "IABTCF_gdprApplies";
        SharedPreferences.Editor editor = prefs.edit();
        if (gdprApplies == null) {
            editor.remove(iabGdprAppliesKey);
        } else if (gdprApplies) {
            editor.putInt(iabGdprAppliesKey, 1);
        } else {
            editor.putInt(iabGdprAppliesKey, 0);
        }
        editor.apply();
    }

    // Static helper, mimics SharedPreferences.toPrivacyFlag
    private static Boolean toPrivacyFlag(SharedPreferences prefs, String key) {
        String val = prefs.getString(key, null);
        return toPrivacyFlag(val);
    }

    // Static helper, mimics String.toPrivacyFlag
    private static Boolean toPrivacyFlag(String val) {
        if ("0".equals(val)) return false;
        if ("1".equals(val)) return true;
        return null;
    }
}
