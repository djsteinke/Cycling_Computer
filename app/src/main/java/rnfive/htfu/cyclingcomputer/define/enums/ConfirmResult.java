package rnfive.htfu.cyclingcomputer.define.enums;

public enum ConfirmResult {
    POSITIVE(0,"Positive"),
    NEGATIVE(1,"Negative"),
    NEUTRAL(2,"Neutral");

    private final int intValue;
    private final String name;

    ConfirmResult(int intValue,String name) {
        this.intValue = intValue;
        this.name = name;
    }

    public static ConfirmResult getValueFromInt(int intValue) {
        intValue &= -129;
        ConfirmResult[] arr$ = values();

        for (ConfirmResult c$ : arr$) {
            if (c$.intValue == intValue) {
                return c$;
            }
        }

        return NEUTRAL;
    }

    public String toString() {
        return this.name;
    }
}
