package su.aspectvisuals.client.account;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.account.api.ApiError;
import su.aspectvisuals.client.account.api.ApiException;
import su.aspectvisuals.client.account.api.AspectApiClient;
import su.aspectvisuals.client.config.JsonStore;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Состояние аккаунта Aspect Visuals внутри клиента.
 *
 * Пароль от сайта здесь не появляется никогда: вход идёт одноразовым кодом,
 * который пользователь подтверждает в браузере. На диск ложится только
 * токен сессии — его можно отозвать с сайта, и тогда следующий запрос
 * профиля вернёт 401, а клиент честно выйдет из аккаунта.
 */
public final class AccountManager {
    public enum State {
        SIGNED_OUT,
        LINKING,
        SIGNED_IN
    }

    private static final long PROFILE_TTL_MS = 5 * 60 * 1000L;
    private static final long RETRY_AFTER_FAILURE_MS = 60 * 1000L;
    private static final String SESSION_FILE = "session.json";

    private final AspectApiClient api;
    private final AvatarCache avatars = new AvatarCache();
    private final Path sessionPath;

    private State state = State.SIGNED_OUT;
    private ClientSession session;
    private AspectProfile profile;
    private DeviceRequest pending;
    private String statusMessage = "";

    private long nextPollAt;
    private long nextProfileAt;
    private long nextRefreshAt;
    private boolean requestInFlight;

    public AccountManager(String clientVersion) {
        this.api = new AspectApiClient(clientVersion);
        this.sessionPath = JsonStore.directory().resolve(SESSION_FILE);
    }

    public State state() {
        return state;
    }

    public AspectProfile profile() {
        return profile;
    }

    public DeviceRequest pending() {
        return pending;
    }

    public String statusMessage() {
        return statusMessage;
    }

    public AvatarCache avatars() {
        return avatars;
    }

    public boolean signedIn() {
        return state == State.SIGNED_IN;
    }

    public void load() {
        JsonObject json = JsonStore.read(sessionPath);
        if (json == null) {
            return;
        }

        String token = JsonStore.string(json, "token");
        if (token.isEmpty()) {
            return;
        }

        session = new ClientSession(token, JsonStore.string(json, "sessionId"), JsonStore.longValue(json, "expiresAt"));
        if (session.expired()) {
            AspectVisuals.LOGGER.info("Сохранённая сессия истекла");
            clearSession("Сессия истекла");
            return;
        }

        state = State.SIGNED_IN;
        nextProfileAt = 0L;
    }

