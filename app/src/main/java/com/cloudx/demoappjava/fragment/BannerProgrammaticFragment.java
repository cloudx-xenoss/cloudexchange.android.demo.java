package com.cloudx.demoappjava.fragment;

import static com.cloudx.demoappjava.loglistview.LogListViewKt.setupLogListView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cloudx.demoappjava.R;
import com.cloudx.demoappjava.dynamic.AdContainerLayout;

import java.util.ArrayList;
import java.util.List;

import io.cloudx.sdk.AdViewListener;
import io.cloudx.sdk.CloudXAd;
import io.cloudx.sdk.CloudXAdError;
import io.cloudx.sdk.CloudXAdView;
import io.cloudx.sdk.internal.AdViewSize;
import io.cloudx.sdk.internal.CloudXLogger;

public abstract class BannerProgrammaticFragment extends Fragment {

    private static final String KEY_PLACEMENTS = "KEY_PLACEMENTS";
    private static final String KEY_LOG_TAG = "KEY_LOG_TAG";

    private List<CloudXAdView> bannerAdViews = new ArrayList<>();

    private LinearLayout llAds;
    private Button loadShowButton;
    private Button stopButton;

    private ArrayList<String> placements;
    private String logTag;

    public BannerProgrammaticFragment() {
        super(R.layout.fragment_banner_programmatic);
    }

    // Children must implement this
    protected abstract String logTagFilterRule(String logTag, String forTag);

    // Children must implement this
    protected abstract CloudXAdView createAdView(Activity activity, String placementName, AdViewListener listener);

    protected abstract AdViewSize getAdViewSize();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        placements = args.getStringArrayList(KEY_PLACEMENTS);
        logTag = args.getString(KEY_LOG_TAG);

        loadShowButton = view.findViewById(R.id.btn_load_show);
        stopButton = view.findViewById(R.id.btn_stop);
        llAds = view.findViewById(R.id.llAds);

        loadShowButton.setOnClickListener(v -> onLoadShowClick());
        stopButton.setOnClickListener(v -> onStopClick());

        // Prepare ad containers for each placement
        System.out.println("hop: " + placements);
        for (String placement : placements) {
            System.out.println("hop: banner placements " + placement);
        }

        if (placements != null) {
            for (String placementName : placements) {
                if (placementName == null || placementName.trim().isEmpty()) continue;
                AdContainerLayout adContainer = new AdContainerLayout(requireContext());
                adContainer.setPlacement(placementName, getAdViewSize());
                llAds.addView(adContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }

        updateButtonVisibility();

        // If you have a Java version of setupLogListView, use it here (optional)
        setupLogListView(view.findViewById(R.id.log_list), forTag -> logTagFilterRule(logTag, forTag));
    }

    @Override
    public void onResume() {
        super.onResume();
        for (CloudXAdView view : bannerAdViews) {
            view.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (CloudXAdView view : bannerAdViews) {
            view.hide();
        }
    }

    private void onLoadShowClick() {
        destroyBanners();
        List<CloudXAdView> newBannerAdViews = new ArrayList<>();
        int childIndex = 0;
        if (placements != null) {
            for (String placementName : placements) {
                if (placementName == null || placementName.trim().isEmpty()) continue;
                CloudXAdView bannerAdView = createAdView(requireActivity(), placementName, createBannerListener(placementName));
                if (bannerAdView == null) {
                    CloudXLogger.INSTANCE.error(logTag, "Can't create banner ad: SDK is not initialized or " + placementName + " placement is missing in SDK config", false);
                } else {
                    CloudXLogger.INSTANCE.info(logTag, "Banner ad created for \"" + placementName + "\" placement", false);
                    bannerAdView.setVisibility(View.VISIBLE);
                    View adContainerView = llAds.getChildAt(childIndex);
                    if (adContainerView instanceof AdContainerLayout) {
                        ((AdContainerLayout) adContainerView).addAdView(bannerAdView);
                    }
                    newBannerAdViews.add(bannerAdView);
                }
                childIndex++;
            }
        }
        bannerAdViews = newBannerAdViews;
        updateButtonVisibility();
    }

    private void onStopClick() {
        destroyBanners();
        updateButtonVisibility();
    }

    private void destroyBanners() {
        for (CloudXAdView view : bannerAdViews) {
            view.destroy();
        }
        bannerAdViews.clear();
        llAds.removeAllViews();

        // Recreate containers
        if (placements != null) {
            for (String placementName : placements) {
                if (placementName == null || placementName.trim().isEmpty()) continue;
                AdContainerLayout adContainer = new AdContainerLayout(requireContext());
                adContainer.setPlacement(placementName, getAdViewSize());
                llAds.addView(adContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
        CloudXLogger.INSTANCE.info(logTag, "Banner ads destroyed for placements: " + (placements != null ? placements.toString() : ""), false);
    }

    private void updateButtonVisibility() {
        boolean hasBanner = !bannerAdViews.isEmpty();
        loadShowButton.setVisibility(hasBanner ? View.GONE : View.VISIBLE);
        stopButton.setVisibility(hasBanner ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyBanners();
    }

    private AdViewListener createBannerListener(final String placementName) {
        return new AdViewListener() {
            @Override
            public void onAdHidden(@NonNull CloudXAd cloudXAd) {

            }

            @Override
            public void onAdShowFailed(@NonNull CloudXAdError cloudXAdError) {

            }

            @Override
            public void onAdShowSuccess(@NonNull CloudXAd cloudXAd) {

            }

            @Override
            public void onAdLoadFailed(@NonNull CloudXAdError cloudXAdError) {

            }

            @Override
            public void onAdLoadSuccess(@NonNull CloudXAd cloudXAd) {

            }

            @Override
            public void onAdClicked(@NonNull CloudXAd cloudXAd) {

            }

            // Optionally implement more methods if your BasePublisherListener does more
            @Override
            public void onAdClosedByUser(String pn) {
                CloudXLogger.INSTANCE.info(logTag, "Ad closed by user: " + pn, false);
                destroyBanners();
                updateButtonVisibility();
            }
        };
    }

    // Factory method for fragment arguments
    public static Bundle createArgs(ArrayList<String> placements, String logTag) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_PLACEMENTS, placements);
        bundle.putString(KEY_LOG_TAG, logTag);
        return bundle;
    }
}
