package rnfive.htfu.cyclingcomputer.define.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import rnfive.htfu.cyclingcomputer.R;
import rnfive.htfu.cyclingcomputer.define.DataFields;

@Getter
public enum DataFieldName {
    
    DATA_FIELDS(1,R.string.data_fields),
    NONE(0,DataFields.NONE),
    EMPTY(0,DataFields.EMPTY),
    GENERAL(1,R.string.general),
    ALTITUDE(0,DataFields.ALTITUDE),
    ASCENT(0,DataFields.ASCENT),
    BEARING(0,DataFields.BEARING),
    DESCENT(0,DataFields.DESCENT),
    DISTANCE(0,DataFields.DISTANCE),
    DISTANCE_LAP(0,DataFields.DISTANCE_LAP),
    GEAR_RATIO(0,DataFields.GEAR_RATIO),
    GPS_ACCURACY(0,DataFields.GPS_ACCURACY),
    GRADE(0,DataFields.GRADE),
    ANGLE(0,DataFields.ANGLE),
    TEMPERATURE(0,DataFields.TEMPERATURE),
    TIME_OF_DAY(0,DataFields.TIME_OF_DAY),
    WIND(0,DataFields.WIND),
            //TEST(0,TEST),
    TIME_TITLE(1,R.string.time),
            //TIME_MOVING(0,R.string.time_moving),
            //TIME_MOVING_LAP(0,R.string.time_moving_lap),
    TIME(0,DataFields.TIME),
    TIME_LAP(0,DataFields.TIME_LAP),
    CADENCE_TITLE(1,R.string.cadence),
    CADENCE(0,DataFields.CADENCE),
    CADENCE_AVG(0,DataFields.CADENCE_AVG),
    CADENCE_LAP(0,DataFields.CADENCE_LAP),
    HR_TITLE(1,R.string.heart_rate),
    HR(0,DataFields.HR),
    HR_AVG(0,DataFields.HR_AVG),
    HR_LAP(0,DataFields.HR_LAP),
    POWER_TITLE(1,R.string.power),
    POWER(0,DataFields.POWER),
    POWER_AVG(0,DataFields.POWER_AVG),
    POWER_3S(0,DataFields.POWER_3S),
    POWER_10S(0,DataFields.POWER_10S),
    POWER_30S(0,DataFields.POWER_30S),
    POWER_LAP(0,DataFields.POWER_LAP),
    BALANCE(0,DataFields.BALANCE),
    TORQUE(0,DataFields.TORQUE),
    SMOOTHNESS(0,DataFields.SMOOTHNESS),
            //WATTS_PER_KG(0,R.string.watts_per_kg),
    SPEED_TITLE(1,R.string.speed_pace),
    SPEED(0,DataFields.SPEED),
    SPEED_AVG(0,DataFields.SPEED_AVG),
    SPEED_LAP(0,DataFields.SPEED_LAP),
    PACE(0,DataFields.PACE),
    PACE_AVG(0,DataFields.PACE_AVG),
    PACE_LAP(0,DataFields.PACE_LAP);

    private final int flag;
    private final int name;

    DataFieldName(int flag, int name) {
        this.name =name;
        this.flag =flag;
    }

    public static int[][] getIntArray() {
        DataFieldName[] arr = values();
        int size = arr.length;
        int[][] ret = new int[size][2];

        for (int i=0 ; i < size ; i++) {
            ret[i] = new int[] {arr[i].getFlag(), arr[i].getName()};
        }

        return ret;
    }

    public static List<DataFieldName> toList() {
        return new ArrayList<>(Arrays.asList(values()));
    }
}
