import me.brennan.electrum.Electrum;
import me.brennan.electrum.model.PaymentRequest;
import me.brennan.electrum.model.Transaction;

import java.io.IOException;

/**
 * @author Brennan
 * @since 9/15/21
 **/
public class ElectrumTest {

    public static void main(String[] args) throws IOException {
        final Electrum electrum = new Electrum("admin", "admin123", "localhost");

        final String address = electrum.createNewAddress();

        System.out.println(address);
        System.out.println(electrum.isValid(address));
        System.out.println(electrum.isMine(address));

        System.out.println(electrum.getBalance(true));
        System.out.println(electrum.getBalance(false));

        final PaymentRequest paymentRequest = electrum.createPaymentRequest(0.00011f, "test");
        System.out.println(paymentRequest.getAddress());

        final PaymentRequest paymentRequest1 = electrum.getPaymentRequest(paymentRequest.getAddress());
        System.out.println(paymentRequest1.getAddress());
//
        for (Transaction transaction : electrum.getHistory(1)) {
            System.out.println(transaction.getTxID() + " - " + transaction.getBalance() + " - " + transaction.isIncoming());
        }


        final String tx = electrum.payTo("ADDRESS", 0.1F);
        final String txID = electrum.broadcast(tx);
        System.out.println("https://blockchair.com/bitcoin/transaction/" + txID);

        final String maxTX = electrum.payMax("ADDRESS");
        final String maxTXID = electrum.broadcast(maxTX);
        System.out.println("https://blockchair.com/bitcoin/transaction/" + maxTXID);

        sendBTCWithCustomFee(electrum);

    }

    private static void sendBTCWithCustomFee(Electrum electrum) throws IOException {
        final float feeRate = electrum.getFeeRate(0.3F);
        final String txTmp = electrum.payTo("ADDRESS", 0.1F);
        final float fee = electrum.sat2btc(feeRate * txTmp.length() / 2);

        final String tx = electrum.payTo("ADDRESS", 0.1F, fee);
        final String txID = electrum.broadcast(tx);

        System.out.println("https://blockchair.com/bitcoin/transaction/" + txID);
    }
}
