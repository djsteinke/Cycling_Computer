package rnfive.djs.ant.utility.uuid;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import rnfive.djs.ant.utility.log.LogAnt;

public class UniqueIdGenerator {
    private static final String TAG = UniqueIdGenerator.class.getSimpleName();
    protected static final String PREFS_FILE = "device_id.xml";
    protected static final String PREFS_DEVICE_ID = "device_id";

    public UniqueIdGenerator() {
    }

    public static int getTwoByteUniqueId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), "android_id");

        try {
            SharedPreferences prefs = context.getSharedPreferences("device_id.xml", 0);
            String id = prefs.getString("device_id", (String)null);
            UUID uuid;
            if (id != null) {
                uuid = UUID.fromString(id);
            } else {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                } else {
                    uuid = UUID.randomUUID();
                }

                prefs.edit().putString("device_id", uuid.toString()).commit();
            }

            int generatedID = (int)(65535L & uuid.getLeastSignificantBits());
            return generatedID;
        } catch (UnsupportedEncodingException var6) {
            LogAnt.e(context.getClass().getSimpleName(), "UnsupportedEncodingException trying to decode Andriod ID as utf8");
            return -1;
        }
    }

    public static long getFourByteUniqueId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), "android_id");

        try {
            SharedPreferences prefs = context.getSharedPreferences("device_id.xml", Context.MODE_MULTI_PROCESS);
            String id = prefs.getString("device_id", (String)null);
            UUID uuid;
            if (id != null) {
                uuid = UUID.fromString(id);
            } else {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                } else {
                    uuid = UUID.randomUUID();
                }

                prefs.edit().putString("device_id", uuid.toString()).commit();
            }

            long generatedDeviceID = 4294967295L & uuid.getLeastSignificantBits();
            return generatedDeviceID;
        } catch (UnsupportedEncodingException var7) {
            LogAnt.e(TAG, "UnsupportedEncodingException trying to decode Andriod ID as utf8");
            return -1L;
        }
    }
}
