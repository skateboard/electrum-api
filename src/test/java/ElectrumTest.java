import me.brennan.electrum.Electrum;
import me.brennan.electrum.model.Parameter;

import java.io.IOException;

/**
 * @author Brennan
 * @since 9/15/21
 **/
public class ElectrumTest {

    public static void main(String[] args) throws IOException {
        final Electrum electrum = new Electrum("admin", "admin123", "localhost");

        final String address = electrum.createNewAddress();

        System.out.println(electrum.isValid(address));
        System.out.println(electrum.isMine(address));
        System.out.println(electrum.getAddressBalance(address));
    }
}
