package rnfive.djs.cyclingcomputer.define;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class Json {
    private Json() {}

    public static JSONObject loadJSONFromFile(File dir, String fileName) throws Exception {
        File file = new File(dir,fileName);
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader bfr = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bfr.readLine()) != null) {
                sb.append(line).append("\n");
            }
            bfr.close();
            fis.close();
            Log.d("loadJSONFromFile","File[" + fileName + "] loaded.");
            return new JSONObject(sb.toString());
        } else {
            Log.d("loadJSONFromFile","File[" + fileName + "] does not exist.");
            return null;
        }
    }

    public static Integer getJSONInt(JSONObject object, String key, @Nullable Integer def) {
        try {
            return object.getInt(key);
        } catch (JSONException e) {
            return def;
        }
    }

    public static boolean getJSONBoolean(JSONObject object, String key, @Nullable Boolean def) {
        try {
            return object.getBoolean(key);
        } catch (JSONException e) {
            return (def==null?false:def);
        }
    }

    public static String getJSONString(JSONObject object, String key, @Nullable String def) {
        try {
            return object.getString(key);
        } catch (JSONException e) {
            return def;
        }
    }

    public static Long getJSONLong(JSONObject object, String key, @Nullable Long def) {
        try {
            return object.getLong(key);
        } catch (JSONException e) {
            return def;
        }
    }

    public static List<String> getJSONStringArray(JSONObject object, String key) {
        try {
            List<String> retList = new ArrayList<>();
            JSONArray array = object.getJSONArray(key);
            for (int i=0;i<array.length();i++) {
                retList.add(array.getString(i));
            }
            return retList;
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    public static List<Integer> getJSONIntArray(JSONObject object, String key) {
        try {
            List<Integer> retList = new ArrayList<>();
            JSONArray array = object.getJSONArray(key);
            for (int i=0;i<array.length();i++) {
                retList.add(array.getInt(i));
            }
            return retList;
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    public static void saveJSONToFile(File dir, String fileName, JSONObject jsonObject) throws IOException {
        File file = new File(dir,fileName);
        if (file.exists() || (!file.exists() && file.createNewFile())) {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(jsonObject.toString());
            bw.close();
            fw.close();
        }
        Log.d("saveJSONToFile","File[" + fileName + "] saved.");
    }
}
