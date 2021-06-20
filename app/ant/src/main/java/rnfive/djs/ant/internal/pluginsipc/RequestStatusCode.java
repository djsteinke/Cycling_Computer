package rnfive.djs.ant.internal.pluginsipc;

public class RequestStatusCode {
    public static final int SUCCESS = 0;
    public static final int FAIL_CANCELLED = -2;
    public static final int FAIL_OTHER = -10;
    public static final int FAIL_ALREADY_BUSY_EXTERNAL = -20;
    public static final int FAIL_DEVICE_COMMUNICATION_FAILURE = -40;
    public static final int FAIL_DEVICE_TRANSMISSION_LOST = -41;
    public static final int FAIL_BAD_PARAMS = -50;
    public static final int FAIL_NO_PERMISSION = -60;
    public static final int FAIL_NOT_SUPPORTED = -61;

    public RequestStatusCode() {
    }
}
