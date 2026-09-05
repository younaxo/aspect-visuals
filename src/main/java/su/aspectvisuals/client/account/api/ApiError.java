package su.aspectvisuals.client.account.api;

/** Ошибки, которые клиент обязан различать: от них зависит поведение интерфейса. */
public enum ApiError {
    NETWORK("Сайт недоступен"),
    UNAUTHORIZED("Нужен повторный вход"),
    SESSION_EXPIRED("Сессия истекла"),
    AUTHORIZATION_PENDING("Ожидается подтверждение"),
    SLOW_DOWN("Запросы слишком частые"),
    ACCESS_DENIED("Вход отклонён"),
    EXPIRED_CODE("Срок действия кода истёк"),
    MALFORMED("Некорректный ответ сервера"),
    SERVER("Ошибка на сервере");

    private final String message;

    ApiError(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

    public static ApiError fromCode(String code) {
        if (code == null) {
            return SERVER;
        }
        return switch (code) {
            case "authorization_pending" -> AUTHORIZATION_PENDING;
            case "slow_down" -> SLOW_DOWN;
            case "access_denied" -> ACCESS_DENIED;
            case "expired_token" -> EXPIRED_CODE;
            case "session_expired" -> SESSION_EXPIRED;
            case "unauthorized" -> UNAUTHORIZED;
            default -> SERVER;
        };
    }
}
