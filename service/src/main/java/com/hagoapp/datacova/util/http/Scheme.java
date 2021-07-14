package com.hagoapp.datacova.util.http;

import java.util.Arrays;

/**
 * Enumeration of http schemes.
 *
 * @author suncj2
 * @since 2.0
 */
public enum Scheme {
    HTTP("http"),
    HTTPS("https");

    private String value;

    Scheme(String protocolName) {
        value = protocolName.toLowerCase();
        if (Arrays.stream(Scheme.values()).noneMatch(protocol -> protocol.toString().equals(value))) {
            value = "https";
        }
    }

    public String getValue() {
        return value;
    }
}
