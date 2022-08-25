package rnfive.htfu.cyclingcomputer.define;

import java.util.Calendar;

import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bMetric;

public class Strings {

    private static final int[] scaleMap = new int[] {0,10,100,1000,10000,100000,1000000};

    private Strings() {}

    public static <T> String getSpeedString(T t) {
        double speed = 0;
        if (t instanceof Double)
            speed = (Double) t;
        else if (t instanceof Float)
            speed = ((Float) t).doubleValue();
        else if (t instanceof Integer)
            speed = ((Integer) t).doubleValue();
        return getNumericString(Units.getSpeed(speed),1);
    }

    public static String getDistanceString(float distance) {
        float d = Units.getDistance(distance);
        return getFloatString(d,(d>=10?1:2));
    }

    public static String getTimeString(int iMs, int iSize) {
        String sTime = "";
        int iS = iMs/1000;
        int iHr = iS/3600;
        iS -= iHr*3600;
        int iMin = iS/60;
        int iSec = iS-iMin*60;

        if (iSize == 0 || iMs/1000 >= 3600)
            sTime = (iHr > 9 ? String.valueOf(iHr) : (iHr > 0 ? "0" + iHr : "00")) + ":";
        sTime += (iMin>9?String.valueOf(iMin):(iMin>0?"0"+iMin:"00"));
        if (iSize == 0 || iMs/1000 < 3600)
            sTime += ":" + (iSec > 9 ? String.valueOf(iSec) : (iSec > 0 ? "0" + iSec : "00"));

        return sTime;
    }

    public static String getPaceString(float speedIn) {
        float speed = Units.getSpeed(speedIn);
        int iMin = (int) (60/speed);
        int iSec = (int) (60*(60/speed-iMin));

        String sTime = "00:00";
        if (iMin < 60) {
            sTime = String.valueOf(iMin);
            sTime += ":" + (iSec > 9 ? String.valueOf(iSec) : (iSec > 0 ? "0" + iSec : "00"));
        }

        return sTime;
    }

    public static String getTemperatureString(float in) {
        if (bMetric)
            return String.valueOf((int)in);
        else
            return String.valueOf((int)(1.8f*in+32));
    }

    public static String getAltitudeString(float in) {
        if (bMetric)
            return String.valueOf((int)in);
        else
            return String.valueOf((int)(in* Units.FC_M_FT));
    }

    public static String getTimeOfDayString() {
        Calendar cal = Calendar.getInstance();
        int min = cal.get(Calendar.MINUTE);
        int hr = cal.get(Calendar.HOUR);
        return (hr==0?12:hr) + ":" + (min < 10?"0"+min:String.valueOf(min)) + (cal.get(Calendar.HOUR_OF_DAY)>=12?"P":"A");
    }

    public static <T> String getNumericString(T t, int scale) {
        double value = 0;
        if (t instanceof Double)
            value = (Double) t;
        else if (t instanceof Float)
            value = ((Float) t).doubleValue();
        else if (t instanceof Integer)
            value = ((Integer) t).doubleValue();

        int intScale = scaleMap[scale];
        long lValue = Math.round(value*intScale);
        String sValue = String.valueOf(lValue/intScale);
        boolean bDec = false;
        StringBuilder decimal = new StringBuilder();
        while (intScale >= 10) {
            if (lValue < 0)
                lValue = -lValue;
            decimal.insert(0,lValue%10);
            lValue /= 10;
            intScale /= 10;
            bDec = true;
        }
        if (bDec)
            sValue += "." + decimal.toString();
        return sValue;
    }

    public static String getFloatString(float fValue, int scale) {
        int intScale = scaleMap[scale];
        int intValue = Math.round(fValue*intScale);
        String sValue = String.valueOf(intValue/intScale);
        boolean bDec = false;
        StringBuilder decimal = new StringBuilder();
        while (intScale >= 10) {
            if (intValue < 0)
                intValue = -intValue;
            decimal.insert(0,intValue%10);
            intValue /= 10;
            intScale /= 10;
            bDec = true;
        }
        if (bDec)
            sValue += "." + decimal.toString();
        return sValue;
    }
}
