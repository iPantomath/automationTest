package com.dhl.demp.mydmac.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.request.RefreshTokenRequest;
import com.dhl.demp.mydmac.api.response.RefreshTokenResponse;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.utils.RefreshAccessTokenCallback;
import com.dhl.demp.mydmac.utils.Utils;

import retrofit2.Call;

/**
 * Created by robielok on 10/12/2017.
 */
public class DMACJobService extends JobService {
    private static final int JOB_ID_REFRESH_TOKEN = 1;
    private static final int JOB_ID_SEND_DEVICE_INFO = 2;

    static void cancelAll(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
    }

    static void refreshToken(Context context) {
        //set up job info
        ComponentName componentName = new ComponentName(context.getPackageName(), DMACJobService.class.getName());
        JobInfo jobInfo = new JobInfo.Builder(DMACJobService.JOB_ID_REFRESH_TOKEN, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        //schedule the job
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    static void sendDeviceInfo(Context context, long intervalMillis /*in milliseconds*/) {
        ComponentName componentName = new ComponentName(context.getPackageName(), DMACJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(DMACJobService.JOB_ID_SEND_DEVICE_INFO, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(intervalMillis, (long)(intervalMillis * 0.1f));
        } else {
            builder.setMinimumLatency(intervalMillis);
        }
        JobInfo jobInfo = builder.build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        //cancel the previous job
        jobScheduler.cancel(jobInfo.getId());

        //schedule a new job
        jobScheduler.schedule(jobInfo);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        boolean result = false;

        switch (params.getJobId()) {
            case JOB_ID_REFRESH_TOKEN:
                doRefreshToken(params);
                result = true;
                break;
            case JOB_ID_SEND_DEVICE_INFO:
                sendDeviceInfo(params);
                result = true;
                break;
        }

        return result;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void doRefreshToken(final JobParameters params) {
        UnifiedApi api = ApiFactory.buildUnifiedApi();
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        Call<RefreshTokenResponse> refreshToken = api.refreshToken(
                new RefreshTokenRequest(
                    LauncherPreference.getRefToken(this),
                    ApiFactory.getScope(),
                    deviceInfo
                ),
                ApiFactory.getApiKey());
        refreshToken.enqueue(new RefreshAccessTokenCallback(this) {
            @Override
            protected void onRequestDone() {
                jobFinished(params, false);
            }

            @Override
            protected void onError(String error, boolean logoutUser) {
                jobFinished(params, false);
            }

            @Override
            protected void onFailure(String message) {
                jobFinished(params, false);
            }

            @Override
            protected void onTokenRefreshed() {
                //nothing to do
            }
        });
    }

    private void sendDeviceInfo(final JobParameters params) {
        UnifiedApi api = ApiFactory.buildUnifiedApi();
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        Call<RefreshTokenResponse> refreshToken = api.refreshToken(
                new RefreshTokenRequest(
                    LauncherPreference.getRefToken(this),
                    ApiFactory.getScope(),
                    deviceInfo),
                ApiFactory.getApiKey());
        refreshToken.enqueue(new RefreshAccessTokenCallback(this) {
            @Override
            protected void onRequestDone() {
                jobFinished(params, false);
            }

            @Override
            protected void onError(String error, boolean logoutUser) {
                jobFinished(params, true);
            }

            @Override
            protected void onFailure(String message) {
                jobFinished(params, true);
            }

            @Override
            protected void onTokenRefreshed() {
                //nothing to do
            }
        });
    }
}
