package rnfive.htfu.ant.antplus.pcc.defines;

public enum RequestStatus {
    SUCCESS(0),
    FAIL_CANCELLED(-2),
    UNRECOGNIZED(-3),
    FAIL_OTHER(-10),
    FAIL_ALREADY_BUSY_EXTERNAL(-20),
    FAIL_DEVICE_COMMUNICATION_FAILURE(-40),
    FAIL_DEVICE_TRANSMISSION_LOST(-41),
    FAIL_BAD_PARAMS(-50),
    FAIL_NO_PERMISSION(-60),
    FAIL_NOT_SUPPORTED(-61),
    FAIL_PLUGINS_SERVICE_VERSION(-62);

    private final int intValue;

    RequestStatus(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static RequestStatus getValueFromInt(int intValue) {
        RequestStatus[] var1 = values();

        for (RequestStatus status : var1) {
            if (status.getIntValue() == intValue) {
                return status;
            }
        }

        return UNRECOGNIZED;
    }
}
