package su.aspectvisuals.client.account;

import java.util.List;

/** Профиль с сайта. Сайт — источник истины, клиент только показывает эти данные. */
public record AspectProfile(
        String id,
        String username,
        String displayName,
        Integer uid,
        String avatarUrl,
        List<String> roles,
        Subscription subscription,
        long fetchedAt) {

    public record Subscription(String name, boolean permanent, Integer daysLeft, String purchasedAt, long expiresAt) {
    }

    /** Подпись под ником: роль, если она есть, иначе тариф подписки. */
    public String badge() {
        if (!roles.isEmpty()) {
            return roles.get(0);
        }
        return subscription != null ? subscription.name() : "";
    }

    public boolean hasSubscription() {
        return subscription != null;
    }
}
