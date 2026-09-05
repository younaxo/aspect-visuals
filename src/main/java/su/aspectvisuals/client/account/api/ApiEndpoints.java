package su.aspectvisuals.client.account.api;

import su.aspectvisuals.client.AspectVisuals;

import java.net.URI;
import java.util.Locale;

/**
 * Один источник адресов бэкенда.
 *
 * Боевой адрес зашит в клиент и по умолчанию не переопределяется: обычный
 * конфиг не должен уметь незаметно увести токен сессии на чужой хост.
 * Подменить адрес можно только системным свойством запуска — этого хватает
 * для разработки и требует явного действия.
 */
public final class ApiEndpoints {
    private static final String PRODUCTION = "https://aspectvisuals.su";
    private static final String OVERRIDE_PROPERTY = "aspectvisuals.api";

    private static final String base = resolveBase();

    private ApiEndpoints() {
    }

    private static String resolveBase() {
        String override = System.getProperty(OVERRIDE_PROPERTY, "").trim();
        if (override.isEmpty()) {
            return PRODUCTION;
        }

        String normalized = override.replaceAll("/+$", "");
        if (!isAllowedOverride(normalized)) {
            AspectVisuals.LOGGER.warn("Адрес API из {} отклонён, используется боевой", OVERRIDE_PROPERTY);
            return PRODUCTION;
        }

        AspectVisuals.LOGGER.info("Адрес API переопределён: {}", normalized);
        return normalized;
    }

    /** Токены уходят только по HTTPS. Исключение — локальная разработка. */
    private static boolean isAllowedOverride(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);

            if ("https".equals(scheme)) {
                return true;
            }
            return "http".equals(scheme) && (host.equals("localhost") || host.equals("127.0.0.1"));
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    public static String base() {
        return base;
    }

    public static URI deviceCode() {
        return URI.create(base + "/api/client/auth/device");
    }

    public static URI deviceToken() {
        return URI.create(base + "/api/client/auth/token");
    }

    public static URI profile() {
        return URI.create(base + "/api/client/profile");
    }

    public static URI refresh() {
        return URI.create(base + "/api/client/session/refresh");
    }

    public static URI logout() {
        return URI.create(base + "/api/client/session");
    }
}
