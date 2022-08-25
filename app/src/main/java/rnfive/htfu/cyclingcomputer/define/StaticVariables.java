package rnfive.htfu.cyclingcomputer.define;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.GeomagneticField;
import android.util.Log;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

public final class StaticVariables {

    public static final String DARK_SKY_KEY = "f71cb523e435a8574e97de147922db4d";

    public static boolean bStarted;
    public static boolean bPaused;
    public static boolean bMoving;

    public static boolean bHRExists;
    public static boolean bBPExists;
    public static boolean bBPCadExists;
    public static boolean bBCExists;
    public static boolean bBSExists;

    public static int bcAntBattery = -1;
    public static int bsAntBattery = -1;
    public static int bpAntBattery = -1;
    public static int hrAntBattery = -1;

    // Preferences
    public static boolean bAntSpeed;
    public static boolean bKeepAwake;
    public static boolean bPowerZoneColors;
    public static boolean bHrZoneColors;
    public static boolean bMetric;
    public static boolean bDebug;
    public static boolean bInvert;
    public static boolean bAntDistance;
    public static int iWheelSize;
    public static int iAthleteHrMax;
    public static int iAthleteFtp;
    public static double dGradeOffset;

    public static DarkSkyResponse darkSkyResponse;

    public static GeomagneticField geomagneticField;

    public static final float speedMin = 1.25f;

    public static long lastUpdateValuesMS;

    private StaticVariables() {}

    public static <T> T getClassFromJson(String inJson, Class<T> t) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(inJson, t);
    }

    public static double roundDouble(double in, int places) {
        double precision = StrictMath.pow(10.0d, places);
        double ret = Math.round(in*precision);
        return ret/precision;
    }

    public static <T> double lowPassFilter(T t1, T t2) {
        double v1 = 0.0d;
        double v2 = 0.0d;
        double a = 0.999d;

        if (t1 instanceof Float) {
            v1 = ((Float) t1).doubleValue();
            v2 = ((Float) t2).doubleValue();
        } else if (t1 instanceof Double) {
            v1 = (Double) t1;
            v2 = (Double) t2;
        } else if (t1 instanceof Integer) {
            v1 = ((Integer) t1).doubleValue();
            v2 = ((Integer) t2).doubleValue();
        }

        return (v1 + a * (v2-v1));
    }

    public static int getThemeColor(Context context, int val) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(val, typedValue, true);
        return typedValue.data;
    }

    public static <T> T getFromJson(String json, Class<T> tClass) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, tClass);
    }

    public static <T> String getToJson(T t) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(t);
    }

    public static void saveFile(File dir, String fileName, String contents) {
        File file = new File(dir,fileName);
        boolean process = file.exists();
        if (!file.exists()) {
            try {
                process = file.createNewFile();
            } catch (IOException e) {
                Log.e("saveFile()", "Failed to create new file " + file + ". Error: " + e.getMessage());
            }
        }
        if (process) {
            try (FileWriter fw = new FileWriter(file)) {
                write(fw, contents);
            } catch (IOException e) {
                Log.e("saveFile()", "Failed to write file " + file + ". Error: " + e.getMessage());
            }
        }
    }

    public static <T> T loadFile(File dir, String fileName, Class<T> tClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        File file = new File(dir,fileName);
        try (FileInputStream fis = new FileInputStream(file)) {
            String contents = read(fis);
            Log.d("loadFile()", contents);
            return getFromJson(contents, tClass);
        } catch (FileNotFoundException e) {
            Log.e("loadFile()", "File " + file + " not found.");
        } catch (IOException e) {
            Log.e("loadFile()", "Load " + file + " failed. Error: " + e.getMessage());
        }
        return tClass.getConstructor().newInstance();
    }

    private static void write(FileWriter fw, String contents) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(contents);
        }
    }

    private static String read(FileInputStream fis) throws IOException {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader bfr = new BufferedReader(new InputStreamReader(fis))) {
            String line = bfr.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = bfr.readLine();
            }
        }
        return sb.toString();
    }
}
