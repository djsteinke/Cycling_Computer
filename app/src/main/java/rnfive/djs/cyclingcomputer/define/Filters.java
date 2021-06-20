package rnfive.djs.cyclingcomputer.define;

public class Filters {
    private Filters() {}

    public static float[] lowPassFilter ( float[] input, float[] output ) {
        float f_alpha = 0.25f;
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + f_alpha * (input[i] - output[i]);
        }
        return output;
    }
}
