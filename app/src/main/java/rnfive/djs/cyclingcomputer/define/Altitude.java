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
    private static final int minDistGrade = 10;
    private static final int minDistAscent = 25;

    private double altitude;
    private double ascent;
    private double descent;
    private double ascentLap;
    private double descentLap;
    private double grade;

    private double ascentLastAlt;
    private Location ascentLastLoc;
    private Location gradeLastLoc;
    private List<Point> gradePoints = new ArrayList<>();
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
            updateGradePoints();
            if (ascentLastAlt == 0)
                ascentLastAlt = alt;
            if (ascentLastLoc == null)
                ascentLastLoc = location;
            double p2p = location.distanceTo(ascentLastLoc);
            if (p2p >= minDistAscent) {
                updateAscent(altitude, ascentLastAlt);
                ascentLastLoc = location;
                ascentLastAlt = altitude;
            }
        }
    }

    private void updateGradePoints() {
        double p2p = location.distanceTo(gradeLastLoc);
        if (p2p >= minDistGrade) {
            Point p = new Point(altitude, p2p);
            addValue(gradePoints, p, 3);
            if (gradePoints.size() > 2)
                updateGrade();
            gradeLastLoc = location;
        }
    }

    private double total() {
        return sensorPressureList.stream().mapToDouble(Double::doubleValue).sum();
    }

    private double calculateAltitude(double pressure) {
        return (1 - StrictMath.pow(pressure / absolutePressure, 1 / 5.25588)) / 0.0000225577;
    }

    private void updateGrade() {
        Point p1 = gradePoints.get(0);
        Point p2 = gradePoints.get(1);
        Point p3 = gradePoints.get(2);
        double alt = p1.getAltitude() - p3.getAltitude();
        double dist = p1.getDistance() + p2.getDistance() + p3.getDistance();
        double g = alt/dist*100.0f;
        if (g < 50)
            data.setGrade((float) g);
    }

    private void updateAscent(double newVal, double oldVal) {
        double diff = newVal - oldVal;
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

    @Getter
    @Setter
    private static class Point {
        private double altitude;
        private double distance;

        Point() {}
        Point(double altitude, double distance) {
            this.altitude = altitude;
            this.distance = distance;
        }
    }

}

