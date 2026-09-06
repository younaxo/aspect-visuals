package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.account.AccountManager;
import su.aspectvisuals.client.account.AspectProfile;
import su.aspectvisuals.client.account.DeviceRequest;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;
import su.aspectvisuals.client.util.Animation;

/**
 * Капсула профиля в шапке и раскрывающаяся карточка аккаунта из макета.
 * Данные приходят с сайта, клиент их только показывает.
 */
public class ProfileControl extends Component {
    private static final float HEIGHT = 32f;
    private static final float PADDING = 6f;
    private static final float AVATAR = 20f;
    private static final float GAP = 8f;

    private static final float CARD_WIDTH = 212f;
    private static final float CARD_PADDING = 12f;

    private final AccountManager account;
    private final Animation reveal = new Animation(0.18f);
    private boolean expanded;

    public ProfileControl(AccountManager account) {
        super(0f, HEIGHT);
        this.account = account;
    }

    public boolean expanded() {
        return expanded;
    }

    public void collapse() {
        expanded = false;
    }

    private String label() {
        AspectProfile profile = account.profile();
        if (profile != null) {
            return profile.displayName();
        }
        return switch (account.state()) {
            case SIGNED_IN -> "Загружаем профиль…";
            case LINKING -> "Ожидаем подтверждения";
            case SIGNED_OUT -> "Войти";
        };
    }

    @Override
    public float width() {
        return PADDING * 2 + AVATAR + GAP + AspectFont.MEDIUM.width(label());
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        width = width();
        boolean hover = hovered(mouseX, mouseY);

        Render2D.filledBorder(context, x, y, width, height,
                height / 2f, hover || expanded ? AspectColors.SURFACE_CARD_HOVER : AspectColors.SURFACE_MAIN_GLASS, 0.5f, AspectColors.SURFACE_BORDER);

        drawAvatar(context, x + PADDING, y + (height - AVATAR) / 2f, AVATAR);
        AspectFont.MEDIUM.draw(context, label(), x + PADDING + AVATAR + GAP,
                y + (height - AspectFont.MEDIUM.lineHeight()) / 2f, AspectColors.TEXT_PRIMARY);
    }

    private void drawAvatar(DrawContext context, float x, float y, float size) {
        Render2D.roundedRect(context, x, y, size, size, size / 2f, AspectColors.SURFACE_INPUT);

        if (account.avatars().ready()) {
            Render2D.texture(context, account.avatars().texture(), x + 1f, y + 1f, size - 2f, size - 2f,
                    account.avatars().width(), account.avatars().height(), AspectColors.ACCENT_PRIMARY);
            return;
        }
        Render2D.texture(context, Icons.LOGOTYPE, x + size * 0.25f, y + size * 0.25f, size * 0.5f, AspectColors.TEXT_TERTIARY);
    }

    /** Карточка аккаунта рисуется поверх остального экрана. */
    public void renderCard(DrawContext context, int mouseX, int mouseY) {
        reveal.target(expanded ? 1f : 0f);
        float progress = reveal.eased();
        if (progress < 0.02f) {
            return;
        }

        float cardHeight = cardHeight();
        float height = cardHeight * progress;
        float cardY = y + this.height + 8f;

        Render2D.shadow(context, x, cardY, CARD_WIDTH, height, 12f);
        Render2D.filledBorder(context, x, cardY, CARD_WIDTH, height,
                12f, AspectColors.SURFACE_CARD, 0.5f, AspectColors.SURFACE_BORDER);

        if (progress < 0.9f) {
            return;
        }

        Render2D.pushClip(context, x, cardY, CARD_WIDTH, height);
        switch (account.state()) {
            case SIGNED_IN -> drawAccount(context, cardY, mouseX, mouseY);
            case LINKING -> drawLinking(context, cardY);
            case SIGNED_OUT -> drawSignedOut(context, cardY, mouseX, mouseY);
        }
        Render2D.popClip(context);
    }

