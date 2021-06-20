package rnfive.djs.cyclingcomputer.define;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import lombok.Data;
import rnfive.djs.cyclingcomputer.MainActivity;

@Data
public class Preferences {

    private final static String TAG = "Preferences";
    private final static String FILE_NAME = "preference.json";
    private Integer iHrMax = 220;
    private Integer iFtp = 200;
    private boolean bHrZoneColor = false;
    private boolean bPowerZoneColor = false;
    private boolean bDebugMode = false;
    private boolean bInvertColorMode = false;
    private boolean bMetricMode = false;
    private boolean bKeepAwakeMode = false;
    private boolean bUseSensorSpeed = false;
    private boolean bUseSensorDistance = false;
    private Integer iWheelCircumfrence = 2095;
    private int[] bikeDataFields = new int[39];

    public Preferences() {
        Arrays.fill(bikeDataFields, 2000);
    }

    public void save() {
        Log.d(TAG,"save()");
        try {
            Json.saveJSONToFile(MainActivity.filePathProfile,FILE_NAME, toJSON());
        } catch (Exception e) {
            Log.e("Preferences.save()","Error saving. " + e.getMessage());
        }
    }

    private JSONObject toJSON() {
        Log.d(TAG,"toJSON()");
        JSONObject prefsObject = new JSONObject();
        try {
            prefsObject.put("hr_max", iHrMax);
            prefsObject.put("ftp", iFtp);
            prefsObject.put("hr_zone_color", bHrZoneColor);
            prefsObject.put("power_zone_color", bPowerZoneColor);
            prefsObject.put("debug_mode", bDebugMode);
            prefsObject.put("invert_color_mode", bInvertColorMode);
            prefsObject.put("metric_mode", bMetricMode);
            prefsObject.put("keep_awake_mode", bKeepAwakeMode);
            prefsObject.put("use_sensor_speed", bUseSensorSpeed);
            prefsObject.put("use_sensor_distance", bUseSensorDistance);
            prefsObject.put("wheel_circumfrence", iWheelCircumfrence);
            JSONArray dataFields = new JSONArray();
            for (int i=0;i<bikeDataFields.length;i++) {
                dataFields.put(i,bikeDataFields[i]);
            }
            prefsObject.put("bike_data_fields", dataFields);
            return prefsObject;
        } catch (JSONException e) {
            Log.e("Preferneces.toJSON()","Error saving Preferences. " + e.getMessage());
            return null;
        }
    }

    private void fromJSON() {
        Log.d(TAG,"fromJSON()");
        JSONObject jsonObject;
        try {
            jsonObject = Json.loadJSONFromFile(MainActivity.filePathProfile,FILE_NAME);
        } catch (Exception e) {
            Log.e("Preferneces.fromJSON()","Loading file from json failed. " + e.getMessage());
            jsonObject = null;
        }
        if (jsonObject != null) {
            iHrMax = Json.getJSONInt(jsonObject,"hr_max", 220);
            iFtp = Json.getJSONInt(jsonObject,"ftp", 200);
            bHrZoneColor = Json.getJSONBoolean(jsonObject,"hr_zone_color", null);
            bPowerZoneColor = Json.getJSONBoolean(jsonObject,"power_zone_color", null);
            bDebugMode = Json.getJSONBoolean(jsonObject,"debug_mode", null);
            bInvertColorMode = Json.getJSONBoolean(jsonObject,"invert_color_mode", null);
            bMetricMode = Json.getJSONBoolean(jsonObject,"metric_mode", null);
            bKeepAwakeMode = Json.getJSONBoolean(jsonObject,"keep_awake_mode", null);
            bUseSensorSpeed = Json.getJSONBoolean(jsonObject,"use_sensor_speed", null);
            bUseSensorDistance = Json.getJSONBoolean(jsonObject,"use_sensor_distance", null);
            iWheelCircumfrence = Json.getJSONInt(jsonObject,"wheel_circumfrence", 2095);
            try {
                JSONArray array = jsonObject.getJSONArray("bike_data_fields");
                for (int i=0;i<bikeDataFields.length;i++) {
                    bikeDataFields[i] = array.getInt(i);
                }
            } catch (JSONException e) {
                Log.e("Preferences.fromJSON()","bike data fields empty. " + e.getMessage());
            }
        }
    }

    public void load() {
        Log.d(TAG,"load()");
        fromJSON();
    }
}
