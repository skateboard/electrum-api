package me.brennan.electrum.model;

/**
 * @author Brennan
 * @since 9/21/21
 **/
public class Transaction {
    private final String address;
    private final float value;

    public Transaction(String address, float value) {
        this.address = address;
        this.value = value;
    }

    public String getAddress() {
        return address;
    }

    public float getValue() {
        return value;
    }
}
