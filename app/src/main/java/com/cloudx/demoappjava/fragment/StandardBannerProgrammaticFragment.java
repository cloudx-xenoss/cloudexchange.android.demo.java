package com.cloudx.demoappjava.fragment;

import android.app.Activity;

import io.cloudx.sdk.AdViewListener;
import io.cloudx.sdk.CloudX;
import io.cloudx.sdk.CloudXAdView;
import io.cloudx.sdk.internal.AdType;
import io.cloudx.sdk.internal.AdViewSize;

public class StandardBannerProgrammaticFragment extends BannerProgrammaticFragment {
    @Override
    protected String logTagFilterRule(String logTag, String forTag) {
        // Implement commonLogTagListRules in Java or stub here
        if (forTag.equals(logTag) || forTag.equals("BannerImpl")) return "Banner";
        return null;
    }

    @Override
    protected CloudXAdView createAdView(Activity activity, String placementName, AdViewListener listener) {
        return CloudX.createBanner(activity, placementName, listener);
    }

    @Override
    protected AdViewSize getAdViewSize() {
        return AdType.Banner.Standard.INSTANCE.getSize();
    }
}
