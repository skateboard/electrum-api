package me.brennan.electrum.model;

/**
 * @author Brennan
 * @since 9/22/2021
 **/
public class Balance {
    private String confirmed, unconfirmed;

    public Balance() {
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }

    public void setUnconfirmed(String unconfirmed) {
        this.unconfirmed = unconfirmed;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public String getUnconfirmed() {
        return unconfirmed;
    }

    @Override
    public String toString() {
        return "Balance{" +
                "confirmed='" + confirmed + '\'' +
                ", unconfirmed='" + unconfirmed + '\'' +
                '}';
    }
}
