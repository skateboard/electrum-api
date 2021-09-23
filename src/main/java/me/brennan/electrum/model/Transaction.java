package me.brennan.electrum.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Brennan
 * @since 9/21/21
 *
 * Our transaction class used in Electrum#getHistory()
 **/
public class Transaction {

    @SerializedName("balance")
    private String balance;

    @SerializedName("txid")
    private String txID;

    @SerializedName("incoming")
    private boolean incoming;

    public String getBalance() {
        return balance;
    }

    public String getTxID() {
        return txID;
    }

    public boolean isIncoming() {
        return incoming;
    }
}
