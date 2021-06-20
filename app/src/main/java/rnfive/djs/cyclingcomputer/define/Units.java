package rnfive.djs.cyclingcomputer.define;

import static rnfive.djs.cyclingcomputer.define.StaticVariables.bMetric;

public class Units {
    public static final float FC_MPS_MPH = 2.23694f;
    public static final float FC_MPS_KPH = 3.6f;
    public static final float FC_M_FT = 3.28084f;
    public static final float FC_M_MI = 0.000621371f;
    public static final float FC_M_KM = 0.001f;

    private Units() {}

    public static float getSpeed(float speed) {
        return calcSpeed(speed);
    }

    public static float getSpeed(double speed) {
        return calcSpeed((float)speed);
    }

    public static float calcSpeed(float speed) {
        if (bMetric)
            return speed* FC_MPS_KPH;
        else
            return speed* FC_MPS_MPH;
    }

    public static float getDistance(float distance) {
        if (bMetric)
            return distance* FC_M_KM;
        else
            return distance* FC_M_MI;
    }
}
