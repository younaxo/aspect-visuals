package su.aspectvisuals.client.account;

/** Ответ бэкенда на запрос одноразового кода. deviceCode на экран не выводится. */
public record DeviceRequest(
        String deviceCode,
        String userCode,
        String verificationUri,
        String verificationUriComplete,
        long expiresAt,
        int intervalSeconds) {
}
