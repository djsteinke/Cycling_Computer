package rnfive.djs.ant.antplus.pcc.defines;

public enum RequestAccessResult {
    SUCCESS(0),
    USER_CANCELLED(-2),
    CHANNEL_NOT_AVAILABLE(-3),
    OTHER_FAILURE(-4),
    DEPENDENCY_NOT_INSTALLED(-5),
    DEVICE_ALREADY_IN_USE(-6),
    SEARCH_TIMEOUT(-7),
    ALREADY_SUBSCRIBED(-8),
    BAD_PARAMS(-9),
    ADAPTER_NOT_DETECTED(-10),
    UNRECOGNIZED(-200);

    private final int intValue;

    RequestAccessResult(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static RequestAccessResult getValueFromInt(int intValue) {
        RequestAccessResult[] var1 = values();

        for (RequestAccessResult result : var1) {
            if (result.getIntValue() == intValue) {
                return result;
            }
        }

        return UNRECOGNIZED;
    }
}
