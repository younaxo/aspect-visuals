package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.module.Module;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

import java.util.ArrayList;
import java.util.List;

/**
 * Привязки клавиш клиента.
 *
 * Показываются модули клиента, а не клавиши движения игры: ходьбу игрок и
 * так знает, а вот какой модуль на какой клавише — нет. Клавиша выводится
 * бейджем справа, как в макете; название приглушено, когда модуль выключен.
 */
public class KeystrokesModule extends HudModule {
    private static final float BADGE_HEIGHT = 16f;
    private static final float BADGE_RADIUS = 4f;
    private static final float BADGE_PADDING = 4.5f;
    private static final float BADGE_TEXT = 8f;
    private static final float LABEL_SIZE = 10f;
    private static final float LABEL_TOP = 2f;
    private static final int BADGE_FILL = 0xB819191B;

    public KeystrokesModule() {
        super("Keybinds", "Привязки клавиш клиента", Category.HUD, 0.86f, 0.22f, HudAnchor.TOP_RIGHT);
    }

    private List<Module> bound() {
        List<Module> result = new ArrayList<>();
        for (Module module : AspectVisuals.modules().all()) {
            if (module.keybind().bound()) {
                result.add(module);
            }
        }
        return result;
    }

    @Override
    public boolean drawInEditor() {
        return true;
    }

    @Override
    public boolean hasContent() {
        return !bound().isEmpty();
    }

    @Override
    public float widgetWidth() {
        return HudCard.WIDTH;
    }

    @Override
    public float widgetHeight() {
        List<HudCard.Row> rows = new ArrayList<>();
        for (int i = 0; i < bound().size(); i++) {
            rows.add(HudCard.Row.of("", ""));
        }
        return HudCard.height("Keybinds", rows);
    }

    @Override
    public void renderWidget(DrawContext context) {
        List<Module> modules = bound();
        List<HudCard.Row> empty = new ArrayList<>();
        for (int i = 0; i < modules.size(); i++) {
            empty.add(HudCard.Row.of("", ""));
        }

        // Карточка рисуется без строк: строка здесь своя, с бейджем клавиши
        HudCard.draw(context, 0f, 0f, Icons.COMMAND, "Keybinds", empty);

        float inner = HudCard.WIDTH - HudCard.PADDING * 2;
        float cursorY = HudCard.PADDING + HudCard.HEADER_HEIGHT + HudCard.GAP + HudCard.GAP;

        for (Module module : modules) {
            int color = module.enabled() ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_SECONDARY;
            AspectFont.MEDIUM.draw(context, module.name(),
                    HudCard.PADDING, cursorY + LABEL_TOP, LABEL_SIZE, color);

            String key = module.keybind().label();
            float badgeWidth = AspectFont.MEDIUM.width(key, BADGE_TEXT) + BADGE_PADDING * 2;
            float badgeX = HudCard.PADDING + inner - badgeWidth;

            Render2D.filledBorder(context, badgeX, cursorY, badgeWidth, BADGE_HEIGHT,
                    BADGE_RADIUS, BADGE_FILL, 1f, AspectColors.SURFACE_BORDER);
            AspectFont.MEDIUM.draw(context, key, badgeX + BADGE_PADDING, cursorY + 3f,
                    BADGE_TEXT, AspectColors.TEXT_PRIMARY);

            cursorY += HudCard.ROW_HEIGHT + HudCard.GAP;
        }
    }
}
