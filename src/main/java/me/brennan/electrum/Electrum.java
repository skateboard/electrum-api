package me.brennan.electrum;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.*;
import me.brennan.electrum.listener.CacheRemovalListener;
import me.brennan.electrum.model.*;
import me.brennan.electrum.request.JsonBody;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Brennan
 * @since 9/15/21
 **/
public class Electrum {
    private final String rpcUser, rpcPassword;

    private final String RPC_URL;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .build();

    private final Gson GSON = new GsonBuilder().create();

    private final LoadingCache<String, PaymentRequest> storedPaymentRequest;

    public Electrum(String rpcUser, String rpcPassword, String rpcHost) {
        this.rpcUser = rpcUser;
        this.rpcPassword = rpcPassword;

        this.RPC_URL = String.format("http://%s:%s@%s:%s", rpcUser, rpcPassword, rpcHost, 7777);

        this.storedPaymentRequest = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .removalListener(new CacheRemovalListener())
                .build(new CacheLoader<>() {
                    @NotNull
                    @Override
                    public PaymentRequest load(@NotNull String address) throws IOException {
                        return getPaymentRequestFromRPC(address);
                    }
                });
    }

    public Electrum(String rpcUser, String rpcPassword, String rpcHost, int rpcPort) {
        this.rpcUser = rpcUser;
        this.rpcPassword = rpcPassword;

        this.RPC_URL = String.format("http://%s:%s@%s:%s", rpcUser, rpcPassword, rpcHost, rpcPort);

        this.storedPaymentRequest = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .removalListener(new CacheRemovalListener())
                .build(new CacheLoader<>() {
                    @NotNull
                    @Override
                    public PaymentRequest load(@NotNull String address) throws IOException {
                        return getPaymentRequestFromRPC(address);
                    }
                });
    }

    /**
     * send our basic request
     * @param method - our function
     * @param params - our params
     * @return - return JSON object response
     * @throws IOException - failed to send request
     */
    private JsonObject sendRequest(String method, Parameter... params) throws IOException {
        final JsonArray paramsArray = new JsonArray();

        for(Parameter parameter : params) {
            if (parameter.getValue() instanceof String) {
                paramsArray.add((String) parameter.getValue());
            } else if(parameter.getValue() instanceof Boolean) {
                paramsArray.add((Boolean) parameter.getValue());
            } else if(parameter.getValue() instanceof Number) {
                paramsArray.add((Number) parameter.getValue());
            }
        }

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "electrum_java");
        jsonObject.addProperty("method", method);
        jsonObject.add("params", paramsArray);

        final Request request = new Request.Builder()
                .url(RPC_URL)
                .post(new JsonBody(jsonObject))
                .header("Authorization", Credentials.basic(rpcUser, rpcPassword))
                .build();

        try(Response response = httpClient.newCall(request).execute()) {
            final int responseCode = response.code();

            switch (responseCode) {
                case 200 -> {
                    final JsonObject responseObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

                    if (responseObject.has("error")) {
                        if (!responseObject.get("error").isJsonNull())
                            throw new IOException("Received an error! " + responseObject);
                    }

                    return responseObject;
                }
                case 400 -> throw new IOException("Encountered an error! Code: 400");
                case 401 -> throw new IOException("Failed to authenticate! Code: 401");
            }
        }

