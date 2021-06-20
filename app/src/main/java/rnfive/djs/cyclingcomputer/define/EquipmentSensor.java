package rnfive.djs.cyclingcomputer.define;

import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.Nullable;

public class EquipmentSensor {
    private EquipmentSensor() {}

    // T
    public static final int BLE = 0;
    public static final int ANT = 1;

    // V
    public static final int SPEED = 0;
    public static final int CADENCE = 1;
    public static final int SPDCAD = 2;
    public static final int HEARTRATE = 3;
    public static final int POWER = 4;
    public static final int FITNESSMCHN = 5;
    public static final int DEFAULT = -1;

    public static int getSensorId(int band, int type) {
        return (band*100+type);
    }

    private static final SparseArray<String> sensorTypes = new SparseArray<>();
    static {
        sensorTypes.append(SPEED,"Speed");
        sensorTypes.append(CADENCE,"Cadence");
        sensorTypes.append(SPDCAD,"Speed/Cadence");
        sensorTypes.append(HEARTRATE,"Heart Rate");
        sensorTypes.append(POWER,"Power Meter");
        sensorTypes.append(FITNESSMCHN,"Fitness Machine");
    }

    private static final SparseIntArray sensorBleServiceMap = new SparseIntArray();
    /*
    static {
        sensorBleServiceMap.append(GattServices.FITNESS_MACHINE,FITNESSMCHN);
        sensorBleServiceMap.append(GattServices.CYCLING_POWER,POWER);
        sensorBleServiceMap.append(GattServices.HEART_RATE,HEARTRATE);
        sensorBleServiceMap.append(GattServices.CYCLING_SPEED_AND_CADENCE,SPDCAD);
    }

     */

    public static Integer getBleSensorId(int serviceId) {
        int type = sensorBleServiceMap.get(serviceId,DEFAULT);
        if (type == DEFAULT)
            return null;
        else
            return getSensorId(BLE,type);
    }

    public static String getSensorName(@Nullable Integer sensorId) {
        if (sensorId == null)
            return null;
        else
            return (getBand(sensorId)==BLE?"BLE ":"ANT+ ") + sensorTypes.get(getType(sensorId));
    }

    public static int getBand(int sensorId) {
        return sensorId/100;
    }

    public static int getType(int sensorId) {
        return sensorId-(getBand(sensorId)*100);
    }

}
