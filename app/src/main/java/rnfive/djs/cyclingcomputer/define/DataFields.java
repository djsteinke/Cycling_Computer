package rnfive.djs.cyclingcomputer.define;

import android.util.SparseArray;

import rnfive.djs.cyclingcomputer.R;

public class DataFields {
    private DataFields() {}

    public static final int NONE = 2000;
    public static final int EMPTY = 2033;
    public static final int TIME = 2001;
    public static final int TIME_LAP = 2002;
    public static final int GRADE = 2003;
    public static final int DISTANCE = 2004;
    public static final int DISTANCE_LAP = 2005;
    public static final int ASCENT = 2006;
    public static final int DESCENT = 2007;

    public static final int SPEED = 2008;
    public static final int SPEED_AVG = 2009;
    public static final int SPEED_LAP = 2010;
    public static final int PACE = 2011;
    public static final int PACE_AVG = 2012;
    public static final int PACE_LAP = 2013;

    public static final int HR = 2014;
    public static final int HR_AVG = 2015;
    public static final int HR_LAP = 2016;

    public static final int CADENCE = 2017;
    public static final int CADENCE_AVG = 2018;
    public static final int CADENCE_LAP = 2019;

    public static final int POWER = 2020;
    public static final int POWER_AVG = 2021;
    public static final int POWER_LAP = 2022;
    public static final int POWER_3S = 2023;
    public static final int POWER_10S = 2024;
    public static final int POWER_30S = 2025;
    public static final int TORQUE = 2026;
    public static final int SMOOTHNESS = 2028;
    public static final int BALANCE = 2027;

    public static final int TEMPERATURE = 2029;
    public static final int WIND = 2030;
    public static final int GPS_ACCURACY = 2031;
    public static final int BEARING = 2032;
    public static final int ALTITUDE = 2034;
    public static final int ASCENT_LAP = 2035;
    public static final int DESCENT_LAP = 2036;
    public static final int TEST = 2037;
    public static final int TIME_OF_DAY = 2038;
    public static final int GEAR_RATIO = 2039;

    public static final int[][] HR_ZONE = new int[][] {
            {0,R.color.black,R.color.white},
            {65,R.color.hr_blue,R.color.hr_blue_i},
            {75,R.color.hr_green,R.color.hr_green_i},
            {82,R.color.hr_yellow,R.color.hr_yellow_i},
            {89,R.color.hr_orange,R.color.hr_orange_i},
            {94,R.color.hr_red,R.color.hr_red_i}};

    public static final int[][] POWER_ZONE = new int[][] {
            {0,R.color.black,R.color.white},
            {56,R.color.hr_blue,R.color.hr_blue_i},
            {76,R.color.hr_green,R.color.hr_green_i},
            {91,R.color.hr_yellow,R.color.hr_yellow_i},
            {106,R.color.hr_orange,R.color.hr_orange_i},
            {121,R.color.hr_red,R.color.hr_red_i}};

