package pk.gov.pitb.configapp.utils;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;

public class FileUtils {

    public static void appendLog(Context c,
                                 String file,
                                 String text) {

        try {

            File f = new File(
                    c.getExternalFilesDir(null),
                    file
            );

            FileWriter writer =
                    new FileWriter(f, true);

            writer.append(text).append("\n");

            writer.close();

        } catch (Exception ignored) {
        }
    }
}