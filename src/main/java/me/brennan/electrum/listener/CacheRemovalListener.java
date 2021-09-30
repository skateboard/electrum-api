package me.brennan.electrum.listener;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import me.brennan.electrum.model.PaymentRequest;

/**
 * @author Brennan
 * @since 9/29/2021
 **/
public class CacheRemovalListener implements RemovalListener<String, PaymentRequest> {

    @Override
    public void onRemoval(RemovalNotification<String, PaymentRequest> notification) {

    }
}
