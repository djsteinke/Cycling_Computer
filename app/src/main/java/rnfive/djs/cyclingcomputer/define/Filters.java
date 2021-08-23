package rnfive.djs.cyclingcomputer.define;

public class Filters {
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

    public static double doubleLPFilter(double oldVal, double newVal) {
        double f_alpha = 0.25d;
        return oldVal + f_alpha * (newVal - oldVal);
    }
}
