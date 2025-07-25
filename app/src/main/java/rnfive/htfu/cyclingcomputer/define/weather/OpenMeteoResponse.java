package rnfive.htfu.cyclingcomputer.define.weather;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenMeteoResponse {
    private double latitude;
    private double longitude;
    private double elevation;
    private Current current;

    public OpenMeteoResponse() { }

    public double getTemperature() {
        return current.getTemperature_2m();
    }

    public double getWindSpeed() {
        return current.getWind_speed_10m();
    }

    public int getWindDirection() {
        return current.getWind_direction_10m();
    }

    public double getPressureSeaLevel() {
        return current.getPressure_msl();
    }

    @Getter
    @Setter
    private static final class Current {
        private String time;
        private int interval;
        private double temperature_2m;
        private double wind_speed_10m;
        private int wind_direction_10m;
        private double pressure_msl;
        private Current() { }
    }
}
