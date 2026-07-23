package pk.gov.pitb.configapp.mdm;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import pk.gov.pitb.configapp.download.DownloadHelper;
import pk.gov.pitb.configapp.download.DownloadReceiver;
import pk.gov.pitb.configapp.utils.LogHelper;


public class MDMReplaceManager {

    private final Context context;
    private final LogHelper logger;

    private BroadcastReceiver receiver;

    public MDMReplaceManager(Context context,
                             LogHelper logger) {

        this.context = context;
        this.logger = logger;
    }

    public void start() {

        receiver = new DownloadReceiver(logger);

        context.registerReceiver(
                receiver,
                new IntentFilter(
                        DownloadManager.ACTION_DOWNLOAD_COMPLETE
                ),
                Context.RECEIVER_NOT_EXPORTED
        );

        DownloadHelper.downloadApk(context);
    }

    public void unregister() {

        if (receiver != null)
            context.unregisterReceiver(receiver);
    }
}