package rnfive.htfu.cyclingcomputer.define;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class Feedback {
    private Feedback() {}

    public static void tick(Context context) {
        Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK),null);
            else
                vib.vibrate(150);
    }

    public static void click(Context context) {
        Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),null);
            else
                vib.vibrate(300);
    }
}
