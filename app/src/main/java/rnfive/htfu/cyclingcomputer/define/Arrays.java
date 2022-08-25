package rnfive.htfu.cyclingcomputer.define;

import java.util.ArrayList;
import java.util.List;

public class Arrays {
    private Arrays() {}

    public static <T> double getAvg(T[] inArray) {
        List<Double> outList = new ArrayList<>();
        for (T f : inArray) {
            if (f instanceof Float)
                outList.add(((Float) f).doubleValue());
            else if (f instanceof Double)
                outList.add((Double) f);
            else if (f instanceof Integer)
                outList.add(((Integer) f).doubleValue());
        }
        return Lists.avg(outList);
    }

    public static <T> void updateArray(T[] array, T value) {
        int i = array.length-1;
        while (i>0)
            array[i] = array[--i];
        array[0] = value;
    }

    public static int[] updateIntArray(int[] array, int value) {
        int i = array.length-1;
        while (i>0)
            array[i] = array[--i];
        array[0] = value;
        return array;
    }

    public static int getIntArrayAvg(int[] array) {
        int tot = 0;
        int length = array.length;
        for(int i : array) {
            tot += i;
        }

        if (length == 0)
            return 0;
        else
            return tot/length;
    }

    public static float[] updateFloatArray(float[] array, float value) {
        int i = array.length-1;
        while (i>0)
            array[i] = array[--i];
        array[0] = value;
        return array;
    }

    public static double[] updateDoubleArray(double[] array, double value) {
        int i = array.length-1;
        while (i>0)
            array[i] = array[--i];
        array[0] = value;
        return array;
    }

    public static float getFloatArrayAvg(float[] array) {
        float avg = 0f;
        float length = (float)array.length;
        for(float f : array) {
            avg += f;
        }

        if (length == 0)
            return 0f;
        else
            return avg/length;
    }

    public static double getDoubleArrayAvg(double[] array) {
        double avg = 0f;
        double length = (double)array.length;
        for(double f : array) {
            avg += f;
        }

        if (length == 0)
            return 0f;
        else
            return avg/length;
    }
}