    private float cardHeight() {
        return switch (account.state()) {
            case SIGNED_IN -> account.profile() != null && account.profile().hasSubscription() ? 148f : 104f;
            case LINKING -> 118f;
            case SIGNED_OUT -> 96f;
        };
    }

    private void drawAccount(DrawContext context, float cardY, int mouseX, int mouseY) {
        AspectProfile profile = account.profile();
        float cursorY = cardY + CARD_PADDING;

        drawAvatar(context, x + CARD_PADDING, cursorY, 32f);

        float textX = x + CARD_PADDING + 32f + GAP;
        String name = profile != null ? profile.displayName() : "Профиль";
        AspectFont.SEMIBOLD.draw(context, AspectFont.SEMIBOLD.clip(name, (int) (CARD_WIDTH - (textX - x) - CARD_PADDING)),
                textX, cursorY + 2f, AspectColors.TEXT_PRIMARY);

        String badge = profile != null ? profile.badge() : "";
        if (!badge.isEmpty()) {
            AspectFont.MEDIUM.draw(context, badge, textX, cursorY + 16f, AspectColors.TEXT_TERTIARY);
        }

        cursorY += 32f + 12f;

        if (profile != null && profile.hasSubscription()) {
            drawSubscription(context, profile.subscription(), cursorY);
            cursorY += 44f;
        } else if (profile != null) {
            AspectFont.MEDIUM.draw(context, "Подписка не активна", x + CARD_PADDING, cursorY, AspectColors.TEXT_TERTIARY);
            cursorY += 20f;
        }

        if (!account.statusMessage().isEmpty()) {
            AspectFont.MEDIUM.draw(context, account.statusMessage(), x + CARD_PADDING, cursorY, AspectColors.SYSTEM_INFO);
            cursorY += 16f;
        }

        drawAction(context, "Обновить", x + CARD_PADDING, cursorY, mouseX, mouseY);
        drawAction(context, "Выйти", x + CARD_WIDTH - CARD_PADDING - AspectFont.MEDIUM.width("Выйти"), cursorY, mouseX, mouseY);
    }

    private void drawSubscription(DrawContext context, AspectProfile.Subscription subscription, float cursorY) {
        float width = CARD_WIDTH - CARD_PADDING * 2;
        Render2D.roundedRect(context, x + CARD_PADDING, cursorY, width, 40f, 8f, AspectColors.SURFACE_INPUT);

        AspectFont.SEMIBOLD.draw(context, AspectFont.SEMIBOLD.clip(subscription.name(), (int) width - 60),
                x + CARD_PADDING + 8f, cursorY + 6f, AspectColors.TEXT_PRIMARY);

        String remaining = subscription.permanent()
                ? "Навсегда"
                : (subscription.daysLeft() == null ? "" : subscription.daysLeft() + " дн.");
        AspectFont.MEDIUM.drawRight(context, remaining, x + CARD_WIDTH - CARD_PADDING - 8f, cursorY + 6f, AspectColors.TEXT_TERTIARY);

        // Полоска остатка: без срока (навсегда) она заполнена целиком
        float progress = subscription.permanent() ? 1f : progressOf(subscription);
        float barWidth = width - 16f;
        Render2D.roundedRect(context, x + CARD_PADDING + 8f, cursorY + 24f, barWidth, 3f, 1.5f, AspectColors.BOOLEAN_SOFT);
        Render2D.roundedRect(context, x + CARD_PADDING + 8f, cursorY + 24f, barWidth * progress, 3f, 1.5f, AspectColors.ACCENT_PRIMARY);
    }

    private float progressOf(AspectProfile.Subscription subscription) {
        if (subscription.daysLeft() == null) {
            return 0f;
        }
        // Шкала на 30 дней: столько же показывает сайт в карточке подписки
        return Math.max(0f, Math.min(1f, subscription.daysLeft() / 30f));
    }

