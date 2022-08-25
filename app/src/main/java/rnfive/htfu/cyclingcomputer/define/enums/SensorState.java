package rnfive.htfu.cyclingcomputer.define.enums;

public enum SensorState {
    CONNECTED(1,"Connected"),
    DISCONNECTED(2,"Disconnected");

    private final int intValue;
    private final String name;

    SensorState(int intValue, String name) {
        this.intValue = intValue;
        this.name = name;
    }

    public int getIntValue() {
        return this.intValue;
    }
    public String getName() {
        return this.name;
    }
}