        return null;
    }

    public float btc2sat(float btc) {
        return btc * 100000000;
    }

    public float sat2btc(float sat) {
        return sat / 100000000;
    }

    /**
     * Determines if a specific address is to the current wallet
     *
     * @param address - the address wanting to check
     * @return if the address is the wallets
     * @throws IOException - failed to send request
     */
    public boolean isMine(String address) throws IOException {
        return sendRequest("ismine", new Parameter(address)).get("result").getAsBoolean();
    }

    /**
     * Determines if a specific address is a valid address
     *
     * @param address 0 the address wanting to check
     * @return if the address is valid
     * @throws IOException - failed to send request
     */
    public boolean isValid(String address) throws IOException {
        return sendRequest("validateaddress", new Parameter(address)).get("result").getAsBoolean();
    }

    /**
     * Get total wallet balance
     * @param confirmedOnly - only get confirmed BTC
     * @return the total balance of BTC
     * @throws IOException - failed to send request
     */
    public Balance getBalance(boolean confirmedOnly) throws IOException {
        final JsonObject response = sendRequest("getbalance").getAsJsonObject("result");

        final Balance balance = new Balance();
        if (!confirmedOnly && response.has("unconfirmed"))
            balance.setUnconfirmed(response.get("unconfirmed").getAsString());

        if (response.has("confirmed"))
            balance.setConfirmed(response.get("confirmed").getAsString());

        return balance;
    }

    /**
     * List all current address on the wallet
     *
     * @return a JSON array of all addresses on the wallet
     * @throws IOException
     */
    public List<String> listAddresses() throws IOException {
        final List<String> addresses = new LinkedList<>();

        for (JsonElement jsonElement : sendRequest("listaddresses").get("result").getAsJsonArray()) {
            addresses.add(jsonElement.getAsString());
        }

        return addresses;
    }

    /**
     * Create a new address on the wallet
     *
     * @return the address
     * @throws IOException - failed to send request
     */
    public String createNewAddress() throws IOException {
        return sendRequest("createnewaddress").get("result").getAsString();
    }

    /**
     * Go through all transactions with provided parameters and returns a list of total transactions.
     *
     * @param minConfirms - only include transactions with a certain min confirms
     * @return - the list of transactions
     * @throws IOException - failed to send request
     */
    public List<Transaction> getHistory(int minConfirms) throws IOException {
        final List<Transaction> transactions = new LinkedList<>();
        final JsonObject response = sendRequest("onchain_history").getAsJsonObject("result");

        for (JsonElement jsonElement : response.getAsJsonArray("transactions")) {
            if (jsonElement instanceof JsonObject) {
                final JsonObject transaction = (JsonObject) jsonElement;
                if (transaction.get("confirmations").getAsInt() < minConfirms) continue;

                transactions.add(GSON.fromJson(transaction, Transaction.class));
            }
        }

        return transactions;
    }

    /**
     * Broadcast the HEX encoded transaction ID to the network.
     *
     * @param tx - the hex encoded transaction ID
     * @return - the transaction hash
     * @throws IOException - failed to send request
     */
    public String broadcast(String tx) throws IOException {
        return sendRequest("broadcast", new Parameter(tx)).getAsJsonObject("result").getAsString();
    }

    /**
     * Same as #payTo but no custom fee
     *
     * @param address - the destination address
     * @param amount - the amount you want to send
     * @return - the hex encoded transaction ID
     * @throws IOException - failed to send request
     */
    public String payTo(String address, float amount) throws IOException {
        return payTo(address, amount, 0.0F);
    }

    /**
     * Generates and signs a new transaction.
     *
     * @param address - the destination address
     * @param amount - the amount you want to send
     * @param amountFee (optional) - the fee amount
     * @return - the hex encoded transaction ID
     * @throws IOException - failed to send request
     */
    public String payTo(String address, float amount, float amountFee) throws IOException {
        if (amount <= 0) return null;
        if (amountFee >= 0.01) return null;

        Parameter[] params = new Parameter[3];
        params[0] = new Parameter(address);
        params[1] = new Parameter(amount);

        if (amountFee > 0.0) {
            params[2] = new Parameter(amountFee);
        }

        return sendRequest("payto", params).getAsJsonObject("result").get("hex").getAsString();
    }

    /**
     * the same as #payMax but no custom fee
     *
     * @param address - destination address
     * @return - the hex encoded transaction ID
     * @throws IOException - failed to send request
     */
    public String payMax(String address) throws IOException {
        return payMax(address, 0.0F);
    }

    /**
     * The same as #payTo but no amount needed as it sends all the BTC in the wallet.
     *
     * @param address - destination address
     * @param amountFee - the fee amount
     * @return - the hex encoded transaction ID
     * @throws IOException - failed to send request
     */
    public String payMax(String address, float amountFee) throws IOException {
        if (amountFee >= 0.01) return null;

        Parameter[] params = new Parameter[3];
        params[0] = new Parameter(address);
        params[1] = new Parameter("!");

        if (amountFee > 0.0) {
            params[2] = new Parameter(amountFee);
        }

        return sendRequest("payto", params).getAsJsonObject("result").get("hex").getAsString();
    }

    /**
     * Get the fee rate for a certain fee level (normally used in custom fee transactions)
     *
     * @param feeLevel - the fee level
     * @return - the fee rate to use
     * @throws IOException - failed to send request
     */
    public float getFeeRate(float feeLevel) throws IOException {
        if(feeLevel < 0.0 || feeLevel > 1.0) throw new IOException("Fee level must be between 0.0 and 1.0");

        float response = sendRequest("getfeerate").get("result").getAsFloat();

        return response / 1000;
    }

    /**
     * Request a payment really cool for
     *
     * @param amount - amount requested
     * @param memo - a memo (like a description)
     * @return - the payment request object
     * @throws IOException - the IO exception
     */
    public PaymentRequest createPaymentRequest(float amount, String memo) throws IOException {
        if (amount <= 0) return null;

        final JsonObject resultObject = memo.isEmpty() ? sendRequest("add_request", new Parameter(amount)).getAsJsonObject("result") :
                sendRequest("add_request", new Parameter(amount), new Parameter(memo)).getAsJsonObject("result");

        final PaymentRequest paymentRequest = GSON.fromJson(resultObject, PaymentRequest.class);
        storedPaymentRequest.put(paymentRequest.getAddress(), paymentRequest);

        return paymentRequest;
    }

    public PaymentRequest getPaymentRequest(String address) {
        return storedPaymentRequest.getIfPresent(address);
    }

    public PaymentRequest getPaymentRequestFromRPC(String address) throws IOException {
        final JsonObject resultObject = sendRequest("getrequest", new Parameter(address)).getAsJsonObject("result");
        return GSON.fromJson(resultObject, PaymentRequest.class);
    }
}
