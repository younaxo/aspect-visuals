package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/** Панель сверху по центру: ник, FPS, пинг и TPS сервера. */
public class SessionStatsModule extends HudModule {
    private static final float GAP = 10f;

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

    @Override
    public float widgetWidth() {
        float width = HudCard.PADDING * 2 + AspectFont.SEMIBOLD.width(playerName());
        for (String stat : stats()) {
            width += GAP + AspectFont.MEDIUM.width(stat);
        }
        return width;
    }

    @Override
    public float widgetHeight() {
        return HudCard.pillHeight();
    }

    @Override
    public void renderWidget(DrawContext context) {
        float width = widgetWidth();
        float height = widgetHeight();

        Render2D.filledBorder(context, 0f, 0f, width, height,
                height / 2f, AspectColors.SURFACE_MAIN_GLASS, 0.5f, AspectColors.SURFACE_BORDER);

        float textY = (height - AspectFont.MEDIUM.lineHeight()) / 2f;
        float cursor = HudCard.PADDING;

        AspectFont.SEMIBOLD.draw(context, playerName(), cursor, textY, AspectColors.TEXT_PRIMARY);
        cursor += AspectFont.SEMIBOLD.width(playerName());

        for (String stat : stats()) {
            cursor += GAP;
            AspectFont.MEDIUM.draw(context, stat, cursor, textY, AspectColors.TEXT_TERTIARY);
            cursor += AspectFont.MEDIUM.width(stat);
        }
    }
}
