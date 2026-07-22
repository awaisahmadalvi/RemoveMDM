package com.config.setipaddress_val_m;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class IndustryUpgradeClient {

    private static final String TAG = "IndustryUpgrade";
    private static final int CHUNK_SIZE = 81920;

    private final String baseUrl;
    private final String username;
    private final String password;

    public interface Callback {
        void onProgress(int percent);
        void onMessage(String msg);
        void onCompleted();
        void onError(Exception e);
    }

    public IndustryUpgradeClient(String baseUrl,
                                 String username,
                                 String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;

        CookieHandler.setDefault(new CookieManager());
    }

    private String referer() {
        return baseUrl +
                "/pages/maintenance/default.html?_=" +
                System.currentTimeMillis();
    }

    //=========================================================
    // LOGIN
    //=========================================================

    public boolean login() throws Exception {

        URL url = new URL(baseUrl + "/devapi/v1/basic/key");

        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Content-Type",
                "application/json");

        JSONObject json = new JSONObject();

        json.put("username", username);
        json.put("password", password);
        json.put("language", 1);
        json.put("autoLogin", 1);
        json.put("pwenc", 1);

        DataOutputStream out =
                new DataOutputStream(conn.getOutputStream());

        out.write(json.toString().getBytes());
        out.flush();
        out.close();

        int code = conn.getResponseCode();

        String body = read(conn);

        Log.d(TAG, "Login " + code + " " + body);

        return code == 200;
    }

    //=========================================================
    // UPLOAD
    //=========================================================

    public void uploadFile(File file,
                           String remoteName,
                           int packType,
                           Callback callback)
            throws Exception {

        long totalSize = file.length();

        int totalNum =
                (int) Math.ceil((double) totalSize / CHUNK_SIZE);

        BufferedInputStream bis =
                new BufferedInputStream(
                        new FileInputStream(file));

        byte[] buffer = new byte[CHUNK_SIZE];

        int read;
        int chunk = 1;

        while ((read = bis.read(buffer)) != -1) {

            URL url = new URL(baseUrl +
                    "/devapi/v1/basic/industryuploadfile");

            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Origin", baseUrl);
            conn.setRequestProperty("Referer", referer());

            conn.setRequestProperty("fileName", remoteName);
            conn.setRequestProperty("packType",
                    String.valueOf(packType));
            conn.setRequestProperty("Totalnum",
                    String.valueOf(totalNum));
            conn.setRequestProperty("Rmupnum",
                    String.valueOf(chunk));

            conn.setRequestProperty(
                    "Content-Type",
                    "application/octet-stream");

            DataOutputStream out =
                    new DataOutputStream(conn.getOutputStream());

            out.write(buffer, 0, read);
            out.flush();
            out.close();

            int code = conn.getResponseCode();

            String body = read(conn);

            if (callback != null) {

                callback.onMessage(
                        "Chunk "
                                + chunk
                                + "/"
                                + totalNum
                                + " -> "
                                + code
                                + " "
                                + body);

                callback.onProgress(
                        (chunk * 100) / totalNum);
            }

            if (code != 200)
                throw new IOException(body);

            chunk++;
        }

        bis.close();
    }

    //=========================================================
    // TRIGGER
    //=========================================================

    public String trigger(String filename,
                          int packType,
                          int periphMask,
                          int triggerMode,
                          String filePatch,
                          int subPeriphMask)
            throws Exception {

        String query =
                "packType=" + packType +
                        "&periphMask=" + periphMask +
                        "&triggerMode=" + triggerMode +
                        "&filename=" +
                        URLEncoder.encode(filename, "UTF-8") +
                        "&filepatch=" +
                        URLEncoder.encode(filePatch, "UTF-8") +
                        "&subPeriphMask=" + subPeriphMask +
                        "&_=" + System.currentTimeMillis();

        URL url = new URL(
                baseUrl +
                        "/devapi/v1/basic/industryupgradetrigger?"
                        + query);

        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        conn.setRequestProperty(
                "Referer",
                referer());

        conn.setRequestProperty(
                "X-Requested-With",
                "XMLHttpRequest");

        String body = read(conn);

        Log.d(TAG, body);

        return body;
    }

    //=========================================================
    // POLL
    //=========================================================

    public boolean pollResult(long intervalMs,
                              long timeoutMs,
                              Callback callback)
            throws Exception {

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start
                < timeoutMs) {

            try {

                URL url = new URL(
                        baseUrl +
                                "/devapi/v1/basic/industryupgraderestult?_="
                                + System.currentTimeMillis());

                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");

                conn.setRequestProperty(
                        "Referer",
                        referer());

                conn.setRequestProperty(
                        "X-Requested-With",
                        "XMLHttpRequest");

                JSONObject body =
                        new JSONObject(read(conn));

                JSONObject data =
                        body.optJSONObject("data");

                int result =
                        data.optInt("result");

                int reason =
                        data.optInt("reason");

                if (callback != null)
                    callback.onMessage(
                            "result="
                                    + result
                                    + " reason="
                                    + reason);

                if (result == 5) {

                    if (callback != null)
                        callback.onCompleted();

                    return true;
                }

            } catch (Exception e) {

                if (callback != null)
                    callback.onMessage(
                            "Device unreachable...");
            }

            Thread.sleep(intervalMs);
        }

        return false;
    }

    //=========================================================
    // UTIL
    //=========================================================

    private String read(HttpURLConnection conn)
            throws Exception {

        InputStream is;

        if (conn.getResponseCode() >= 400)
            is = conn.getErrorStream();
        else
            is = conn.getInputStream();

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(is));

        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null)
            sb.append(line);

        br.close();

        return sb.toString();
    }
}