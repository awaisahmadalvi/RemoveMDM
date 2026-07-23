package pk.gov.pitb.configapp.download;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

public class DownloadHelper {

    public static final String APK_NAME = "MDM_6.26.11.apk";

    // Replace with your APK URL
    public static final String APK_URL =
            "https://mdm1.punjab.gov.pk/files/" + APK_NAME;

    /**
     * Starts downloading the APK.
     *
     * @return DownloadManager download ID
     */
    public static long downloadApk(Context context) {

        DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(APK_URL));

        request.setTitle("Downloading Headwind MDM");
        request.setDescription("Downloading latest MDM APK");
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        );

        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);

        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                APK_NAME
        );

        return downloadManager.enqueue(request);
    }

    /**
     * Checks whether a download completed successfully.
     */
    public static boolean isDownloadSuccessful(Context context, long downloadId) {

        DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = downloadManager.query(query);

        if (cursor == null) {
            return false;
        }

        try {
            if (!cursor.moveToFirst()) {
                return false;
            }

            int status = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_STATUS
                    )
            );

            return status == DownloadManager.STATUS_SUCCESSFUL;

        } finally {
            cursor.close();
        }
    }

    /**
     * Returns the downloaded APK file path.
     */
    public static String getDownloadedApkPath() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
        ).getAbsolutePath() + "/" + APK_NAME;
    }
}