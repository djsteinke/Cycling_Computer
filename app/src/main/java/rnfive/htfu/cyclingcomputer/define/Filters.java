package rnfive.htfu.cyclingcomputer.define;

public final class Filters {
    private static final double f_dulp = 0.05d;
    private static final double f_dlp = 0.2d;
    private Filters() {}

    public static float[] lowPassFilter ( float[] input, float[] output ) {
        float f_alpha = 0.25f;
        int l = input.length;
        if ( output == null ) return input;
        for ( int i=0; i<l; i++ ) {
            output[i] = output[i] + f_alpha * (input[i] - output[i]);
        }
        return output;
    }

    public static float floatLPFilter(float oldVal, float newVal) {
        float f_alpha = 0.6f;
        return oldVal + f_alpha * (newVal - oldVal);
    }

    public static float floatUltraLPFilter(float oldVal, float newVal) {
        float f_alpha = 0.05f;
        return oldVal + f_alpha * (newVal - oldVal);
    }

    public static double doubleLPFilter(double oldVal, double newVal) {
        return oldVal + f_dlp * (newVal - oldVal);
    }

    public static double doubleULPFilter(double oldVal, double newVal) {
        return oldVal + f_dulp * (newVal - oldVal);
    }
}
