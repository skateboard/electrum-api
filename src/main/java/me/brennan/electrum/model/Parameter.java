package me.brennan.electrum.model;

/**
 * @author Brennan
 * @since 9/15/21
 **/
public class Parameter {
    private final String key;
    private final Object value;

    public Parameter(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
