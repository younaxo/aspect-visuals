package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/**
 * Панель сверху по центру: ник, FPS, пинг и TPS сервера.
 *
 * В макете это капсула высотой 32 с внутренними секциями высотой 27,
 * разделёнными тонкими линиями. Отступ секции 8, кегль 12, голова игрока
 * 16x16 со скруглением 2. Ширины секций в макете подогнаны под пример,
 * поэтому здесь они считаются по тексту с тем же отступом.
 */
public class SessionStatsModule extends HudModule {
    private static final float HEIGHT = 32f;
    private static final float INSET = 2.5f;
    private static final float SECTION_PADDING = 8f;
    private static final float TEXT_SIZE = 12f;
    private static final float HEAD = 16f;
    private static final float HEAD_GAP = 4f;
    /** Разделитель короче секции: в макете он высотой 12 по центру. */
    private static final float DIVIDER_HEIGHT = 12f;
    private static final float DIVIDER = 0.5f;

    public SessionStatsModule() {
        super("Session Stats", "Статистика сессии", Category.HUD, 0.5f, 0.03f, HudAnchor.TOP_LEFT);
    }

    private String playerName() {
        return mc.player != null ? mc.player.getGameProfile().getName() : "Aspect";
    }

    private String[] stats() {
        return new String[]{mc.getCurrentFps() + "fps", ping() + "ms", tps() + "tps"};
    }

    private int ping() {
        if (mc.player == null || mc.getNetworkHandler() == null) {
            return 0;
        }
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    private int tps() {
        // Точный TPS клиенту недоступен: показываем целевые 20 при живом соединении
        return mc.getNetworkHandler() != null ? 20 : 0;
    }

    /** Ширина секции с ником: голова, отступ и текст. */
    private float nameSectionWidth() {
        return SECTION_PADDING * 2 + HEAD + HEAD_GAP + AspectFont.SEMIBOLD.width(playerName(), TEXT_SIZE);
    }

    private float statSectionWidth(String stat) {
        return SECTION_PADDING * 2 + AspectFont.MEDIUM.width(stat, TEXT_SIZE);
    }

    @Override
    public float widgetWidth() {
        float width = INSET * 2 + nameSectionWidth();
        for (String stat : stats()) {
            width += statSectionWidth(stat);
        }
        return width;
    }

    @Override
    public float widgetHeight() {
        return HEIGHT;
    }

    @Override
    public void renderWidget(DrawContext context) {
        float width = widgetWidth();

        Render2D.filledBorder(context, 0f, 0f, width, HEIGHT, HEIGHT / 2f,
                AspectColors.SURFACE_CARD, 1f, AspectColors.SURFACE_BORDER);

        float textY = (HEIGHT - AspectFont.MEDIUM.lineHeight(TEXT_SIZE)) / 2f;
        float cursor = INSET;

        // Секция ника: голова и имя
        Render2D.roundedRect(context, cursor + SECTION_PADDING, (HEIGHT - HEAD) / 2f,
                HEAD, HEAD, 2f, AspectColors.SURFACE_INPUT);
        AspectFont.SEMIBOLD.draw(context, playerName(),
                cursor + SECTION_PADDING + HEAD + HEAD_GAP, textY, TEXT_SIZE, AspectColors.TEXT_PRIMARY);
        cursor += nameSectionWidth();

        for (String stat : stats()) {
            // Разделитель ставится перед секцией, по центру высоты
            Render2D.rect(context, cursor, (HEIGHT - DIVIDER_HEIGHT) / 2f,
                    DIVIDER, DIVIDER_HEIGHT, AspectColors.SURFACE_BORDER);

            AspectFont.MEDIUM.draw(context, stat, cursor + SECTION_PADDING, textY,
                    TEXT_SIZE, AspectColors.TEXT_SECONDARY);
            cursor += statSectionWidth(stat);
        }
    }
}
