package com.config.setipaddress_val_m;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

public class DownloadHelper {

    private static final String APK_URL =
            "https://mdm1.punjab.gov.pk/files/MDM_6.26.14.apk";

    public static final String APK_NAME =
            "MDM_6.26.14.apk";

    public static long downloadApk(Context context) {

        DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(APK_URL));

        request.setTitle("Downloading Headwind MDM");
        request.setDescription("Downloading latest MDM...");
        request.setMimeType("application/vnd.android.package-archive");

        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);

        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        );

        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                APK_NAME
        );

        DownloadManager dm =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        return dm.enqueue(request);
    }
}