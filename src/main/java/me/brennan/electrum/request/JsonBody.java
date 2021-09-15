package me.brennan.electrum.request;

import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Brennan
 * @since 8/21/21
 *
 * made for easy-of json bodys for request :)
 **/
public class JsonBody extends RequestBody {
    private final String jsonObject;

    public JsonBody(String jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JsonBody(JsonObject jsonObject) {
        this.jsonObject = jsonObject.toString();
    }

    @Override
    public long contentLength() throws IOException {
        return jsonObject.length();
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse("application/json; charset=utf-8");
    }

    @Override
    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
        final byte[] bytes = jsonObject.getBytes(StandardCharsets.UTF_8);
        bufferedSink.write(bytes, 0, bytes.length);
    }
}