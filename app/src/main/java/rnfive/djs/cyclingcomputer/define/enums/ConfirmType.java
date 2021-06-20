package rnfive.djs.cyclingcomputer.define.enums;

public enum ConfirmType {
    OTHER(0,"Other"),
    SAVE(1,"Save"),
    PERMISSIONS(2,"Permissions"),
    SENSOR(3,"Device"),
    EXIT(4,"Exit"),
    TEST(-1,"Run");

    private final int intValue;
    private final String name;

    ConfirmType(int intValue,String name) {
        this.intValue = intValue;
        this.name = name;
    }

    public static ConfirmType getValueFromInt(int intValue) {
        intValue &= -129;
        ConfirmType[] arr$ = values();

        for (ConfirmType c$ : arr$) {
            if (c$.intValue == intValue) {
                return c$;
            }
        }

        return OTHER;
    }

    public String toString() {
        return this.name;
    }

}