    private void saveSession() {
        if (session == null) {
            JsonStore.delete(sessionPath);
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("token", session.token());
        json.addProperty("sessionId", session.sessionId());
        json.addProperty("expiresAt", session.expiresAt());
        // Токен сессии не копируется: общесистемная папка читается всеми
        // учётными записями машины, и копия сделала бы вход доступным им
        JsonStore.write(sessionPath, json, false);
    }

    /** Шаг входа: запрашиваем одноразовый код и открываем страницу подтверждения. */
    public void beginLogin() {
        if (state == State.LINKING || requestInFlight) {
            return;
        }

        requestInFlight = true;
        statusMessage = "Запрашиваем код…";

        api.requestDeviceCode().whenComplete((request, error) -> MinecraftClient.getInstance().execute(() -> {
            requestInFlight = false;
            if (error != null) {
                statusMessage = describe(error);
                return;
            }

            pending = request;
            state = State.LINKING;
            statusMessage = "Подтвердите вход на сайте";
            nextPollAt = System.currentTimeMillis() + request.intervalSeconds() * 1000L;
            openVerificationPage();
        }));
    }

    public void openVerificationPage() {
        if (pending != null) {
            Util.getOperatingSystem().open(URI.create(pending.verificationUriComplete()));
        }
    }

    public void cancelLogin() {
        pending = null;
        state = session != null ? State.SIGNED_IN : State.SIGNED_OUT;
        statusMessage = "";
    }

    public void logout() {
        ClientSession current = session;
        clearSession("");

        if (current != null) {
            // Сессию гасим и на сервере, но выход в клиенте не должен ждать сеть
            api.logout(current.token()).exceptionally(error -> null);
        }
    }

    private void clearSession(String message) {
        session = null;
        profile = null;
        pending = null;
        state = State.SIGNED_OUT;
        statusMessage = message;
        avatars.clear();
        saveSession();
    }

    /** Принудительное обновление профиля: кнопка в интерфейсе и открытие меню. */
    public void refreshProfile() {
        nextProfileAt = 0L;
    }

    public void tick() {
        long now = System.currentTimeMillis();

        if (state == State.LINKING) {
            pollDeviceCode(now);
            return;
        }

        if (state != State.SIGNED_IN || session == null || requestInFlight) {
            return;
        }

        if (session.expired()) {
            clearSession("Сессия истекла");
            return;
        }

        if (now >= nextProfileAt) {
            fetchProfile();
        } else if (session.shouldRefresh() && now >= nextRefreshAt) {
            refreshSession();
        }
    }

    private void pollDeviceCode(long now) {
        if (pending == null) {
            cancelLogin();
            return;
        }

        if (now > pending.expiresAt()) {
            pending = null;
            state = session != null ? State.SIGNED_IN : State.SIGNED_OUT;
            statusMessage = ApiError.EXPIRED_CODE.message();
            return;
        }

        if (requestInFlight || now < nextPollAt) {
            return;
        }

        requestInFlight = true;
        int interval = pending.intervalSeconds();

        api.exchangeDeviceCode(pending.deviceCode()).whenComplete((granted, error) ->
                MinecraftClient.getInstance().execute(() -> {
                    requestInFlight = false;

                    if (error == null) {
                        session = granted;
                        profile = null;
                        pending = null;
                        state = State.SIGNED_IN;
                        statusMessage = "";
                        nextProfileAt = 0L;
                        saveSession();
                        AspectVisuals.LOGGER.info("Вход в аккаунт Aspect Visuals выполнен");
                        return;
                    }

                    ApiError reason = reason(error);
                    switch (reason) {
                        case AUTHORIZATION_PENDING -> nextPollAt = System.currentTimeMillis() + interval * 1000L;
                        // Сервер просит сбавить темп — увеличиваем паузу, а не долбим чаще
                        case SLOW_DOWN -> nextPollAt = System.currentTimeMillis() + interval * 2000L;
                        case NETWORK -> {
                            statusMessage = reason.message();
                            nextPollAt = System.currentTimeMillis() + interval * 2000L;
                        }
                        default -> {
                            statusMessage = reason.message();
                            pending = null;
                            state = session != null ? State.SIGNED_IN : State.SIGNED_OUT;
                        }
                    }
                }));
    }

    private void fetchProfile() {
        requestInFlight = true;

        api.profile(session.token()).whenComplete((loaded, error) -> MinecraftClient.getInstance().execute(() -> {
            requestInFlight = false;

            if (error == null) {
                profile = loaded;
                statusMessage = "";
                nextProfileAt = System.currentTimeMillis() + PROFILE_TTL_MS;
                avatars.load(api, loaded.avatarUrl());
                return;
            }

            ApiError reason = reason(error);
            if (reason == ApiError.UNAUTHORIZED || reason == ApiError.SESSION_EXPIRED) {
                AspectVisuals.LOGGER.info("Сессия клиента больше не действительна: {}", reason.message());
                clearSession(reason.message());
                return;
            }

            // Сеть или сервер: остаёмся в аккаунте и показываем прошлый профиль
            statusMessage = reason.message();
            nextProfileAt = System.currentTimeMillis() + RETRY_AFTER_FAILURE_MS;
        }));
    }

    private void refreshSession() {
        requestInFlight = true;
        // Неудачное продление не должно повторяться каждый тик
        nextRefreshAt = System.currentTimeMillis() + RETRY_AFTER_FAILURE_MS;

        api.refresh(session.token()).whenComplete((refreshed, error) -> MinecraftClient.getInstance().execute(() -> {
            requestInFlight = false;

            if (error == null) {
                session = refreshed;
                saveSession();
                return;
            }

            ApiError reason = reason(error);
            if (reason == ApiError.UNAUTHORIZED || reason == ApiError.SESSION_EXPIRED) {
                clearSession(reason.message());
            }
        }));
    }

    private static ApiError reason(Throwable error) {
        Throwable cause = error instanceof java.util.concurrent.CompletionException ? error.getCause() : error;
        if (cause instanceof ApiException api) {
            return api.error();
        }
        return ApiError.NETWORK;
    }

    private static String describe(Throwable error) {
        return reason(error).message();
    }

    /** Профиль устарел — используется интерфейсом, чтобы обновиться при открытии. */
    public CompletableFuture<Void> ensureFresh() {
        if (state == State.SIGNED_IN && System.currentTimeMillis() >= nextProfileAt) {
            refreshProfile();
        }
        return CompletableFuture.completedFuture(null);
    }
}
