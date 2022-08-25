package rnfive.htfu.cyclingcomputer.define.enums;

import androidx.annotation.NonNull;

public enum Action {
    START("START"),
    STOP("STOP");

    private final String stringVal;

    Action(String stringVal) {
        this.stringVal = stringVal;
    }

    @NonNull
    @Override
    public String toString() {
        return stringVal;
    }
}
