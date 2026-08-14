package org.acme;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Urgency {
    low,
    medium,
    high;

    @JsonCreator
    public static Urgency fromString(String value) {
        return valueOf(value.toLowerCase());
    }
}
