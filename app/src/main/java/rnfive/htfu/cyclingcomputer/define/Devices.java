package rnfive.htfu.cyclingcomputer.define;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import rnfive.htfu.cyclingcomputer.MainActivity;

@Data
public class Devices {
    private static final String FILE_NAME = "devices.json";
    private List<Device> devices = new ArrayList<>();

    public Devices() {}

    public void save() {
        try {
            Json.saveJSONToFile(MainActivity.filePathProfile,FILE_NAME, toJSON());
        } catch (Exception e) {
            Log.e("Devices.save()","Error saving Devices. " + e.getMessage());
        }
    }

    public Devices addDevice(Device device) {
        if (!devices.contains(device)) {
            devices.add(device);
            save();
        }
        return this;
    }

    public Devices removeDevice(Device device) {
        devices.remove(device);
        save();
        return this;
    }

    public void load() {
        try {
            fromJSON();
        } catch (Exception e) {
            Log.e("Devices.load()","Error loading Devices. " + e.getMessage());
        }
    }

    private JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        if (devices != null && !devices.isEmpty()) {
            for (int i=0;i<devices.size();i++) {
                array.put(devices.get(i).toJSON());
            }
            object.put("devices",array);
        }
        return object;
    }

    private void fromJSON() throws Exception {
        JSONObject jsonObject = Json.loadJSONFromFile(MainActivity.filePathProfile,FILE_NAME);
        JSONArray array = jsonObject.getJSONArray("devices");
        for (int i=0;i<array.length();i++) {
            devices.add(new Device().fromJSON((JSONObject) array.get(i)));
        }
    }
}
