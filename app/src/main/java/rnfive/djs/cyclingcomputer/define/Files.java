package rnfive.djs.cyclingcomputer.define;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.Nullable;
import rnfive.djs.cyclingcomputer.MainActivity;

public class Files {
    private Files() {}

    public static void logMesg(String severity, String source, @Nullable String msg) {
        if (StaticVariables.bDebug) {
            File file = new File(MainActivity.filePathLog, "log.txt");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            String ts = sdf.format(Calendar.getInstance().getTime());

            if (!file.exists()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                String message = ts + " " + severity + "/" + source + (msg != null ? ": " + msg : "") + "\n";
                FileWriter writer = new FileWriter(file, true);
                writer.append(message);
                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeFile(File dir, String fileName) {
        File file = new File(dir,fileName);
        if (file.exists()) {
            if (file.delete())
                Log.d("removeFile","File[" + fileName + "] removed.");
        }
    }
}
