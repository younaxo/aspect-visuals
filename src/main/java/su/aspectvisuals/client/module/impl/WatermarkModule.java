package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.account.AccountManager;
import su.aspectvisuals.client.account.AspectProfile;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.setting.BooleanSetting;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/**
 * Левый верхний угол из макета: знак Aspect и капсула профиля с сайта.
 * Без входа в аккаунт капсула показывает приглашение войти.
 */
public class WatermarkModule extends HudModule {
    private static final float MARK = 26f;
    private static final float GAP = 8f;
    private static final float PILL_HEIGHT = 26f;
    private static final float PADDING = 6f;
    private static final float AVATAR = 18f;

    private final BooleanSetting showProfile = register(new BooleanSetting("Профиль", "Показывать капсулу аккаунта", true));

    public WatermarkModule() {
        super("Watermark", "Знак клиента и профиль", Category.HUD, 0.012f, 0.028f, HudAnchor.TOP_LEFT);
    }

    private AccountManager account() {
        return AspectVisuals.account();
    }

    private String label() {
        AccountManager account = account();
        if (account == null) {
            return AspectVisuals.NAME;
        }

        AspectProfile profile = account.profile();
        if (profile != null) {
            return profile.displayName();
        }
        return switch (account.state()) {
            case SIGNED_IN -> "Загружаем профиль…";
            case LINKING -> "Подтвердите вход на сайте";
            case SIGNED_OUT -> "Войти в Aspect Visuals";
        };
    }

    private float pillWidth() {
        return PADDING * 2 + AVATAR + GAP + AspectFont.MEDIUM.width(label());
    }

    @Override
    public float widgetWidth() {
        return showProfile.get() ? MARK + GAP + pillWidth() : MARK;
    }

    @Override
    public float widgetHeight() {
        return MARK;
    }

    @Override
    public void renderWidget(DrawContext context) {
        Render2D.roundedRect(context, 0f, 0f, MARK, MARK, MARK / 2f, AspectColors.SURFACE_MAIN_GLASS);
        Render2D.border(context, 0f, 0f, MARK, MARK, 0.5f, AspectColors.SURFACE_BORDER);
        Render2D.texture(context, Icons.LOGO, (MARK - 14f) / 2f, (MARK - 14f) / 2f, 14f, AspectColors.ACCENT_PRIMARY);

        if (!showProfile.get()) {
            return;
        }

        float pillX = MARK + GAP;
        float width = pillWidth();
        float top = (MARK - PILL_HEIGHT) / 2f;

        Render2D.roundedRect(context, pillX, top, width, PILL_HEIGHT, PILL_HEIGHT / 2f, AspectColors.SURFACE_MAIN_GLASS);
        Render2D.border(context, pillX, top, width, PILL_HEIGHT, 0.5f, AspectColors.SURFACE_BORDER);

        drawAvatar(context, pillX + PADDING, top + (PILL_HEIGHT - AVATAR) / 2f);

        float textX = pillX + PADDING + AVATAR + GAP;
        float textY = top + (PILL_HEIGHT - AspectFont.MEDIUM.lineHeight()) / 2f;
        int color = account() != null && account().signedIn() ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_SECONDARY;
        AspectFont.MEDIUM.draw(context, label(), textX, textY, color);
    }

    private void drawAvatar(DrawContext context, float x, float y) {
        AccountManager account = account();
        Render2D.roundedRect(context, x, y, AVATAR, AVATAR, AVATAR / 2f, AspectColors.SURFACE_INPUT);

        if (account != null && account.avatars().ready()) {
            // Круглая маска без шейдера недоступна, поэтому изображение вписывается
            // в круглую подложку с небольшим отступом
            Render2D.texture(context, account.avatars().texture(), x + 1f, y + 1f, AVATAR - 2f, AVATAR - 2f,
                    account.avatars().width(), account.avatars().height(), AspectColors.ACCENT_PRIMARY);
            return;
        }

        Render2D.texture(context, Icons.LOGOTYPE, x + 4f, y + 4f, AVATAR - 8f, AspectColors.TEXT_TERTIARY);
    }
}
