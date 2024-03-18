package com.dhl.demp.mydmac.api.tls;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;
import com.dhl.demp.mydmac.utils.OkHttpUrlLoader;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by robielok on 7/20/2018.
 */

public class TLSOkHttpGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {}

    @Override
    public void registerComponents(Context context, Glide glide) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        OkHttpClient client = builder.build();

        glide.register(GlideUrl.class, InputStream.class,new OkHttpUrlLoader.Factory(client));
    }
}
