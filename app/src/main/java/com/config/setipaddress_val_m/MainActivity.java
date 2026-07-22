package com.config.setipaddress_val_m;

import static com.config.setipaddress_val_m.RootUtils.installApk;
import static com.config.setipaddress_val_m.RootUtils.removeApp;
import static com.config.setipaddress_val_m.RootUtils.removeDeviceOwner;
import static com.config.setipaddress_val_m.RootUtils.writeToExternalStorage;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hmdm.HeadwindMDM;
import com.hmdm.MDMService;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements HeadwindMDM.EventHandler {
    private HeadwindMDM headwindMDM;
    private TextView txtLog;
    private Button btnRunAgain;

    private static final String PREF = "config";
    private static final String KEY_DONE = "ethernet_configured";

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLog = findViewById(R.id.txtLog);
        btnRunAgain = findViewById(R.id.btnRunAgain);

        txtLog.setMovementMethod(new ScrollingMovementMethod());
        headwindMDM = HeadwindMDM.getInstance();

        // Automatic run on startup (honors the flag)
//        new Thread(() -> runConfiguration(false)).start();
//
        // Manual button (ignores the flag)
        btnRunAgain.setOnClickListener(v ->
                new Thread(() -> runConfiguration(true)).start()
        );

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {

            try {

                IndustryUpgradeClient client =
                        new IndustryUpgradeClient(
                                "http://10.100.100.30",
                                "admin",
                                "05ebe863b7fd27351df60cdab8532e72");

                client.login();

                client.uploadFile(
                        new File("/sdcard/BUS_PIS_C0010_V1.0.0.1_R25080201"),
                        "BUS_PIS_C0010_V1.0.0.1_R25080201",
                        1,
                        new IndustryUpgradeClient.Callback() {

                            @Override
                            public void onProgress(int percent) {
                                log("Upgrade: "+percent + "%");
                            }

                            @Override
                            public void onMessage(String msg) {
                                log("Upgrade: " + msg);
                            }

                            @Override
                            public void onCompleted() {
                                log("Upgrade Done");
                            }

                            @Override
                            public void onError(Exception e) {
//                            log("Error: " + e.printStackTrace().toString());
                                log("Error");

                            }
                        });

                client.trigger(
                        "BUS_PIS_C0010_V1.0.0.1_R25080201",
                        5,
                        3,
                        3,
                        "/mnt/sdcard/Bus",
                        0);

                Thread.sleep(5000);

                client.pollResult(
                        3000,
                        900000,
                        null);

            } catch (Exception e) {
                e.printStackTrace();
            }

        });


//        StaticIpConfiguration staticIp = new StaticIpConfiguration();
//
//        staticIp.setIpAddress(
//                new LinkAddress(
//                        InetAddress.getByName("10.100.100.50"),
//                        24));
//
//        staticIp.setGateway(
//                InetAddress.getByName("10.100.100.1"));
//
//        staticIp.setDnsServers(Arrays.asList(
//                InetAddress.getByName("8.8.8.8")));
//
//        IpConfiguration config =
//                new IpConfiguration(
//                        IpConfiguration.IpAssignment.STATIC,
//                        IpConfiguration.ProxySettings.NONE,
//                        staticIp,
//                        null);
//
//        EthernetManager em =
//                (EthernetManager)getSystemService(Context.ETHERNET_SERVICE);
//
//        em.setConfiguration("eth0", config);
    }

    private void initMDMReplace() {
        long downloadId = DownloadHelper.downloadApk(this);
    }

    private void runConfiguration(boolean force) {
        registerReceiver(
                downloadReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        );

        initMDMReplace();

//        SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
//
//        if (!force && pref.getBoolean(KEY_DONE, false)) {
//            log("Ethernet already configured. Skipping.");
//            return;
//        }
//
//        if (force) {
//            log("Manual configuration requested.");
//        } else {
//            log("Automatic configuration started.");
//        }
//
//        String result = RootUtils.configureEthernet(
//                "eth0",
//                "10.100.100.50",
//                24,
//                "10.100.100.1",
//                "8.8.8.8"
//        );
//
//        log(result);
//
//        if (!result.contains("Error")) {
//            pref.edit().putBoolean(KEY_DONE, true).apply();
//            log("Configuration completed successfully.");
//        } else {
//            log("Configuration failed.");
//        }
    }

    private void log(String msg) {

        String line = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(new Date()) + "  " + msg;

        runOnUiThread(() -> {
            txtLog.append(line + "\n");
        });

        writeLogsToFile(line);
    }

    private void writeLogsToFile(String text) {

        try {

            File dir = getExternalFilesDir(null);
            if (dir == null) return;

            File file = new File(dir, "ethernet_log.txt");

            FileWriter writer = new FileWriter(file, true);
            writer.append(text).append("\n");
            writer.close();

        } catch (Exception ignored) {
        }
    }

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            long downloadId = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID,
                    -1
            );

            log("APK Download Completed");
//
//            Toast.makeText(
//                    context,
//                    Toast.LENGTH_LONG
//            ).show();

            // APK location:
            // /storage/emulated/0/Download/MDM_6.26.11.apk

            if (isDownloadSuccessful(context,downloadId))
            {
                String resp = removeDeviceOwner();
                log("DeviceOwner Removed: "+ resp);
                resp = removeApp("com.hmdm.launcher");
                log("MDM removed: "+resp);

                installApk("/storage/emulated/0/Download/MDM_6.26.11.apk", "com.hmdm.launcher/.MainActivity");
                log("APK Installed");
            }
        }
    };

    public static boolean isDownloadSuccessful(Context context, long downloadId) {

        DownloadManager dm =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = dm.query(query);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                );
                cursor.close();
                return status == DownloadManager.STATUS_SUCCESSFUL;
            }
            cursor.close();
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!headwindMDM.isConnected()) {
            if (!headwindMDM.connect(this, this)) {
                // Your application is running outside Headwind MDM
            }
        } else {
            // Already connected, but settings may have changed
            // when our app was in the background, so reload them
            loadSettings();
            String deviceID = headwindMDM.getDeviceId();
            writeToExternalStorage(deviceID,"/storage/emulated/0/deviceID.txt");
        }
    }

    @Override
    protected void onDestroy() {
        headwindMDM.disconnect(this);
        unregisterReceiver(downloadReceiver);
        super.onDestroy();
    }

    @Override
    public void onHeadwindMDMConnected() {
        // Connected to Headwind MDM, now you can load settings and use other MDM functions
        loadSettings();
    }

    @Override
    public void onHeadwindMDMDisconnected() {
    }

    @Override
    public void onHeadwindMDMConfigChanged() {
        // Settings were changed on the server, you need to reload them
        loadSettings();
    }

    // Get the application settings from the server
    private void loadSettings() {
//        String someSetting = MDMService.Preferences.get("some_setting", getString(R.string.default_setting));
    }
}