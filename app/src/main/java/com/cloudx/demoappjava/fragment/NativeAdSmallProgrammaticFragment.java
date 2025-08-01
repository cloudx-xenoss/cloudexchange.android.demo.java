package com.cloudx.demoappjava.fragment;

import android.app.Activity;

import io.cloudx.sdk.AdViewListener;
import io.cloudx.sdk.CloudX;
import io.cloudx.sdk.CloudXAdView;
import io.cloudx.sdk.internal.AdType;
import io.cloudx.sdk.internal.AdViewSize;

public class NativeAdSmallProgrammaticFragment extends BannerProgrammaticFragment {
    @Override
    protected String logTagFilterRule(String logTag, String forTag) {
        if (forTag.equals(logTag) || forTag.equals("BannerImpl")) return "NativeAdSmall";
        return null;
    }

    @Override
    protected CloudXAdView createAdView(Activity activity, String placementName, AdViewListener listener) {
        return CloudX.createNativeAdSmall(activity, placementName, listener);
    }

    @Override
    protected AdViewSize getAdViewSize() {
        return AdType.Native.Small.INSTANCE.getSize();
    }
}
