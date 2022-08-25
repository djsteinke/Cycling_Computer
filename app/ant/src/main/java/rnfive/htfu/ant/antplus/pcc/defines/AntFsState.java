package rnfive.htfu.ant.antplus.pcc.defines;

public enum AntFsState {
    LINK_REQUESTING_LINK(100),
    AUTHENTICATION(500),
    AUTHENTICATION_REQUESTING_PAIRING(550),
    TRANSPORT_IDLE(800),
    TRANSPORT_DOWNLOADING(850),
    UNRECOGNIZED(-1);

    private final int intValue;

    AntFsState(int inVal) {
        intValue = inVal;
    }

    public int getIntValue() {
        return intValue;
    }

    public static AntFsState getValueFromInt(int inVal) {
        AntFsState[] var1 = values();

        for (AntFsState state : var1) {
            if (state.getIntValue() == inVal) {
                return state;
            }
        }

        return UNRECOGNIZED;
    }
}
