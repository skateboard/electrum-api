package me.brennan.electrum.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @Expose
    private final Map<String, String> metaData = new LinkedHashMap<>();

    public void addMetadata(String key, String value) {
        metaData.put(key, value);
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

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
