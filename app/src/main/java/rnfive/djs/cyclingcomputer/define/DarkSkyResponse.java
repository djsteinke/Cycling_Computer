package rnfive.djs.cyclingcomputer.define;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DarkSkyResponse {

    private double latitude;
    private double longitude;
    private String timezone;
    private DarkSkyCurrently currently;

    public DarkSkyResponse() {}

    public double getPressure() {
        if (currently != null)
            return currently.getPressure();
        else
            return 0.0d;
    }

    public double getTemperature() {
        if (currently != null)
            return currently.getTemperature();
        else
            return 0.0d;
    }

    public double getWindBearing() {
        if (currently != null)
            return currently.getWindBearing();
        else
            return 0.0d;
    }

    public double getWindSpeed() {
        if (currently != null)
            return currently.getWindSpeed();
        else
            return 0.0d;
    }
}
