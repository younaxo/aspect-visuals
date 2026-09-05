package su.aspectvisuals.client.account.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.account.AspectProfile;
import su.aspectvisuals.client.account.ClientSession;
import su.aspectvisuals.client.account.DeviceRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * HTTP-клиент аккаунта Aspect Visuals.
 *
 * Все запросы асинхронные: игровой поток не должен ждать сеть.
 * Токен сессии никогда не попадает в логи — при ошибках пишется только код.
 */
public final class AspectApiClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_RESPONSE_BYTES = 64 * 1024;

    private final Executor executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "aspect-api");
        thread.setDaemon(true);
        return thread;
    });

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .executor(executor)
            .build();

    private final String clientVersion;

    public AspectApiClient(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public CompletableFuture<DeviceRequest> requestDeviceCode() {
        JsonObject body = new JsonObject();
        body.addProperty("clientName", AspectVisuals.NAME);
        body.addProperty("clientVersion", clientVersion);

        return send(post(ApiEndpoints.deviceCode(), body.toString(), null))
                .thenApply(json -> new DeviceRequest(
                        string(json, "deviceCode"),
                        string(json, "userCode"),
                        string(json, "verificationUri"),
                        string(json, "verificationUriComplete"),
                        System.currentTimeMillis() + number(json, "expiresIn", 600) * 1000L,
                        (int) number(json, "interval", 5)));
    }

    public CompletableFuture<ClientSession> exchangeDeviceCode(String deviceCode) {
        JsonObject body = new JsonObject();
        body.addProperty("deviceCode", deviceCode);

        return send(post(ApiEndpoints.deviceToken(), body.toString(), null)).thenApply(AspectApiClient::toSession);
    }

    public CompletableFuture<AspectProfile> profile(String token) {
        HttpRequest request = HttpRequest.newBuilder(ApiEndpoints.profile())
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return send(request).thenApply(AspectApiClient::toProfile);
    }

    public CompletableFuture<ClientSession> refresh(String token) {
        return send(post(ApiEndpoints.refresh(), "{}", token)).thenApply(AspectApiClient::toSession);
    }

    public CompletableFuture<Void> logout(String token) {
        HttpRequest request = HttpRequest.newBuilder(ApiEndpoints.logout())
                .timeout(TIMEOUT)
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        return send(request).thenApply(json -> (Void) null);
    }

    /** Загрузка аватара: отдельный метод, потому что ответ бинарный и с лимитом. */
    public CompletableFuture<byte[]> download(URI uri, int maxBytes) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .GET()
                .build();

        return http.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .handle((response, error) -> {
                    if (error != null) {
                        throw new ApiException(ApiError.NETWORK);
                    }
                    if (response.statusCode() != 200) {
                        throw new ApiException(ApiError.SERVER);
                    }
                    byte[] bytes = response.body();
                    if (bytes.length > maxBytes) {
                        throw new ApiException(ApiError.MALFORMED);
                    }
                    return bytes;
                });
    }

    private HttpRequest post(URI uri, String body, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder.build();
    }

    private CompletableFuture<JsonObject> send(HttpRequest request) {
        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, error) -> {
                    if (error != null) {
                        throw new ApiException(ApiError.NETWORK);
                    }
                    return parse(response);
                });
    }

    private JsonObject parse(HttpResponse<String> response) {
        String body = response.body() == null ? "" : response.body();
        if (body.length() > MAX_RESPONSE_BYTES) {
            throw new ApiException(ApiError.MALFORMED);
        }

        JsonObject json;
        try {
            JsonElement element = JsonParser.parseString(body.isEmpty() ? "{}" : body);
            json = element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
        } catch (JsonSyntaxException error) {
            throw new ApiException(ApiError.MALFORMED);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return json;
        }

        String code = json.has("error") && json.get("error").isJsonPrimitive() ? json.get("error").getAsString() : null;
        // 202 отдаётся, пока пользователь не подтвердил вход, — это не ошибка сети
        ApiError error = status == 401 ? ApiError.UNAUTHORIZED : ApiError.fromCode(code);
        if (status == 401 && "session_expired".equals(code)) {
            error = ApiError.SESSION_EXPIRED;
        }
        throw new ApiException(error);
    }

    private static ClientSession toSession(JsonObject json) {
        String token = string(json, "sessionToken");
        if (token.isEmpty()) {
            throw new ApiException(ApiError.MALFORMED);
        }

        JsonObject session = json.has("session") && json.get("session").isJsonObject()
                ? json.getAsJsonObject("session")
                : new JsonObject();

        return new ClientSession(token, string(session, "id"), instant(string(json, "expiresAt")));
    }

    private static AspectProfile toProfile(JsonObject json) {
        String username = string(json, "username");
        if (username.isEmpty()) {
            throw new ApiException(ApiError.MALFORMED);
        }

        List<String> roles = new ArrayList<>();
        if (json.has("roles") && json.get("roles").isJsonArray()) {
            json.getAsJsonArray("roles").forEach(element -> {
                if (element.isJsonPrimitive()) {
                    roles.add(element.getAsString());
                }
            });
        }

        AspectProfile.Subscription subscription = null;
        if (json.has("subscription") && json.get("subscription").isJsonObject()) {
            JsonObject sub = json.getAsJsonObject("subscription");
            Integer daysLeft = sub.has("daysLeft") && sub.get("daysLeft").isJsonPrimitive()
                    ? sub.get("daysLeft").getAsInt()
                    : null;
            subscription = new AspectProfile.Subscription(
                    string(sub, "name"),
                    sub.has("permanent") && sub.get("permanent").getAsBoolean(),
                    daysLeft,
                    string(sub, "purchasedAt"),
                    instant(string(sub, "expiresAt")));
        }

        Integer uid = json.has("uid") && json.get("uid").isJsonPrimitive() ? json.get("uid").getAsInt() : null;
        String displayName = string(json, "displayName");

        return new AspectProfile(
                string(json, "id"),
                username,
                displayName.isEmpty() ? username : displayName,
                uid,
                string(json, "avatar"),
                List.copyOf(roles),
                subscription,
                System.currentTimeMillis());
    }

    private static String string(JsonObject json, String key) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsString();
        }
        return "";
    }

    private static long number(JsonObject json, String key, long fallback) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsLong();
        }
        return fallback;
    }

    private static long instant(String value) {
        if (value == null || value.isEmpty()) {
            return 0L;
        }
        try {
            return Instant.parse(value).toEpochMilli();
        } catch (DateTimeParseException error) {
            return 0L;
        }
    }
}
