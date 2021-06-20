package rnfive.djs.cyclingcomputer.define;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class Lists {

    public Lists() {}

    public static <T> void addValue(List<T> inList, T value, @Nullable Integer maxLength) {
        inList.add(0, value);

        if (maxLength != null) {
            int s = inList.size();
            while (s > maxLength) {
                inList.remove(s - 1);
                s = inList.size();
            }
        }
    }

    public static <T> double getAvg(List<T> inList) {
        List<Double> outList = new ArrayList<>();
        for (T f : inList) {
            if (f instanceof Float)
                outList.add(((Float) f).doubleValue());
            else if (f instanceof Double)
                outList.add((Double) f);
            else if (f instanceof Integer)
                outList.add(((Integer) f).doubleValue());
        }
        return avg(outList);
    }

    public static double avg(List<Double> inList) {
        return inList.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0);
    }


}
