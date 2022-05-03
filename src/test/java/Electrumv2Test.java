import me.brennan.electrumv2.Electrum;
import me.brennan.electrumv2.model.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Brennan / skateboard
 * @since 5/3/2022
 **/
public class Electrumv2Test {

    @Test
    public void testElectrum() throws Exception {
        final Electrum electrum = new Electrum("admin", "admin123", "localhost");

        final var paymentRequest = electrum.createPaymentRequest(0.01F, "cool new payment");
        System.out.println("PAYMENT ADDRESS => " + paymentRequest.getAddress());

        final var paid = new AtomicBoolean(false);

        while (!paid.get()) {
            final var payment = electrum.getPaymentRequest(paymentRequest.getAddress());

            switch (payment.getStatus()) {
                case UNKNOWN -> System.out.println("Unknown Payment Status");
                case CREATED -> System.out.println("Created Payment Status");
                case UNCONFIRMED -> System.out.println("Unconfirmed Payment Status");
                case PAID -> paid.set(true);
                case EXPIRED -> System.out.println("Expired Payment Status");
            }

            TimeUnit.SECONDS.sleep(5);
        }
    }
}