    private void drawLinking(DrawContext context, float cardY) {
        DeviceRequest request = account.pending();
        float cursorY = cardY + CARD_PADDING;

        AspectFont.SEMIBOLD.draw(context, "Подтвердите вход", x + CARD_PADDING, cursorY, AspectColors.TEXT_PRIMARY);
        cursorY += 20f;

        AspectFont.MEDIUM.draw(context, "Откройте сайт и введите код:", x + CARD_PADDING, cursorY, AspectColors.TEXT_SECONDARY);
        cursorY += 18f;

        String code = request != null ? request.userCode() : "…";
        float codeWidth = CARD_WIDTH - CARD_PADDING * 2;
        Render2D.roundedRect(context, x + CARD_PADDING, cursorY, codeWidth, 26f, 8f, AspectColors.SURFACE_INPUT);
        AspectFont.SEMIBOLD.drawCentered(context, code, x + CARD_WIDTH / 2f,
                cursorY + (26f - AspectFont.SEMIBOLD.lineHeight()) / 2f, AspectColors.TEXT_PRIMARY);
        cursorY += 34f;

        AspectFont.MEDIUM.draw(context, "Отмена", x + CARD_PADDING, cursorY, AspectColors.TEXT_TERTIARY);
    }

    private void drawSignedOut(DrawContext context, float cardY, int mouseX, int mouseY) {
        float cursorY = cardY + CARD_PADDING;

        AspectFont.SEMIBOLD.draw(context, "Aspect Visuals", x + CARD_PADDING, cursorY, AspectColors.TEXT_PRIMARY);
        cursorY += 18f;

        String message = account.statusMessage().isEmpty()
                ? "Войдите, чтобы видеть профиль"
                : account.statusMessage();
        AspectFont.MEDIUM.draw(context, AspectFont.MEDIUM.clip(message, (int) CARD_WIDTH - 24),
                x + CARD_PADDING, cursorY, AspectColors.TEXT_SECONDARY);
        cursorY += 24f;

        drawAction(context, "Войти", x + CARD_PADDING, cursorY, mouseX, mouseY);
    }

    private void drawAction(DrawContext context, String label, float actionX, float actionY, int mouseX, int mouseY) {
        boolean hover = Render2D.hovered(mouseX, mouseY, actionX, actionY - 2f,
                AspectFont.MEDIUM.width(label), AspectFont.MEDIUM.lineHeight() + 4f);
        AspectFont.MEDIUM.draw(context, label, actionX, actionY,
                hover ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_SECONDARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        width = width();

        if (expanded && handleCardClick(mouseX, mouseY)) {
            return true;
        }

        if (hovered(mouseX, mouseY)) {
            expanded = !expanded;
            if (expanded) {
                account.ensureFresh();
            }
            return true;
        }

        if (expanded) {
            expanded = false;
            return true;
        }
        return false;
    }

    private boolean handleCardClick(double mouseX, double mouseY) {
        float cardY = y + height + 8f;
        if (!Render2D.hovered(mouseX, mouseY, x, cardY, CARD_WIDTH, cardHeight())) {
            return false;
        }

        switch (account.state()) {
            case SIGNED_OUT -> {
                account.beginLogin();
                return true;
            }
            case LINKING -> {
                float cancelY = cardY + CARD_PADDING + 90f;
                if (mouseY >= cancelY - 4f) {
                    account.cancelLogin();
                } else {
                    account.openVerificationPage();
                }
                return true;
            }
            case SIGNED_IN -> {
                float actionsY = cardY + cardHeight() - CARD_PADDING - AspectFont.MEDIUM.lineHeight();
                if (mouseY >= actionsY - 6f) {
                    if (mouseX < x + CARD_WIDTH / 2f) {
                        account.refreshProfile();
                    } else {
                        account.logout();
                        expanded = false;
                    }
                }
                return true;
            }
        }
        return true;
    }
}
