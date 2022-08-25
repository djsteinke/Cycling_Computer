package rnfive.htfu.cyclingcomputer.define;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import static rnfive.htfu.cyclingcomputer.define.Filters.doubleLPFilter;
import static rnfive.htfu.cyclingcomputer.define.Lists.addValue;
import static rnfive.htfu.cyclingcomputer.define.Lists.getAvg;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bMoving;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bStarted;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.roundDouble;
import static rnfive.htfu.cyclingcomputer.service.Service_Recording.location;

@Getter
@Setter
class Altitude {

    private static final String TAG = Altitude.class.getSimpleName();
    private static final double altitudeCutoff = 0.35d;
    private static final double sensorCutoff = 100.0d;
    private static final int pressureListSize = 10;
    private static final int minDistAscent = 40;
    private static final int minTimeAscent = 10;

    private double altitude;
    private double ascent;
    private double descent;
    private double ascentLap;
    private double descentLap;
    private double grade;

    private double lastAltAscent;
    private double p2pAscent;
    private Location lastLocation;
    private int cntAscent;

    private double absolutePressure = 1013.25;
    private double pressure;
    private double lastPressure;

    private double lpPressure;

    private final List<Double> sensorPressureList = new ArrayList<>();

    Altitude() {}

    void updatePressure(double inPressure) {
        inPressure = roundDouble(inPressure, 2);
        /*
        if (lastPressure == 0.0d)
            lastPressure = inPressure;
        if (Math.abs(inPressure-lastPressure) < sensorCutoff) {
            addValue(sensorPressureList, inPressure, pressureListSize);
        }
        pressure = roundDouble(getAvg(sensorPressureList), 2);
        lastPressure = inPressure;
        */
        if (lpPressure == 0)
            lpPressure = inPressure;
        lpPressure = roundDouble(doubleLPFilter(lpPressure, inPressure),2);
    }

    void updateAltitude() {
        //Log.d(TAG, "Pressure[" + pressure + "] LP[" + lpPressure + "]");

        double alt = roundDouble(calculateAltitude(lpPressure), 1);
        if (Math.abs(alt - altitude) >= altitudeCutoff)
            altitude = alt;

        if (bMoving && bStarted) {
            if (lastAltAscent == 0)
                lastAltAscent = altitude;
            if (lastLocation == null)
                lastLocation = location;
            double p2p = location.distanceTo(lastLocation);
            p2pAscent += p2p;
            cntAscent ++;
            if (p2pAscent >= minDistAscent && cntAscent >= minTimeAscent)
                updateAscent();
            lastLocation = location;
        }
    }

    private double total() {
        return sensorPressureList.stream().mapToDouble(Double::doubleValue).sum();
    }

    private double calculateAltitude(double pressure) {
        return (1 - StrictMath.pow(pressure / absolutePressure, 1 / 5.25588)) / 0.0000225577;
    }

    private void updateAscent() {
        double diff = altitude - lastAltAscent;
        if (diff > 0) {
            ascent += diff;
            ascentLap += diff;
        } else {
            descent += diff;
            descentLap += diff;
        }
        lastAltAscent = altitude;
        p2pAscent = 0.0d;
        cntAscent = 0;
    }

}

