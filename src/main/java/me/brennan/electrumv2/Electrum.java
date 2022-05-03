package me.brennan.electrumv2;

import com.google.gson.*;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import me.brennan.electrumv2.model.PaymentRequest;
import me.brennan.electrumv2.records.Parameter;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Brennan / skateboard
 * @since 5/3/2022
 **/
public class Electrum {
    private final String rpcUrl, rpcUser, rpcPassword;
    private final Vertx vertx;

    private final HttpClient client;

    private final Gson GSON = new GsonBuilder().create();

    public Electrum(String rpcUser, String rpcPassword, String rpcHost) {
        this(rpcUser, rpcPassword, rpcHost, 7777);
    }

    public Electrum(String rpcUser, String rpcPassword, String rpcHost, int rpcPort) {
        this.rpcUser = rpcUser;
        this.rpcPassword = rpcPassword;

        this.rpcUrl = String.format("http://%s:%s@%s:%s", rpcUser, rpcPassword, rpcHost, rpcPort);

        this.vertx = Vertx.vertx();

        this.client = this.vertx.createHttpClient(new HttpClientOptions().
                setProtocolVersion(HttpVersion.HTTP_2).
                setSsl(true).
                setUseAlpn(true).
                setTrustAll(true));
    }

    /**
     * Creates a Payment Request and waits for the future to complete.
     *
     * @param amount - The amount of the payment request
     * @param memo - The memo of the payment request
     * @return - the payment request
     * @throws Exception - if the request fails.
     */
    public PaymentRequest createPaymentRequest(float amount, String memo) throws Exception {
        final var promise = createPaymentRequestAsync(amount, memo);

        return promise.get();
    }

    /**
     * Gets a Payment Request and waits for the future to complete.
     *
     * @param address - The address of the payment request.
     * @return - the payment request.
     * @throws Exception - if the request fails.
     */
    public PaymentRequest getPaymentRequest(String address) throws Exception {
        final var promise = getPaymentRequestAsync(address);

        return promise.get();
    }

    /**
     * Gets a Payment Request.
     *
     * @param address - The address of the payment request.
     * @return - a future containing the payment request result.
     * @throws Exception - if the request fails.
     */
    private CompletableFuture<PaymentRequest> getPaymentRequestAsync(String address) throws Exception {
        final var resultObject = sendRequest("getrequest", new Parameter(address)).get().getAsJsonObject("result");
        final var paymentRequest = GSON.fromJson(resultObject, PaymentRequest.class);

        return CompletableFuture.completedFuture(paymentRequest);
    }

    /**
     * Sends a Payment Request.
     *
     * @param amount - The amount of the payment request
     * @param memo - The memo of the payment request
     * @return - a future containing the payment request result.
     * @throws Exception - if the request fails.
     */
    private CompletableFuture<PaymentRequest> createPaymentRequestAsync(float amount, String memo) throws Exception {
        if (amount <= 0) return CompletableFuture.completedFuture(null);

        final var resultObject = memo.isEmpty() ? sendRequest("add_request", new Parameter(amount)).get().getAsJsonObject("result") :
                sendRequest("add_request", new Parameter(amount), new Parameter(memo)).get().getAsJsonObject("result");

        final var paymentRequest = GSON.fromJson(resultObject, PaymentRequest.class);
        return CompletableFuture.completedFuture(paymentRequest);
    }

    /**
     * Sends a request to the Electrum server using the specified method and parameters.
     *
     * @param method - The method to call on the Electrum server.
     * @param parameters - The parameters to pass to the Electrum server.
     * @return - A future containing the JsonObject result of the request.
     *
     * @throws ExecutionException - If the request failed to complete.
     * @throws InterruptedException - If the request was interrupted.
     * @throws TimeoutException - If the request timed out.
     */
    private CompletableFuture<JsonObject> sendRequest(String method, Parameter... parameters) throws ExecutionException, InterruptedException, TimeoutException {
        final JsonArray paramsArray = new JsonArray();

        for(Parameter parameter : parameters) {
            if (parameter.value() instanceof String) {
                paramsArray.add((String) parameter.value());
            } else if(parameter.value() instanceof Boolean) {
                paramsArray.add((Boolean) parameter.value());
            } else if(parameter.value() instanceof Number) {
                paramsArray.add((Number) parameter.value());
            }
        }

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "electrum_java");
        jsonObject.addProperty("method", method);
        jsonObject.add("params", paramsArray);

        final var requestPromise = post(rpcUrl, jsonObject.toString());
        final var response = requestPromise.get(10, TimeUnit.SECONDS);
        if (response == null) {
            throw new TimeoutException("Request timed out");
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Request failed with status code " + response.statusCode());
        }

        final var body = response.body().result().toString();
        final var responseObject = JsonParser.parseString(body).getAsJsonObject();

        if (responseObject.has("error")) {
            if (!responseObject.get("error").isJsonNull())
                throw new RuntimeException("Received an error! " + responseObject);
        }

        return CompletableFuture.completedFuture(responseObject);

    }

    /**
     * Sends a post request to the specified url and returns the response.
     *
     * @param url - The URL of the post request
     * @param body - The body of the post request
     * @return - A future that will contain the response of the post request
     */
    private CompletableFuture<HttpClientResponse> post(String url, String body) {
        try {
            final var requestPromise = new CompletableFuture<HttpClientResponse>();
            final var request = client.request(HttpMethod.POST, url)
                    .result()
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .putHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((rpcUser + ":" + rpcPassword).getBytes()));
            request.write(body);

            request.send(res -> {
                if (res.succeeded()) requestPromise.complete(res.result());
            });

            request.end();
            return requestPromise;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }
}
