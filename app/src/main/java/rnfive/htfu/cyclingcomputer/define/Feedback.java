package rnfive.htfu.cyclingcomputer.define;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class Feedback {
    private Feedback() {}

    public static void tick(Context context) {
        Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null)
            vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK), (AudioAttributes) null);
    }

    public static void click(Context context) {
        Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null)
            vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), (AudioAttributes) null);
    }
}
