package com.dhl.demp.mydmac.installer;

import android.app.Notification;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.collection.LongSparseArray;

import com.dhl.demp.mydmac.api.tls.TLSSocketFactory;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.FileUtils;
import com.dhl.demp.mydmac.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import mydmac.R;

/**
 * Created by robielok on 3/9/2018.
 */

class ApkDownloadManagerImpl implements ApkDownloadManager {
    private long idGenerator = System.currentTimeMillis();

    ApkDownloadManagerImpl(Context context) {
        this.context = context;
    }

    private synchronized long nextId() {
        return ++idGenerator;
    }

    private Context context;
    private ExecutorService executorService;
    private LongSparseArray<ResultContainer> results;
    private ApkDownloadManagerListener listener;
    private Handler handler;

    @Override
    public void onAttach(ApkDownloadManagerListener listener) {
        this.listener = listener;

        executorService = Executors.newSingleThreadExecutor();
        results = new LongSparseArray<>();
        handler = new Handler();
    }

    @Override
    public void onDetach() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    @Override
    public long download(Uri uri, Map<String, String> headers, String appId, String notificationTitle, String notificationDescription) {
        //generate unique id of the download
        long downloadId = nextId();

        //show indeterminate progress notification
        showProgressNotification((int)downloadId, notificationTitle, notificationDescription, 0, 0, true);

        //start download
        executorService.submit(new DownloadTask(downloadId, appId, uri, headers, notificationTitle, notificationDescription));

        return downloadId;
    }

    @Nullable
    @Override
    public String getMimeType(long downloadId) {
        ResultContainer result = results.get(downloadId);
        return result != null ? result.mimeType : null;
    }

    @Nullable
    @Override
    public File getFile(long downloadId, String appId) {
        ResultContainer result = results.get(downloadId);
        return result != null ? result.file : null;
    }

    private void showProgressNotification(int id, String title, String text,
                                          int progressMax, int progress, boolean progressIndeterminate) {
        Utils.createMinorNotificationChannel(context);
        NotificationCompat.Builder builder  = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_MINOR);
        builder.setAutoCancel(false)
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_notification_wrapper)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentTitle(title)
                .setContentText(text)
        .setProgress(progressMax, progress, progressIndeterminate);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id ,builder.build());
    }

    private void showResultNotification(int id, String title, boolean success) {
        Utils.createImportantNotificationChannel(context);
        NotificationCompat.Builder builder    = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_IMPORTANT);
        builder.setAutoCancel(false)
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_notification_wrapper)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentTitle(title)
                .setContentText(context.getString(success ? R.string.apk_download_success : R.string.apk_download_fail));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id ,builder.build());
    }

    private void postProgressNotification(final int id, final String title, final String text,
                                      final int progressMax, final int progress, final boolean progressIndeterminate) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                showProgressNotification(id, title, text, progressMax, progress, progressIndeterminate);
            }
        });
    }

    private void postDownloadComplete(final long downloadId, final String notificationTitle, final boolean success) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //update notification
                showResultNotification((int)downloadId, notificationTitle, success);

                //notify listener
                if (listener != null) {
                    listener.onDownloadComplete(downloadId);
                }
            }
        });
    }

    private class DownloadTask implements Runnable, FileUtils.FileCopyProgressListener {
        private static final int PROGRESS_STEP = 5;
        private final long downloadId;
        private final String appId;
        private final Uri uri;
        private final Map<String, String> headers;
        private final String notificationTitle;
        private final String notificationDescription;
        private int postedProgress = -1;

        private DownloadTask(long downloadId, String appId, Uri uri, Map<String, String> headers,
                             String notificationTitle, String notificationDescription) {
            this.downloadId = downloadId;
            this.appId = appId;
            this.uri = uri;
            this.headers = headers;
            this.notificationTitle = notificationTitle;
            this.notificationDescription = notificationDescription;
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                //setup request
                URL url = new URL(uri.toString());
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setSSLSocketFactory(new TLSSocketFactory());
                for (String header : headers.keySet()) {
                    connection.addRequestProperty(header, headers.get(header));
                }
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    File file = new File(context.getExternalCacheDir(), appId + ".apk");
                    int estimatedSize = connection.getContentLength();

                    //store apk to the file
                    InputStream is = connection.getInputStream();
                    success = FileUtils.copyFile(is, file, estimatedSize, this);
                    try {
                        is.close();
                    } catch (IOException ex) {}


                    if (success) {
                        results.put(downloadId, new ResultContainer(connection.getContentType(), file));
                    } else {
                        file.delete();
                    }
                }
            } catch (Exception ex) {
            } finally {
                postDownloadComplete(downloadId, notificationTitle, success);
            }
        }

        @Override
        public void propagateProgress(int total, int done) {
            if (postedProgress == -1 || (done * 100 / total) - postedProgress >= PROGRESS_STEP) {
                postProgressNotification((int) downloadId, notificationTitle, notificationDescription, total, done, false);
                postedProgress = done * 100 / total;
            }
        }
    }

    private static class ResultContainer {
        private final String mimeType;
        private final File file;

        public ResultContainer(String mimeType, File file) {
            this.mimeType = mimeType;
            this.file = file;
        }
    }
}
