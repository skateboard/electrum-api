package me.brennan.electrum.model;

/**
 * @author Brennan
 * @since 9/15/21
 **/
public class Parameter {
    private final String key, value;

    public Parameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
