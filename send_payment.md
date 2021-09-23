# Sending Payment Example

## Sending all funds to certain address

```java
package me.brennan.electrum.example;

import me.brennan.electrum.Electrum;
import java.io.IOException;

public class SendPayment {

    public static void main(String[] args) throws IOException {
        final Electrum electrum = new Electrum("admin", "admin123", "localhost");
        
        final String tx = electrum.payMax("ADDRESS");
        final String txID = electrum.broadcast(tx);

        System.out.println("https://blockchair.com/bitcoin/transaction/" + txID);
    }

}
```

## Sending funds to certain address with custom fee

```java
package me.brennan.electrum.example;

import me.brennan.electrum.Electrum;
import java.io.IOException;

public class SendPayment {

    public static void main(String[] args) throws IOException {
        final Electrum electrum = new Electrum("admin", "admin123", "localhost");

        final float feeRate = electrum.getFeeRate(0.3f);
        final String txTmp = electrum.payTo("ADDRESS", 0.2f);
        final float fee = electrum.sat2btc(feeRate * txTmp.length() / 2);
        
        final String tx = electrum.payTo("ADDRESS", 0.2, fee);
        final String txID = electrum.broadcast(tx);

        System.out.println("https://blockchair.com/bitcoin/transaction/" + txID);
    }

}
```