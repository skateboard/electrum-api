package me.brennan.electrum.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Brennan
 * @since 9/22/2021
 **/
public class PaymentRequest {

    @SerializedName("address")
    private String address;

    @SerializedName("URI")
    private String bitcoinURL;

    @SerializedName("status_str")
    private String statusString;

    @SerializedName("status")
    private int status;

    @SerializedName("amount_BTC")
    private String amountBTC;

    @SerializedName("expiration")
    private long expiration;

    public String getAddress() {
        return address;
    }

    public String getBitcoinURL() {
        return bitcoinURL;
    }

    public String getStatusString() {
        return statusString;
    }

    public int getStatus() {
        return status;
    }

    public String getAmountBTC() {
        return amountBTC;
    }

    public long getExpiration() {
        return expiration;
    }
}
