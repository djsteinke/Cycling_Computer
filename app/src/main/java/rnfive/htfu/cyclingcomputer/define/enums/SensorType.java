package rnfive.htfu.cyclingcomputer.define.enums;

public enum SensorType {

    BLE_SPEED(1,"BLE Speed Device","spd"),
    BLE_SPEED_CADENCE(2,"BLE Speed/Cadence Device","spdcad"),
    BLE_CADENCE(3,"BLE Cadence Device","cad"),
    BLE_POWER(4,"BLE Power Meter","power"),
    BLE_HEART_RATE(4,"BLE Heart Rate Monitor","hr"),
    ANT_POWER(5,"ANT+ Power Meter","power");

    private final int intValue;
    private final String name;
    private final String type;

    SensorType(int intValue, String name, String type){
        this.intValue = intValue;
        this.name = name;
        this.type = type;
    }

    public int getIntValue() {
        return this.intValue;
    }

    public String getName() {return this.name;}

    public String getType() {return this.type;}
}
