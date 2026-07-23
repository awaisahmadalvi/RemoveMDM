package pk.gov.pitb.configapp.network;


import java.io.File;

import pk.gov.pitb.configapp.streamax.IndustryUpgradeClient;

public class ChangeIPAddress {

    public static Runnable updatePISFirmware() {
        return new Runnable() {
            @Override
            public void run() {
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
//                                    log("Upgrade: " + percent + "%");
                                }

                                @Override
                                public void onMessage(String msg) {
//                                    log("Upgrade: " + msg);
                                }

                                @Override
                                public void onCompleted() {
//                                    log( "Upgrade Done");
                                }

                                @Override
                                public void onError(Exception e) {
//                                    log("Error");
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

                } catch (
                        Exception e) {
                    e.printStackTrace();
                }
            }

        };
    }

    public static void changeIPAddress() {
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
//                (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);
//
//        em.setConfiguration("eth0", config);
    }


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
