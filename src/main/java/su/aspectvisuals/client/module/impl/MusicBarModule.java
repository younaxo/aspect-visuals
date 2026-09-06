package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.music.MediaSource;
import su.aspectvisuals.client.music.MusicTrack;
import su.aspectvisuals.client.music.WindowsMediaSource;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/**
 * Проигрываемый трек.
 *
 * Сведения берутся из системного списка сеансов проигрывания, поэтому
 * подходит любой плеер, сообщающий о себе системе, и не нужны ни авторизация,
 * ни подписка сервиса. Пока источник недоступен, виджет не показывается —
 * выдуманного трека здесь нет.
 *
 * Размеры из макета: карточка 181x70, обложка 29x29, полоса 112x6,
 * три кнопки 16x16 справа.
 */
public class MusicBarModule extends HudModule {
    private static final float WIDTH = 181f;
    private static final float HEIGHT = 70f;
    private static final float PADDING = 8.5f;
    private static final float RADIUS = 10f;

    private static final float COVER = 29f;
    private static final float COVER_RADIUS = 8f;
    private static final float TITLE_LEFT = 37f;
    private static final float TITLE_SIZE = 12f;
    private static final float ARTIST_TOP = 17f;
    private static final float ARTIST_SIZE = 10f;

    private static final float BAR_TOP = 45.5f;
    private static final float BAR_INSET = 5f;
    private static final float BAR_WIDTH = 112f;
    private static final float BAR_HEIGHT = 6f;

    private static final float BUTTON = 16f;
    private static final float BUTTON_STEP = 16f;
    private static final float BUTTONS_LEFT = 116f;

    private MediaSource source;

    public MusicBarModule() {
        super("Music Bar", "Проигрываемый трек", Category.HUD, 0.04f, 0.12f, HudAnchor.TOP_LEFT);
    }

    private MusicTrack track() {
        if (source == null && WindowsMediaSource.windows()) {
            source = new WindowsMediaSource();
        }
        return source == null ? null : source.current();
    }

    @Override
    public boolean drawInEditor() {
        return true;
    }

    @Override
    public boolean hasContent() {
        return track() != null;
    }

    @Override
    public float widgetWidth() {
        return WIDTH;
    }

    @Override
    public float widgetHeight() {
        return HEIGHT;
    }

    @Override
    public void renderWidget(DrawContext context) {
        MusicTrack track = track();

        Render2D.filledBorder(context, 0f, 0f, WIDTH, HEIGHT, RADIUS,
                AspectColors.SURFACE_CARD, 1f, AspectColors.SURFACE_BORDER);

        String title = track == null ? "—" : track.title();
        String artist = track == null ? "" : track.artist();

        Render2D.roundedRect(context, PADDING, PADDING, COVER, COVER, COVER_RADIUS,
                AspectColors.SURFACE_INPUT);

        float textX = PADDING + TITLE_LEFT;
        float textWidth = WIDTH - textX - PADDING;
        AspectFont.SEMIBOLD.draw(context, AspectFont.SEMIBOLD.clip(title, textWidth),
                textX, PADDING, TITLE_SIZE, AspectColors.TEXT_PRIMARY);
        AspectFont.MEDIUM.draw(context, AspectFont.MEDIUM.clip(artist, textWidth),
                textX, PADDING + ARTIST_TOP, ARTIST_SIZE, AspectColors.TEXT_SECONDARY);

        float barY = BAR_TOP + BAR_INSET;
        Render2D.roundedRect(context, PADDING, barY, BAR_WIDTH, BAR_HEIGHT,
                BAR_HEIGHT / 2f, AspectColors.SURFACE_INPUT);
        float filled = BAR_WIDTH * (track == null ? 0f : track.progress());
        if (filled > 0f) {
            Render2D.roundedRect(context, PADDING, barY, filled, BAR_HEIGHT,
                    BAR_HEIGHT / 2f, AspectColors.TEXT_PRIMARY);
        }

        // Кнопки показывают состояние, но не управляют: системный список
        // сеансов читается, а команды ему клиент пока не шлёт
        float buttonY = BAR_TOP;
        float buttonX = PADDING + BUTTONS_LEFT;
        Render2D.texture(context, Icons.DOWN, buttonX, buttonY, BUTTON, AspectColors.TEXT_SECONDARY);
        Render2D.texture(context, track != null && track.playing() ? Icons.PAUSE : Icons.CHECK,
                buttonX + BUTTON_STEP, buttonY, BUTTON, AspectColors.TEXT_PRIMARY);
        Render2D.texture(context, Icons.DOWN, buttonX + BUTTON_STEP * 2, buttonY,
                BUTTON, AspectColors.TEXT_SECONDARY);
    }
}
