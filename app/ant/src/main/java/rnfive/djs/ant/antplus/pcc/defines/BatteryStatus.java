package rnfive.djs.ant.antplus.pcc.defines;

public enum BatteryStatus {
    NEW(1),
    GOOD(2),
    OK(3),
    LOW(4),
    CRITICAL(5),
    INVALID(7),
    UNRECOGNIZED(-1);

    private final int intValue;

    BatteryStatus(int inVal) {
        intValue = inVal;
    }

    public int getIntValue() {
        return intValue;
    }

    public static BatteryStatus getValueFromInt(int inVal) {
        BatteryStatus[] var1 = values();

        for (BatteryStatus status : var1) {
            if (status.getIntValue() == inVal) {
                return status;
            }
        }
        return UNRECOGNIZED;
    }
}
