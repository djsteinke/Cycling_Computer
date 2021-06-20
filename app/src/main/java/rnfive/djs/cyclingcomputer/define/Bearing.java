package rnfive.djs.cyclingcomputer.define;

import android.hardware.GeomagneticField;

public class Bearing {
    private Bearing() {}

    public static String bearingToDirection(double bearing) {

        double range = (bearing / (360d / 32d));

        String sDir;
        if (range >= 31)
            sDir = "N";
        else if (range > 29)
            sDir = "NNW";
        else if (range >= 27)
            sDir = "NW";
        else if (range > 25)
            sDir = "WNW";
        else if (range >= 23)
            sDir = "W";
        else if (range > 21)
            sDir = "WSW";
        else if (range >= 19)
            sDir = "SW";
        else if (range > 17)
            sDir = "SSW";
        else if (range >= 15)
            sDir = "S";
        else if (range > 13)
            sDir = "SSE";
        else if (range >= 11)
            sDir = "SE";
        else if (range > 9)
            sDir = "ESE";
        else if (range >= 7)
            sDir = "E";
        else if (range > 5)
            sDir = "ENE";
        else if (range >= 3)
            sDir = "NE";
        else if (range > 1)
            sDir = "NNE";
        else
            sDir = "N";

        return sDir;
    }

    public static float roundBearing(float bearing) {

        if (bearing < 0)
            bearing += 360;
        else if (bearing > 360)
            bearing -= 360;
        float range = (bearing / (360f / 32f));

        float fDir;
        if (range >= 31)
            fDir = 0;
        else if (range > 29)
            fDir = 30;
        else if (range >= 27)
            fDir = 28;
        else if (range > 25)
            fDir = 26;
        else if (range >= 23)
            fDir = 24;
        else if (range > 21)
            fDir = 22;
        else if (range >= 19)
            fDir = 20;
        else if (range > 17)
            fDir = 18;
        else if (range >= 15)
            fDir = 16;
        else if (range > 13)
            fDir = 14;
        else if (range >= 11)
            fDir = 12;
        else if (range > 9)
            fDir = 10;
        else if (range >= 7)
            fDir = 8;
        else if (range > 5)
            fDir = 6;
        else if (range >= 3)
            fDir = 4;
        else if (range > 1)
            fDir = 2;
        else
            fDir = 0;

        return fDir*(360f/32f);
    }

    public static int semicircleFromDegrees(double val) {
        return (int) (val*(Math.pow(2,31)/180));
    }

    public static double degreesFromSemicircle(int val) {return (double)val/Math.pow(2,31)*180f;}

    public static double determineDirection(double i_bearing, GeomagneticField geomagneticField) {

        if (geomagneticField != null) {
            i_bearing += geomagneticField.getDeclination();
        }

        // bearing must be in 0-360
        if (i_bearing < 0) {
            i_bearing += 360;
        }

        return i_bearing;
    }
}
