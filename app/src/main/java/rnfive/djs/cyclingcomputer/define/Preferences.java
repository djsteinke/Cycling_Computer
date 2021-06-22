package rnfive.djs.cyclingcomputer.define;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import rnfive.djs.cyclingcomputer.MainActivity;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.getToJson;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.saveFile;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.loadFile;

@Getter
@Setter
public class Preferences {

    private static final String TAG = Preferences.class.getSimpleName();
    private static final String FILE_NAME = "preference.json";
    private int iHrMax = 220;
    private int iFtp = 200;
    private boolean bHrZoneColor;
    private boolean bPowerZoneColor;
    private boolean bDebugMode;
    private boolean bInvertColorMode;
    private boolean bMetricMode;
    private boolean bKeepAwakeMode;
    private boolean bUseSensorSpeed;
    private boolean bUseSensorDistance;
    private int iWheelCircumfrence = 2095;
    private int[] bikeDataFields = new int[39];

    public Preferences() {
        Arrays.fill(bikeDataFields, 2000);
    }

    public void save() {
        Log.d(TAG,"save()");
        saveFile(MainActivity.filePathProfile,FILE_NAME, getToJson(this));
    }

    public static Preferences load() {
        Log.d(TAG,"load()");
        try {
            return loadFile(MainActivity.filePathProfile, FILE_NAME, Preferences.class);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            Log.e(TAG, "Failed to load Preferences from file. Error: " + e.getMessage());
            return new Preferences();
        }
    }
}
