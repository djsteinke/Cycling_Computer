package rnfive.htfu.cyclingcomputer.define;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DarkSkyCurrently {
    private double pressure;
    private double temperature;
    private double windSpeed;
    private double windBearing;
    private long time;

    public DarkSkyCurrently() {}
}
