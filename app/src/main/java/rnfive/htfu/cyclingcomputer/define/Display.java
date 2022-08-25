package rnfive.htfu.cyclingcomputer.define;

import android.util.TypedValue;

import rnfive.htfu.cyclingcomputer.MainActivity;

public class Display {
    private Display() {}

    public static int getPxFromDp(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MainActivity.displayMetrics);
    }
}
