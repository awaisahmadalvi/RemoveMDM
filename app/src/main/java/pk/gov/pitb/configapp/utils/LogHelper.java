package pk.gov.pitb.configapp.utils;

import android.app.Activity;
import android.icu.text.SimpleDateFormat;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

public class LogHelper {

    private final Activity activity;
    private final TextView logView;

    public LogHelper(Activity activity,
                     TextView logView) {

        this.activity = activity;
        this.logView = logView;
    }

    public void log(String msg) {

        String line = timestamp() + " " + msg;

        activity.runOnUiThread(() ->
                logView.append(line + "\n"));

        FileUtils.appendLog(
                activity,
                "ethernet_log.txt",
                line
        );
    }

    private String timestamp() {

        return new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault())
                .format(new Date());
    }
}