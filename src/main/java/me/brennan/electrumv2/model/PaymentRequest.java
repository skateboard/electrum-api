package me.brennan.electrumv2.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Brennan / skateboard
 * @since 5/3/2022
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

    public PaymentStatus getStatus() {
        return PaymentStatus.fromInt(status);
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

    public String getAmountBTC() {
        return amountBTC;
    }

    public long getExpiration() {
        return expiration;
    }

    public enum PaymentStatus {
        UNKNOWN(-1),
        CREATED(0),
        UNCONFIRMED(7),
        PAID(3),
        EXPIRED(1);

        private final int status;

        PaymentStatus(int status) {
            this.status = status;
        }

        public static PaymentStatus fromInt(int status) {
            for (PaymentStatus paymentStatus : PaymentStatus.values()) {
                if (paymentStatus.getStatus() == status) {
                    return paymentStatus;
                }
            }

            return UNKNOWN;
        }

        public int getStatus() {
            return status;
        }
    }
}
