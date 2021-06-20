package rnfive.djs.cyclingcomputer.define;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.GeomagneticField;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StaticVariables {

    public static final String DARK_SKY_KEY = "f71cb523e435a8574e97de147922db4d";

    public static boolean bStarted = false;
    public static boolean bPaused = false;
    public static boolean bMoving = false;

    public static boolean bHRExists = false;
    public static boolean bBPExists = false;
    public static boolean bBPCadExists = false;
    public static boolean bBCExists = false;
    public static boolean bBSExists = false;

    public static int bcAntBattery = -1;
    public static int bsAntBattery = -1;
    public static int bpAntBattery = -1;
    public static int hrAntBattery = -1;

    // Preferences
    public static boolean bAntSpeed = false;
    public static boolean bKeepAwake = false;
    public static boolean bPowerZoneColors = false;
    public static boolean bHrZoneColors = false;
    public static boolean bMetric = false;
    public static boolean bDebug = false;
    public static boolean bInvert = false;
    public static boolean bAntDistance = false;
    public static int iWheelSize = 0;
    public static int iAthleteHrMax = 0;
    public static int iAthleteFtp = 0;

    public static DarkSkyResponse darkSkyResponse;

    public static GeomagneticField geomagneticField;

    public static final float speedMin = 0.5f;

    public static long lastUpdateValuesMS = 0;

    public StaticVariables() {}

    public static <T> T getClassFromJson(String inJson, Class<T> t) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(inJson, t);
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
}
