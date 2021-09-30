package me.brennan.electrum.model;

/**
 * @author Brennan
 * @since 9/15/21
 **/
public class Parameter {
    private Object value;

    public Parameter(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
