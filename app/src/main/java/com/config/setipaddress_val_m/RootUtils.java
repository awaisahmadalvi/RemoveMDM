package com.config.setipaddress_val_m;

import static android.provider.Settings.System.getString;

import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class RootUtils {

    private static String executeRootCommand(String... commands) {
        StringBuilder output = new StringBuilder();

        try {
            Process process = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            for (String cmd : commands) {
                os.writeBytes(cmd + "\n");
            }

            os.writeBytes("exit\n");
            os.flush();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();

            reader.close();
            errorReader.close();
            os.close();
            process.destroy();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

        return output.toString().trim();
    }

    public static String getMacAddress() {
        return executeRootCommand("cat /sys/class/net/eth0/address");
    }

    public static String removeDeviceOwner() {
        /* OPEN: from MDM */
        return executeRootCommand("am kill com.hmdm.launcher","dpm remove-active-admin --user 0 com.hmdm.launcher/.AdminReceiver");
    }

    public static String writeToExternalStorage(String filename, String content) {
        String command = "echo " + content +" > " + filename;
        return executeRootCommand(command);
    }

    public static String installApk(String filename, String activity) {
//        pm install /storage/emulated/0/Download/MDM_6.26.11.apk && am start -n com.hmdm.launcher/.MainActivity
        String command = "pm install -r " + filename + " && am start -n " + activity;

        return executeRootCommand(command);
    }

    public static String removeApp(String appName){
//        pm install /storage/emulated/0/Download/MDM_6.26.11.apk && am start -n com.hmdm.launcher/.MainActivity
        String command = "pm uninstall " + appName;

        return executeRootCommand(command);
    }


    public static String startApplication(String activity) {
        String command = "am start -n " + activity;
        return executeRootCommand(command);
    }

    /**
     * Configure Ethernet with static IP.
     *
     * @param iface Interface name (eth0, eth1...)
     * @param ip Static IP
     * @param prefix Prefix length (24)
     * @param gateway Default gateway
     * @param dns DNS server
     */
    public static String configureEthernet(
            String iface,
            String ip,
            int prefix,
            String gateway,
            String dns
    ) {

        return executeRootCommand(
                "ip link set " + iface + " up",
                "ip addr flush dev " + iface,
                "ip addr add " + ip + "/" + prefix + " dev " + iface,
                "ip route del default 2>/dev/null",
                "ip route add default via " + gateway + " dev " + iface,
                "setprop net.dns1 " + dns,
                "setprop net." + iface + ".dns1 " + dns,
                "ip addr show " + iface,
                "ip route",
                "getprop | grep dns"
        );
    }
}