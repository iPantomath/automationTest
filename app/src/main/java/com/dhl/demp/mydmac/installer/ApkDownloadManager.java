package com.dhl.demp.mydmac.installer;

import android.net.Uri;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Map;

/**
 * Created by robielok on 3/9/2018.
 */

interface ApkDownloadManager {
    void onAttach(ApkDownloadManagerListener listener);
    void onDetach();
    long download(Uri uri, Map<String, String> headers, String appId, String notificationTitle, String notificationDescription);
    @Nullable String getMimeType(long downloadId);
    @Nullable File getFile(long downloadId, String appId);
}
