package pk.gov.pitb.configapp.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pk.gov.pitb.configapp.utils.LogHelper;
import pk.gov.pitb.configapp.utils.RootUtils;


public class DownloadReceiver extends BroadcastReceiver {

    private final LogHelper logger;

    public DownloadReceiver(LogHelper logger) {
        this.logger = logger;
    }

    @Override
    public void onReceive(Context context,
                          Intent intent) {

        long id = intent.getLongExtra(
                DownloadManager.EXTRA_DOWNLOAD_ID,
                -1
        );

        if (!DownloadHelper.isDownloadSuccessful(context, id))
            return;

        logger.log("Download complete");

        logger.log(RootUtils.removeDeviceOwner());

        logger.log(RootUtils.removeApp(
                "com.hmdm.launcher"));

        logger.log(
                RootUtils.installRunApk(
                        "/storage/emulated/0/Download/"
                                + DownloadHelper.APK_NAME,
                        "com.hmdm.launcher/.MainActivity"
                )
        );
    }
}