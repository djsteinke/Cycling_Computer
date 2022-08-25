package rnfive.htfu.ant.antplus.pcc.defines;

public enum DeviceState {
    DEAD(-100),
    CLOSED(1),
    SEARCHING(2),
    TRACKING(3),
    PROCESSING_REQUEST(300),
    UNRECOGNIZED(-1);

    private final int intValue;

    DeviceState(int inValue) {
        intValue = inValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static DeviceState getValueFromInt(int inValue) {
        DeviceState[] var1 = values();

        for (DeviceState state : var1) {
            if (state.getIntValue() == inValue) {
                return state;
            }
        }
        return UNRECOGNIZED;
    }
}
