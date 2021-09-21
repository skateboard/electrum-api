package me.brennan.electrum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brennan.electrum.model.Height;
import me.brennan.electrum.model.Parameter;
import me.brennan.electrum.model.Transaction;
import me.brennan.electrum.request.JsonBody;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Brennan
 * @since 9/15/21
 **/
public class Electrum {
    private final String rpcUser, rpcPassword, rpcHost;
    private int rpcPort = 7777;

    private final String RPC_URL;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .build();

    public Electrum(String rpcUser, String rpcPassword, String rpcHost) {
        this.rpcUser = rpcUser;
        this.rpcPassword = rpcPassword;
        this.rpcHost = rpcHost;

        this.RPC_URL = String.format("http://%s:%s@%s", rpcUser, rpcPassword, rpcHost);
    }

    public Electrum(String rpcUser, String rpcPassword, String rpcHost, int rpcPort) {
        this.rpcUser = rpcUser;
        this.rpcPassword = rpcPassword;
        this.rpcHost = rpcHost;
        this.rpcPort = rpcPort;

        this.RPC_URL = String.format("http://%s:%s@%s:%s", rpcUser, rpcPassword, rpcHost, rpcPort);
    }

    private JsonObject sendRequest(String method, Parameter... params) throws IOException {
        final JsonArray paramsArray = new JsonArray();

        for(Parameter parameter : params) {
            final JsonObject parameterObject = new JsonObject();

            if (parameter.getValue() instanceof String) {
                parameterObject.addProperty(parameter.getKey(), (String) parameter.getValue());

            } else if(parameter.getValue() instanceof Boolean) {
                parameterObject.addProperty(parameter.getKey(), (Boolean) parameter.getValue());

            } else if(parameter.getValue() instanceof Number) {
                parameterObject.addProperty(parameter.getKey(), (Number) parameter.getValue());
            }
            paramsArray.add(parameterObject);
        }

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "curltext");
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
                    return JsonParser.parseString(response.body().string()).getAsJsonObject();
                }
                case 400 -> System.out.println("Failed to gather");
                case 401 -> System.out.println("Failed to authenticate");
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
        return sendRequest("ismine", new Parameter("address", address)).get("result").getAsBoolean();
    }

    /**
     * Determines if a specific address is a valid address
     *
     * @param address 0 the address wanting to check
     * @return if the address is valid
     * @throws IOException - failed to send request
     */
    public boolean isValid(String address) throws IOException {
        return sendRequest("validateaddress", new Parameter("address", address)).get("result").getAsBoolean();
    }

    /**
     * Get total wallet balance
     * @param confirmedOnly - only get confirmed BTC
     * @return the total balance of BTC
     * @throws IOException - failed to send request
     */
    public float getBalance(boolean confirmedOnly) throws IOException {
        final JsonObject response = sendRequest("getbalance").getAsJsonObject("result");
        float total = 0.0F;

        if (!confirmedOnly && response.has("unconfirmed")) {
            total += response.get("unconfirmed").getAsFloat();
        }

        if (response.has("confirmed")) {
            total += response.get("confirmed").getAsFloat();
        }

        return total;
    }

    /**
     * List all current address on the wallet
     *
     * @return a JSON array of all addresses on the wallet
     * @throws IOException
     */
    public JsonArray listAddresses() throws IOException {
        return sendRequest("listaddresses").get("result").getAsJsonArray();
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
     * @param fromHeight - only include transactions from a certain height
     * @param height - the last height
     * @return - the list of transactions
     * @throws IOException - failed to send request
     */
    public List<Transaction> getHistory(int minConfirms, int fromHeight, Height height) throws IOException {
        final List<Transaction> transactions = new LinkedList<>();
        final JsonObject response = sendRequest("history", new Parameter("show_addresses", true),
                new Parameter("show_fiat", true), new Parameter("show_fees", true), new Parameter("from_height", fromHeight)).getAsJsonObject("result");

        for (JsonElement jsonElement : response.getAsJsonArray("transactions")) {
            if (jsonElement instanceof JsonObject) {
                final JsonObject transaction = (JsonObject) jsonElement;

                if (!transaction.get("incoming").getAsBoolean() || transaction.get("height").getAsInt() == 0) continue;
                if (transaction.get("confirmations").getAsInt() < minConfirms) continue;

                for (JsonElement jsonElement1 : transaction.getAsJsonArray("outputs")) {
                    if (jsonElement1 instanceof JsonObject) {
                        final JsonObject output = (JsonObject) jsonElement1;

                        if (isMine(output.get("address").getAsString()))
                            transactions.add(new Transaction(output.get("address").getAsString(), output.get("value").getAsFloat()));
                    }
                }

                height.setLastHeight(transaction.get("height").getAsInt());
            }
        }


        return transactions;
    }

}
