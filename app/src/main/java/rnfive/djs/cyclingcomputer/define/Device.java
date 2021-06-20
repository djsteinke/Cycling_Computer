package rnfive.djs.cyclingcomputer.define;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import lombok.Data;

@Data
public class Device {

    private Long id = System.currentTimeMillis();
    private String name;
    private String displayName;
    private List<String> addressList = new ArrayList<>();
    private List<Integer> sensorIdList = new ArrayList<>();
    private Integer antId;
    private String bleMac;
    private String bleMac2;
    private Integer sensorId;
    private boolean dualChannel = false;

    public Device() {}

    public Device withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public Device withName(String name) {
        this.name = name;
        return this;
    }

    public Device withAddress(String val) {
        if (addressList != null && !addressList.isEmpty()) {
            if (!addressList.contains(val))
                addressList.add(val);
        } else {
            addressList = new ArrayList<>();
            addressList.add(val);
        }
        dualChannel = addressList.size()>1;
        return this;
    }

    public Device withSensorId(int val) {
        if (sensorIdList != null && !sensorIdList.isEmpty()) {
            if (!sensorIdList.contains(val))
                sensorIdList.add(val);
        } else {
            sensorIdList = new ArrayList<>();
            sensorIdList.add(val);
        }
        return this;
    }

    public String getSensorName() {
        if (this.sensorId != null && this.sensorId != -1) {
            return EquipmentSensor.getSensorName(sensorId);
        }
        return null;
    }

    @Override
    public @NonNull String toString() {
        StringBuilder sb = new StringBuilder();
        String val;
        boolean exists = false;
        if (this.name != null) {
            val = "Name[" + this.name + "]";
            sb.append(val);
            exists = true;
        }
        if (bleMac != null) {
            val = (exists?"\n":"") + "\tMAC[" + bleMac + "]";
            sb.append(val);
            exists = true;
        }
        if (bleMac2 != null) {
            val = (exists?"\n":"") + "\tMAC2[" + bleMac2 + "]";
            sb.append(val);
            exists = true;
        }
        if (antId != null) {
            val = (exists?"\n":"") + "\tANT ID[" + antId + "]";
            sb.append(val);
        }

        return sb.toString();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("displayName", displayName);
        jsonObject.put("name", name);
        jsonObject.put("id", id);
        jsonObject.put("bleMac", bleMac);
        jsonObject.put("bleMac2", bleMac2);
        jsonObject.put("antId", antId);
        jsonObject.put("dualChannel", dualChannel);
        jsonObject.put("sensorId", sensorId);
        JSONArray sensorArray = new JSONArray();
        if (sensorIdList != null && !sensorIdList.isEmpty()) {
            for (int i : sensorIdList)
                sensorArray.put(i);
            jsonObject.put("sensorIdList",sensorArray);
        }
        JSONArray addressArray = new JSONArray();
        if (addressList != null && !addressList.isEmpty()) {
            for (String val : addressList)
                addressArray.put(val);
            jsonObject.put("addressList",addressArray);
        }
        return jsonObject;
    }

    Device fromJSON(JSONObject jsonObject) {
        this.displayName = Json.getJSONString(jsonObject,"displayName",null);
        this.name = Json.getJSONString(jsonObject,"name",null);
        this.id = Json.getJSONLong(jsonObject,"id",null);
        this.bleMac = Json.getJSONString(jsonObject,"bleMac",null);
        this.bleMac2 = Json.getJSONString(jsonObject,"bleMac2",null);
        this.antId = Json.getJSONInt(jsonObject,"antId",null);
        this.dualChannel = Json.getJSONBoolean(jsonObject,"dualChannel", false);
        this.sensorId = Json.getJSONInt(jsonObject,"sensorId",null);
        this.addressList = Json.getJSONStringArray(jsonObject,"addressList");
        this.sensorIdList = Json.getJSONIntArray(jsonObject,"sensorIdList");
        return this;
    }
}
