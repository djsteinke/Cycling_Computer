package rnfive.djs.cyclingcomputer.define;

import lombok.Data;

@Data
public class PowerField {
    private String name;
    private Integer format;
    private Integer exponent;
    private Integer bytes;
    private boolean mandatory;

    public PowerField() {}
    public PowerField(String name, int format, int exponent, Integer bytes, boolean mandatory) {
        this.name = name;
        this.format = format;
        this.exponent = exponent;
        this.bytes = bytes;
        this.mandatory = mandatory;
    }
}
