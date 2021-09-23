# Payment Example

## Creating new PaymentRequest.
Simply use ``Electrum#createPaymentRequest`` function to create a new payment request and save it to the database. You also will want to store the userID or customerID for later processing. A new address will be generated every new payment request.

```java
package me.brennan.electrum.example;

import me.brennan.electrum.Electrum;
import java.io.IOException;

public class PaymentCreator {

    public static void main(String[] args) throws IOException {
        final Electrum electrum = new Electrum("admin", "admin123", "localhost");
        
        final Product product = db.load_product("pid");
        
        final PaymentRequest paymentRequest = electrum.createPaymentRequest(product.getPrice(), product.getName());
        
        db_save(userID, paymentRequest);
    }

}
```

## Processing payments
I would recommend ether calling this using a JS script every so often on the webpage or a task. All this does is called ``Electrum#getPaymentRequest`` it will return a ``PaymentRequest`` object. You can then check the status of the payment request, the status codes are
```text
0 - Just created no BTC sent at all
7 - Unconfirmed transaction
3 - Paid, 1 confirmation
```
If the status equals the paid status code, you can then give your user the thing purchased and then save it.

```java
package me.brennan.electrum.example;

public class PaymentProcessor {

    public static void main(String[] args) throws IOException {
        final Electrum electrum = new Electrum("admin", "admin123", "localhost");
        
        final PaymentRequest paymentRequest = electrum.getPaymentRequest(db_where("payment_address"));
        
        switch (paymentRequest.getStatus()) {
            case 0: //no btc sent at all
                db_save("payment_request", paymentRequest.getStatus());
                db_save("status_str", "Waiting...");
                break;
            case 7: //unconfirmed
                db_save("payment_request", paymentRequest.getStatus());
                db_save("status_str", "Unconfirmed");
                
                break;
            case 3: // Paid
                give_product(user_id);
                db_save("amount_BTC", paymentRequest.getAmountBTC());
                db_save("completed", paymentRequest.getAmountBTC());
                break;
        }
    }
    
}
```