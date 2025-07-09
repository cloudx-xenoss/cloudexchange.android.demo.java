package com.cloudx.demoappjava.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudx.demoappjava.CloudXInitializer;
import com.cloudx.demoappjava.FullPageAdFragment;
import com.cloudx.demoappjava.InitializationListener;
import com.cloudx.demoappjava.InitializationState;
import com.cloudx.demoappjava.InterstitialFragment;
import com.cloudx.demoappjava.PlacementTypeSelectorFragment;
import com.cloudx.demoappjava.R;
import com.cloudx.demoappjava.RewardedFragment;
import com.cloudx.demoappjava.Settings;
import com.cloudx.demoappjava.SettingsFragment;
import com.cloudx.demoappjava.SettingsRepoKt;
import com.cloudx.demoappjava.dynamic.ConfigurationManager;
import com.cloudx.demoappjava.fragment.BannerProgrammaticFragment;
import com.cloudx.demoappjava.fragment.MRECProgrammaticFragment;
import com.cloudx.demoappjava.fragment.NativeAdMediumProgrammaticFragment;
import com.cloudx.demoappjava.fragment.NativeAdSmallProgrammaticFragment;
import com.cloudx.demoappjava.fragment.StandardBannerProgrammaticFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

import io.cloudx.sdk.CloudX;
import io.cloudx.sdk.CloudXPrivacy;
import io.cloudx.sdk.internal.CloudXLogger;
import io.cloudx.sdk.testing.SdkEnvironment;
import io.cloudx.sdk.testing.SdkOverridesProvider;

public class MainActivity extends AppCompatActivity {

    private InitializationState initState = InitializationState.NotInitialized; // or get from CloudXInitializer
    private ConfigurationManager configurationManager;

    private LinearProgressIndicator progressBar;
    private BottomNavigationView bottomNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        configurationManager = new ConfigurationManager(this);

        progressBar = findViewById(R.id.progress);
        bottomNavBar = findViewById(R.id.bottom_nav);
        setupBottomNavBar(bottomNavBar);

        SdkEnvironment.INSTANCE.setOverridesProvider(new SdkOverridesProvider() {
            @Override
            public String getBundleOverride() {
                if (configurationManager.getCurrentConfig() != null) {
                    configurationManager.getCurrentConfig().getSDKConfiguration();
                    return configurationManager.getCurrentConfig().getSDKConfiguration().getBundle();
                }
                return null;
            }

            @Override
            public String getIFAOverride() {
                if (configurationManager.getCurrentConfig() != null) {
                    configurationManager.getCurrentConfig().getSDKConfiguration();
                    return configurationManager.getCurrentConfig().getSDKConfiguration().getIfa();
                }
                return null;
            }
        });

        CloudXInitializer.getInstance().addListener(new InitializationListener() {
            @Override
            public void onCloudXInitializationStatus(InitializationState state) {
                initState = state;
                supportInvalidateOptionsMenu();

                if (state == InitializationState.InProgress) {
                    progressBar.show();
                } else {
                    progressBar.hide();
                }

                System.out.println("hop: " + state);
            }
        });

