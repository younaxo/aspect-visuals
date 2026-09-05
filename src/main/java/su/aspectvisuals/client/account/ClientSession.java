package su.aspectvisuals.client.account;

/** Токен игровой сессии. Хранится отдельно от косметических настроек клиента. */
public record ClientSession(String token, String sessionId, long expiresAt) {

    public boolean expired() {
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt;
    }

    /** Продлевать сессию имеет смысл заранее, а не в момент истечения. */
    public boolean shouldRefresh() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt - 3L * 24 * 60 * 60 * 1000;
    }
}
