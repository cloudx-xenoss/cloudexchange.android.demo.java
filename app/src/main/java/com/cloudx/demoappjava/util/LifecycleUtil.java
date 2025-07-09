package com.cloudx.demoappjava.util;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class LifecycleUtil {

    public static void repeatOnStart(LifecycleOwner owner, Runnable block) {
        owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(LifecycleOwner owner) {
                block.run();
            }
        });
    }
}
