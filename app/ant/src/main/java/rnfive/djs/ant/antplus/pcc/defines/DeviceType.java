package rnfive.djs.ant.antplus.pcc.defines;

import androidx.annotation.NonNull;

public enum DeviceType {
    BIKE_POWER(11, "Bike Power Sensors"),
    CONTROLLABLE_DEVICE(16, "Controls"),
    FITNESS_EQUIPMENT(17, "Fitness Equipment Devices"),
    BLOOD_PRESSURE(18, "Blood Pressure Monitors"),
    GEOCACHE(19, "Geocache Transmitters"),
    ENVIRONMENT(25, "Environment Sensors"),
    WEIGHT_SCALE(119, "Weight Sensors"),
    HEARTRATE(120, "Heart Rate Sensors"),
    BIKE_SPDCAD(121, "Bike Speed and Cadence Sensors"),
    BIKE_CADENCE(122, "Bike Cadence Sensors"),
    BIKE_SPD(123, "Bike Speed Sensors"),
    STRIDE_SDM(124, "Stride-Based Speed and Distance Sensors"),
    UNKNOWN(-1, "Unknown");

    private final int intValue;
    private final String name;

    DeviceType(int intValue, String name) {
        this.intValue = intValue;
        this.name = name;
    }

    public int getIntValue() {
        return intValue;
    }

    public static DeviceType getValueFromInt(int inValue) {
        int value = inValue;
        value &= -129;
        DeviceType[] var1 = values();

        for (DeviceType dt : var1) {
            if (dt.getIntValue() == value) {
                return dt;
            }
        }

        return UNKNOWN;
    }

    @NonNull
    public String toString() {
        return name;
    }
}
