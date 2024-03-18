package com.dhl.demp.mydmac;

import android.app.Application;

import com.dhl.demp.dmac.data.network.TipInterceptor;
import com.dhl.demp.dmac.data.repository.AppRepositoryLegacy;
import com.dhl.demp.dmac.utils.ProdTree;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.hilt.android.HiltAndroidApp;
import mydmac.BuildConfig;
import timber.log.Timber;

@HiltAndroidApp
public class LauncherApplication extends Application {
    private static LauncherApplication instance;

    //This repository is used only from legacy Java code
    @Deprecated
    @Inject
    public Lazy<AppRepositoryLegacy> appRepositoryLegacy;

    @Inject
    public Lazy<TipInterceptor> tipInterceptor;
    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        initTimber();
    }

    private void initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ProdTree());
        }
    }

    public static LauncherApplication get() {
        return instance;
    }
}
