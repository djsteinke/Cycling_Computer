package rnfive.djs.cyclingcomputer.define;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import static rnfive.djs.cyclingcomputer.define.Lists.addValue;
import static rnfive.djs.cyclingcomputer.define.Lists.getAvg;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bMoving;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bStarted;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.data;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.location;

@Getter
@Setter
class Altitude {

    private static final String TAG = Altitude.class.getSimpleName();
    private static final double altitudeCutoff = 0.5d;
    private static final double sensorCutoff = 75.0d;
    private static final int listSize = 5;
    private static final int calculateDist = 10;

    private double altitude;
    private double ascent;
    private double descent;
    private double ascentLap;
    private double descentLap;
    private double grade;

    private List<Location> locationList = new ArrayList<>();
    private List<Double> altitudeList = new ArrayList<>();
    private double lastAlt;
    private Location lastLoc;
    private double absolutePressure = 1013.25;
    private double pressure;
    private double sensorPressure;
    private double pressureTotal;

    private final List<Double> sensorPressureList = new ArrayList<>();

    Altitude() {}

    void updatePressure(double inPressure) {
        if (sensorPressure == 0.0d)
            sensorPressure = inPressure;
        if (Math.abs(inPressure-sensorPressure) < sensorCutoff) {
            addValue(sensorPressureList, inPressure, listSize);
            if (sensorPressureList.size() >= listSize) {
                if (pressure == 0) {
                    pressure = getAvg(sensorPressureList);
                    pressureTotal = total();
                }
                double tot = total();
                pressure += (tot - pressureTotal)/listSize;
                pressureTotal = tot;
            }
        }
        sensorPressure = inPressure;
    }

    void updateAltitude() {
        double alt = calculateAltitude(pressure);
        if (altitude == 0 || Math.abs(alt - altitude) > altitudeCutoff)
            altitude = alt;

        if (bMoving) {
            if (lastAlt == 0)
                lastAlt = alt;
            if (lastLoc == null)
                lastLoc = location;
            double distanceP2P = location.distanceTo(lastLoc);
            if (distanceP2P >= calculateDist) {
                updateAscent(altitude, lastAlt);
                updateGrade(distanceP2P, altitude, lastAlt);
            }
            /*
            addValue(locationList, location, 10);
            addValue(altitudeList, altitude, 10);

            int i = 0;
            for (Location l : locationList) {
                double distanceP2P = location.distanceTo(l);
                if (distanceP2P >= calculateDist) {
                    updateAscent(altitude, altitudeList.get(i));
                    updateGrade(distanceP2P, altitude, altitudeList.get(i));
                    break;
                }
                i++;
            }

             */
        }
    }

    private double total() {
        return sensorPressureList.stream().mapToDouble(Double::doubleValue).sum();
    }

    private double calculateAltitude(double pressure) {
        return (1 - StrictMath.pow(pressure / absolutePressure, 1 / 5.25588)) / 0.0000225577;
    }

    private static void updateGrade(double distanctP2P, double alt1, double alt2) {
        double altDiff = alt1 - alt2;
        double grade = altDiff/distanctP2P*100.0f;
        if (grade < 50)
            data.setGrade((float) grade);
    }

    private void updateAscent(double newVal, double oldVal) {
        double diff = Math.round((newVal-oldVal)*2)/2.0d;
        if (bStarted && bMoving) {
            if (diff > 0) {
                ascent += diff;
                ascentLap += diff;
            } else {
                descent += diff;
                descentLap += diff;
            }
        }
    }

}