        CloudXLogger.setLogEnabled(true);

//        MetaAudienceNetwork.enableMetaAudienceNetworkTestMode(true);

    }

    private void setupBottomNavBar(BottomNavigationView nav) {
        nav.setOnItemSelectedListener(item -> {
            String tag = String.valueOf(item.getItemId());
            Bundle bundle = null;
            Class fragmentClass = null;

            Settings settings = SettingsRepoKt.settings(this);

            int itemId = item.getItemId();
            if (itemId == R.id.menu_banner) {// You would have to implement PlacementTypeSelectorFragment and PlacementItem in Java!
                fragmentClass = PlacementTypeSelectorFragment.class;
                bundle = PlacementTypeSelectorFragment.Companion.bundleFrom(Arrays.asList(new PlacementTypeSelectorFragment.Companion.PlacementItem(getString(R.string.banner_standard), StandardBannerProgrammaticFragment.class, BannerProgrammaticFragment.createArgs(settings.getBannerPlacementNames(), "StandardBannerFragment")), new PlacementTypeSelectorFragment.Companion.PlacementItem(getString(R.string.mrec), MRECProgrammaticFragment.class, BannerProgrammaticFragment.createArgs(settings.getMrecPlacementNames(), "MRECFragment"))));
            } else if (itemId == R.id.menu_native) {
                fragmentClass = PlacementTypeSelectorFragment.class;
                bundle = PlacementTypeSelectorFragment.Companion.bundleFrom(Arrays.asList(new PlacementTypeSelectorFragment.Companion.PlacementItem(getString(R.string.small), NativeAdSmallProgrammaticFragment.class, BannerProgrammaticFragment.createArgs(settings.getNativeSmallPlacementNames(), "NativeAdSmallFragment")), new PlacementTypeSelectorFragment.Companion.PlacementItem(getString(R.string.medium), NativeAdMediumProgrammaticFragment.class, BannerProgrammaticFragment.createArgs(settings.getNativeMediumPlacementNames(), "NativeAdMediumFragment"))));
            } else if (itemId == R.id.menu_interstitial) {
                fragmentClass = InterstitialFragment.class;
                bundle = FullPageAdFragment.Companion.createArgs(settings.getInterstitialPlacementNames());
            } else if (itemId == R.id.menu_rewarded) {
                fragmentClass = RewardedFragment.class;
                bundle = FullPageAdFragment.Companion.createArgs(settings.getRewardedPlacementNames());
            } else if (itemId == R.id.menu_settings) {
                fragmentClass = SettingsFragment.class;
                bundle = new Bundle();
            }

            if (fragmentClass != null) {
                String fragTag = tag;
                if (getSupportFragmentManager().findFragmentByTag(fragTag) == null) {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(com.google.android.material.R.anim.abc_fade_in, com.google.android.material.R.anim.abc_fade_out).replace(R.id.fragment_container, fragmentClass, bundle, fragTag).commit();
                }
                return true;
            }
            return false;
        });

        nav.setSelectedItemId(R.id.menu_settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            boolean visible = false;
            switch (initState) {
                case NotInitialized:
                    visible = menuItem.getItemId() == R.id.menu_init;
                    break;
                case InProgress:
                    visible = menuItem.getItemId() == R.id.menu_init_in_progress;
                    break;
                case FailedToInitialize:
                    visible = menuItem.getItemId() == R.id.menu_init_retry;
                    break;
                case Initialized:
                    visible = menuItem.getItemId() == R.id.menu_init_success || menuItem.getItemId() == R.id.menu_stop;
                    break;
            }
            menuItem.setVisible(visible);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_init || itemId == R.id.menu_init_retry) {
            initializeCloudX();
            return true;
        } else if (itemId == R.id.menu_stop) {
            stopCloudX();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeCloudX() {
        Settings settings = SettingsRepoKt.settings(this);
        String appKey = settings.getAppKey();
        String endpoint = settings.getInitUrl();

        CloudXLogger.INSTANCE.info(TAG, "🚀 Starting SDK init with appKey: " + appKey + ", endpoint: " + endpoint, false);

        CloudXInitializer.getInstance().initializeCloudX(this, settings, null, TAG,
                result -> runOnUiThread(() -> {
                    String resultMsg = result.getInitialized() ? INIT_SUCCESS : INIT_FAILURE + result.getDescription();
                    if (result.getInitialized()) {
                        CloudXLogger.INSTANCE.info(TAG, resultMsg, false);
                    } else {
                        CloudXLogger.INSTANCE.error(TAG, resultMsg, false);
                    }
                    shortSnackbar(bottomNavBar, resultMsg);
                }));
    }

    private void stopCloudX() {
        CloudX.deinitialize();
        CloudX.setHashedUserId("");
        CloudX.setPrivacy(new CloudXPrivacy());
        CloudX.setTargeting(null);

        CloudXInitializer.getInstance().reset();

        bottomNavBar.setSelectedItemId(R.id.menu_settings);
        shortSnackbar(bottomNavBar, "CloudX SDK stopped!");
    }

    private void shortSnackbar(View anchor, String message) {
        try {
            Snackbar.make(anchor, message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static final String TAG = "MainActivity";
    public static final String INIT_SUCCESS = "Init success!";
    public static final String INIT_FAILURE = "Init failure:";
}
