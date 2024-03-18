package com.dhl.demp.mydmac.analytics;

import com.dhl.demp.mydmac.LauncherApplication;
import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.AnalyticsApi;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.EmptyCallback;
import com.dhl.demp.mydmac.api.request.AppDownloadEventRequest;
import com.dhl.demp.mydmac.api.request.AppInfoEventRequest;
import com.dhl.demp.mydmac.api.request.DmacApiEventRequest;

import mydmac.BuildConfig;

public class Analytics {
    //protect from creating instances of this class
    private Analytics() {}

    public static void sendAppInfo() {
        AnalyticsApi api = ApiFactory.buildAnalyticsApi();

        AppInfoEventRequest request = new AppInfoEventRequest();
        String accessToken = "Bearer " + LauncherPreference.getToken(LauncherApplication.get());

        api.sendAppInfoEvent(BuildConfig.DMAC_APP_ID, accessToken, request).enqueue(new EmptyCallback<Void>());
    }

    public static void sendAppDownload(String targetAppVersion, String targetAppId) {
        AnalyticsApi api = ApiFactory.buildAnalyticsApi();

        AppDownloadEventRequest request = new AppDownloadEventRequest(targetAppVersion, targetAppId);
        String accessToken = "Bearer " + LauncherPreference.getToken(LauncherApplication.get());

        api.sendAppDownloadEvent(BuildConfig.DMAC_APP_ID, accessToken, request).enqueue(new EmptyCallback<Void>());
    }

    public static void sendDmacApiEvent(String function, String targetAppId) {
        AnalyticsApi api = ApiFactory.buildAnalyticsApi();

        DmacApiEventRequest request = new DmacApiEventRequest(function, targetAppId);
        String accessToken = "Bearer " + LauncherPreference.getToken(LauncherApplication.get());

        api.sendDmacApiEvent(BuildConfig.DMAC_APP_ID, accessToken, request).enqueue(new EmptyCallback<Void>());
    }
}