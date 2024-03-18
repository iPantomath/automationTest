package com.dhl.demp.mydmac.api;

import android.content.Context;

import com.dhl.demp.dmac.data.network.TipInterceptor;
import com.dhl.demp.mydmac.LauncherApplication;
import com.dhl.demp.mydmac.LauncherPreference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;
import mydmac.BuildConfig;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.dhl.demp.mydmac.utils.Constants.BuildType;

/**
 *
 * Created by rd on 05/10/16.
 */

public class ApiFactory {
    public static final int URL_TYPE_TEST = 0;
    public static final int URL_TYPE_DEV = 1;
    public static final int URL_TYPE_CUSTOM = 2;
    public static final int URL_TYPE_UAT = 3;
    public static final int URL_TYPE_UAT_PRAGUE = 4;
    public static final int URL_TYPE_UAT_CYBERJAYA = 5;
    public static final int URL_TYPE_UAT_ASHBURN = 6;
    public static final int URL_TYPE_PROD_TIP = 7;

    public static final String BASE_URL_TEST = "https://magtw1-test.dhl.com";
    public static final String BASE_URL_DEV = "https://magtw1-dev.dhl.com";
    public static final String BASE_URL_PROD = "https://magtw1.dhl.com";
    public static final String BASE_URL_UAT = "https://magtw1-uat.dhl.com";
    public static final String BASE_URL_UAT_PRAGUE = "https://magtw-uat-prg.dhl.com";
    public static final String BASE_URL_UAT_CYBERJAYA = "https://magtw-uat-apis.dhl.com";
    public static final String BASE_URL_UAT_ASHBURN = "https://magtw-uat-qas.dhl.com";
    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String SCOPE  = "com.dhl.mam.client";

    public static final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
            .create();

    public static Api buildApi() {
        OkHttpClient client = buildClient(new Authenticator(), false);

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(Api.class);
    }

    public static UnifiedApi buildUnifiedApi() {
        OkHttpClient client = buildClient(null, false);

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(UnifiedApi.class);
    }

    public static AnalyticsApi buildAnalyticsApi() {
        OkHttpClient client = buildClient(new Authenticator(), true);

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(BuildConfig.ANALYTICS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(AnalyticsApi.class);
    }

    public static TipInterceptor getTipInterceptor() {
        return LauncherApplication.get().tipInterceptor.get();
    }

    private static OkHttpClient buildClient(Authenticator authenticator, boolean retryOnConnectionFailure) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(getTipInterceptor());
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addNetworkInterceptor(interceptor);
        }


        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(retryOnConnectionFailure);
        if (authenticator != null) {
            builder.authenticator(authenticator);
        }

        return  builder.build();
    }

    public static String baseUrl() {
        if (BuildConfig.BUILD_TYPE.equals(BuildType.RELEASE)) {
            return BASE_URL_PROD;
        } else if (BuildConfig.BUILD_TYPE.equals(BuildType.UAT)){
            return BASE_URL_UAT;
        } else {
            Context context = LauncherApplication.get();
            int type = LauncherPreference.getUrlType(context);
            switch (type) {
                case URL_TYPE_TEST:
                    return BASE_URL_TEST;
                case URL_TYPE_DEV:
                    return BASE_URL_DEV;
                case URL_TYPE_CUSTOM:
                    return LauncherPreference.getCustomUrl(context);
                case URL_TYPE_UAT:
                    return BASE_URL_UAT;
                case URL_TYPE_UAT_PRAGUE:
                    return BASE_URL_UAT_PRAGUE;
                case URL_TYPE_UAT_CYBERJAYA:
                    return BASE_URL_UAT_CYBERJAYA;
                case URL_TYPE_UAT_ASHBURN:
                    return BASE_URL_UAT_ASHBURN;
                default:
                    throw new IllegalStateException("Unknown type " + type);
            }
        }
    }

    public static String getApiKey() {
        if (BuildConfig.BUILD_TYPE.equals(BuildType.RELEASE) || BuildConfig.BUILD_TYPE.equals(BuildType.UAT)){
            return API_KEY;
        } else {
            Context context = LauncherApplication.get();
            int type = LauncherPreference.getUrlType(context);
            switch (type) {
                case URL_TYPE_TEST:
                case URL_TYPE_DEV:
                case URL_TYPE_UAT:
                case URL_TYPE_UAT_PRAGUE:
                case URL_TYPE_UAT_CYBERJAYA:
                case URL_TYPE_UAT_ASHBURN:
                    return API_KEY;
                case URL_TYPE_CUSTOM:
                    return LauncherPreference.getCustomApiKey(context);
                default:
                    throw new IllegalStateException("Unknown type " + type);
            }
        }
    }

    public static String getScope() {
        if (BuildConfig.BUILD_TYPE.equals(BuildType.RELEASE) || BuildConfig.BUILD_TYPE.equals(BuildType.UAT)){
            return SCOPE;
        } else {
            Context context = LauncherApplication.get();
            int type = LauncherPreference.getUrlType(context);
            switch (type) {
                case URL_TYPE_TEST:
                case URL_TYPE_DEV:
                case URL_TYPE_UAT:
                case URL_TYPE_UAT_PRAGUE:
                case URL_TYPE_UAT_CYBERJAYA:
                case URL_TYPE_UAT_ASHBURN:
                    return SCOPE;
                case URL_TYPE_CUSTOM:
                    return LauncherPreference.getCustomScope(context);
                default:
                    throw new IllegalStateException("Unknown type " + type);
            }
        }
    }
}