    public static final Integer[] dataFieldMapDefault = new Integer[] {-1,-1,-1,-1,-1,-1,-1};
    public static final SparseArray<Integer[]> dataFieldMap = new SparseArray<>();
    // Integer[] Title, Short Title, Unit Top Imperial, Unit Top Metric, Unit Bot Imp, Unit Bot Metric, Device Icon
    static {
        dataFieldMap.put(NONE,new Integer[] {R.string.none,-1,-1,-1,-1,-1,-1});
        dataFieldMap.put(EMPTY,new Integer[] {R.string.empty,-1,-1,-1,-1,-1,-1});
        dataFieldMap.put(TIME,new Integer[] {R.string.time,R.string.time,-1,-1,-1,-1,-1});
        dataFieldMap.put(TIME_LAP,new Integer[] {R.string.time_lap,R.string.time_lap,-1,-1,-1,-1});
        dataFieldMap.put(SPEED,new Integer[] {R.string.speed,R.string.speed,R.string.m,R.string.k,R.string.h,R.string.h,-1});
        dataFieldMap.put(SPEED_AVG,new Integer[] {R.string.speed_avg,R.string.speed_avg,R.string.m,R.string.k,R.string.h,R.string.h,-1});
        dataFieldMap.put(SPEED_LAP,new Integer[] {R.string.speed_lap,R.string.speed_lap,R.string.m,R.string.k,R.string.h,R.string.h,-1});
        dataFieldMap.put(PACE,new Integer[] {R.string.pace,R.string.pace,R.string.min,R.string.min,R.string.mi,R.string.km,-1});
        dataFieldMap.put(PACE_AVG,new Integer[] {R.string.pace_avg,R.string.pace_avg,R.string.min,R.string.min,R.string.mi,R.string.km,-1});
        dataFieldMap.put(PACE_LAP,new Integer[] {R.string.pace_lap,R.string.pace_lap,R.string.min,R.string.min,R.string.mi,R.string.km,-1});
        dataFieldMap.put(HR,new Integer[] {R.string.heart_rate,R.string.HR,R.string.b,R.string.b,R.string.m,R.string.m,-1});
        dataFieldMap.put(HR_AVG,new Integer[] {R.string.heart_rate_avg,R.string.HR_avg,R.string.b,R.string.b,R.string.m,R.string.m,-1});
        dataFieldMap.put(HR_LAP,new Integer[] {R.string.heart_rate_lap,R.string.HR_lap,R.string.b,R.string.b,R.string.m,R.string.m,-1});
        dataFieldMap.put(CADENCE,new Integer[] {R.string.cadence,R.string.cadence,R.string.r,R.string.r,R.string.m,R.string.m,-1});
        dataFieldMap.put(CADENCE_AVG,new Integer[] {R.string.cadence_avg,R.string.cad_avg,R.string.r,R.string.r,R.string.m,R.string.m,-1});
        dataFieldMap.put(CADENCE_LAP,new Integer[] {R.string.cadence_avg,R.string.cad_lap,R.string.r,R.string.r,R.string.m,R.string.m,-1});
        dataFieldMap.put(POWER,new Integer[] {R.string.power,R.string.power,R.string.w,R.string.w,-1,-1,-1});
        dataFieldMap.put(POWER_AVG,new Integer[] {R.string.power_avg,R.string.power_avg,R.string.w,R.string.w,-1,-1,-1});
        dataFieldMap.put(POWER_LAP,new Integer[] {R.string.power_lap,R.string.power_lap,R.string.w,R.string.w,-1,-1,-1});
        dataFieldMap.put(POWER_3S,new Integer[] {R.string.power_3s,R.string.power_3s,R.string.w,R.string.w,-1,-1,-1});
        dataFieldMap.put(POWER_10S,new Integer[] {R.string.power_10s,R.string.power_10s,R.string.w,R.string.w,-1,-1,-1});
        dataFieldMap.put(POWER_30S,new Integer[] {R.string.power_30s,R.string.power_30s,R.string.w,R.string.w,-1,-1,-1});
        dataFieldMap.put(TORQUE,new Integer[] {R.string.torque_effectiveness,R.string.torque,R.string.percent,R.string.percent,-1,-1,-1});
        dataFieldMap.put(BALANCE,new Integer[] {R.string.balance,R.string.balance,R.string.percent,R.string.percent,-1,-1,-1});
        dataFieldMap.put(SMOOTHNESS,new Integer[] {R.string.pedal_smoothness,R.string.smoothness,R.string.percent,R.string.percent,-1,-1,-1});
        dataFieldMap.put(DISTANCE,new Integer[] {R.string.distance,R.string.distance,R.string.mi,R.string.km,-1,-1,-1});
        dataFieldMap.put(DISTANCE_LAP,new Integer[] {R.string.distance_lap,R.string.distance_lap,R.string.mi,R.string.km,-1,-1,-1});
        dataFieldMap.put(ASCENT,new Integer[] {R.string.ascent,R.string.ascent,R.string.ft,R.string.m,-1,-1,-1});
        dataFieldMap.put(DESCENT,new Integer[] {R.string.descent,R.string.descent,R.string.ft,R.string.m,-1,-1,-1});
        dataFieldMap.put(GRADE,new Integer[] {R.string.grade,R.string.grade,R.string.percent,R.string.percent,-1,-1,-1});
        dataFieldMap.put(TEMPERATURE,new Integer[] {R.string.temperature,R.string.temperature,R.string.degF,R.string.degC,-1,-1,-1});
        dataFieldMap.put(WIND,new Integer[] {R.string.wind,R.string.wind,R.string.m,R.string.k,R.string.h,R.string.h,-1});
        dataFieldMap.put(BEARING,new Integer[] {R.string.bearing,R.string.bearing,-1,-1,-1,-1,-1});
        dataFieldMap.put(GPS_ACCURACY,new Integer[] {R.string.gps_accuracy,R.string.gps_accuracy,R.string.ft,R.string.m,-1,-1,-1});
        dataFieldMap.put(ALTITUDE,new Integer[] {R.string.altitude,R.string.altitude,R.string.ft,R.string.m,-1,-1,-1});
        dataFieldMap.put(ASCENT_LAP,new Integer[] {R.string.ascent_lap,R.string.ascent_lap,R.string.ft,R.string.m,-1,-1,-1});
        dataFieldMap.put(DESCENT_LAP,new Integer[] {R.string.descent_lap,R.string.descent_lap,R.string.ft,R.string.m,-1,-1,-1});
        dataFieldMap.put(TEST,new Integer[] {R.string.test,R.string.test,R.string.m,R.string.k,R.string.h,R.string.h,-1});
        dataFieldMap.put(TIME_OF_DAY,new Integer[] {R.string.time_of_day,R.string.time_of_day,-1,-1,-1});
        dataFieldMap.put(GEAR_RATIO,new Integer[] {R.string.gear_ratio,R.string.gear_ratio,-1,-1,-1});
    }
}
