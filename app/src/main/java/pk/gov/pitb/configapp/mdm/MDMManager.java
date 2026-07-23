package pk.gov.pitb.configapp.mdm;

import android.content.Context;

import com.hmdm.HeadwindMDM;

import pk.gov.pitb.configapp.utils.LogHelper;
import pk.gov.pitb.configapp.utils.RootUtils;

public class MDMManager implements HeadwindMDM.EventHandler {

    private final Context context;
    private final LogHelper logger;
    private final HeadwindMDM mdm;

    public MDMManager(Context context, LogHelper logger) {
        this.context = context;
        this.logger = logger;
        this.mdm = HeadwindMDM.getInstance();
    }

    public void backupDeviceId() {

        if (!mdm.isConnected()) {
            mdm.connect(context, this);
            return;
        }

        String id = mdm.getDeviceId();

        RootUtils.writeToExternalStorage(
                "/storage/emulated/0/deviceID.txt",
                id
        );

        logger.log("Device ID = " + id);
    }

    public void disconnect() {
        mdm.disconnect(context);
    }

    @Override
    public void onHeadwindMDMConnected() {
        backupDeviceId();
    }

    @Override
    public void onHeadwindMDMDisconnected() {
    }

    @Override
    public void onHeadwindMDMConfigChanged() {
    }
}