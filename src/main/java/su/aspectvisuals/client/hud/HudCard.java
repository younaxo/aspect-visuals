package su.aspectvisuals.client.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

import java.util.List;

/**
 * Стеклянная карточка HUD из макета: заголовок с иконкой и строки
 * «подпись — значение». Ширина считается по содержимому.
 */
public final class HudCard {
    public static final float PADDING = 8f;
    public static final float ROW_HEIGHT = 14f;
    public static final float HEADER_HEIGHT = 16f;
    public static final float RADIUS = 12f;
    public static final float ICON = 10f;
    public static final float GAP = 6f;

    public record Row(Identifier icon, String label, String value) {
        public static Row of(String label, String value) {
            return new Row(null, label, value);
        }
    }

    private HudCard() {
    }

    public static float width(String title, List<Row> rows) {
        float widest = title == null ? 0f : ICON + GAP + AspectFont.SEMIBOLD.width(title);
        for (Row row : rows) {
            float rowWidth = (row.icon() != null ? ICON + GAP : 0f)
                    + AspectFont.MEDIUM.width(row.label())
                    + 16f
                    + AspectFont.MEDIUM.width(row.value() == null ? "" : row.value());
            widest = Math.max(widest, rowWidth);
        }
        return widest + PADDING * 2;
    }

    public static float height(String title, List<Row> rows) {
        float total = PADDING * 2 + rows.size() * ROW_HEIGHT;
        if (title != null) {
            total += HEADER_HEIGHT;
        }
        return total;
    }

    public static void draw(DrawContext context, float x, float y, Identifier titleIcon, String title, List<Row> rows) {
        float width = width(title, rows);
        float height = height(title, rows);

        Render2D.roundedRect(context, x, y, width, height, RADIUS, AspectColors.SURFACE_MAIN_GLASS);
        Render2D.border(context, x, y, width, height, 0.5f, AspectColors.SURFACE_BORDER);

        float cursorY = y + PADDING;
        if (title != null) {
            float textX = x + PADDING;
            if (titleIcon != null) {
                Render2D.texture(context, titleIcon, textX, cursorY, ICON, AspectColors.TEXT_SECONDARY);
                textX += ICON + GAP;
            }
            AspectFont.SEMIBOLD.draw(context, title, textX, cursorY, AspectColors.TEXT_PRIMARY);
            cursorY += HEADER_HEIGHT;
        }

        for (Row row : rows) {
            float textX = x + PADDING;
            if (row.icon() != null) {
                Render2D.texture(context, row.icon(), textX, cursorY + 1f, ICON, AspectColors.TEXT_SECONDARY);
                textX += ICON + GAP;
            }
            AspectFont.MEDIUM.draw(context, row.label(), textX, cursorY, AspectColors.TEXT_SECONDARY);

            if (row.value() != null && !row.value().isEmpty()) {
                AspectFont.MEDIUM.drawRight(context, row.value(), x + width - PADDING, cursorY, AspectColors.TEXT_TERTIARY);
            }
            cursorY += ROW_HEIGHT;
        }
    }

    /** Компактная пилюля: одна строка текста в стеклянной капсуле. */
    public static void pill(DrawContext context, float x, float y, Identifier icon, String text) {
        float width = pillWidth(icon, text);
        float height = pillHeight();

        Render2D.roundedRect(context, x, y, width, height, height / 2f, AspectColors.SURFACE_MAIN_GLASS);
        Render2D.border(context, x, y, width, height, 0.5f, AspectColors.SURFACE_BORDER);

        float textX = x + PADDING;
        if (icon != null) {
            Render2D.texture(context, icon, textX, y + (height - ICON) / 2f, ICON, AspectColors.TEXT_SECONDARY);
            textX += ICON + GAP;
        }
        AspectFont.MEDIUM.draw(context, text, textX, y + (height - AspectFont.MEDIUM.lineHeight()) / 2f, AspectColors.TEXT_PRIMARY);
    }

    public static float pillWidth(Identifier icon, String text) {
        return PADDING * 2 + (icon != null ? ICON + GAP : 0f) + AspectFont.MEDIUM.width(text);
    }

    public static float pillHeight() {
        return AspectFont.MEDIUM.lineHeight() + PADDING + 2f;
    }
}
