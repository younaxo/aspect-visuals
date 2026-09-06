package su.aspectvisuals.client.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

import java.util.List;

/**
 * Карточка HUD из макета.
 *
 * Размеры взяты из исходника Figma, а не подобраны: карточка имеет
 * постоянную ширину, заголовок отделён линией, строки идут с фиксированным
 * шагом. Раньше ширина считалась по содержимому — из-за этого блоки
 * расходились по размеру между собой и с макетом.
 */
public final class HudCard {
    /** Ширина информационных карточек в макете постоянная. */
    public static final float WIDTH = 128f;
    public static final float PADDING = 8f;
    public static final float RADIUS = 8f;
    public static final float BORDER = 1f;

    public static final float HEADER_HEIGHT = 15f;
    public static final float ROW_HEIGHT = 16f;
    /** Отступ между заголовком, линией и строками. */
    public static final float GAP = 8f;

    public static final float TITLE_ICON = 12f;
    public static final float TITLE_GAP = 4f;
    public static final float ROW_ICON = 16f;
    public static final float ROW_GAP = 4f;

    private static final float TITLE_SIZE = 12f;
    private static final float ROW_SIZE = 10f;
    /** Смещение текста строки: он ниже иконки на два пункта. */
    private static final float ROW_TEXT_TOP = 2f;

    public record Row(Identifier icon, String label, String value) {
        public static Row of(String label, String value) {
            return new Row(null, label, value);
        }
    }

    private HudCard() {
    }

    public static float width(String title, List<Row> rows) {
        return WIDTH;
    }

    public static float height(String title, List<Row> rows) {
        float total = PADDING * 2;
        if (title != null) {
            // Заголовок, отступ до линии и отступ от линии до первой строки
            total += HEADER_HEIGHT + GAP + GAP;
        }
        if (!rows.isEmpty()) {
            total += rows.size() * ROW_HEIGHT + (rows.size() - 1) * GAP;
        }
        return total;
    }

    public static void draw(DrawContext context, float x, float y, Identifier titleIcon, String title, List<Row> rows) {
        float width = width(title, rows);
        float height = height(title, rows);

        Render2D.filledBorder(context, x, y, width, height,
                RADIUS, AspectColors.SURFACE_CARD, BORDER, AspectColors.SURFACE_BORDER);

        float inner = width - PADDING * 2;
        float cursorY = y + PADDING;

        if (title != null) {
            float textX = x + PADDING;
            if (titleIcon != null) {
                Render2D.texture(context, titleIcon, textX, cursorY + 1.5f, TITLE_ICON, AspectColors.TEXT_SECONDARY);
                textX += TITLE_ICON + TITLE_GAP;
            }
            AspectFont.SEMIBOLD.draw(context, title, textX, cursorY, TITLE_SIZE, AspectColors.TEXT_PRIMARY);
            cursorY += HEADER_HEIGHT + GAP;

            // Линия отделяет заголовок от строк, в макете она тоньше границы
            Render2D.rect(context, x + PADDING, cursorY, inner, 0.5f, AspectColors.SURFACE_BORDER);
            cursorY += GAP;
        }

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            float textX = x + PADDING;
            if (row.icon() != null) {
                Render2D.texture(context, row.icon(), textX, cursorY, ROW_ICON, AspectColors.TEXT_SECONDARY);
                textX += ROW_ICON + ROW_GAP;
            }
            AspectFont.MEDIUM.draw(context, row.label(), textX, cursorY + ROW_TEXT_TOP, ROW_SIZE, AspectColors.TEXT_PRIMARY);

            if (row.value() != null && !row.value().isEmpty()) {
                AspectFont.MEDIUM.drawRight(context, row.value(), x + width - PADDING,
                        cursorY + ROW_TEXT_TOP, ROW_SIZE, AspectColors.TEXT_TERTIARY);
            }
            cursorY += ROW_HEIGHT + GAP;
        }
    }

    /** Компактная пилюля: одна строка текста в капсуле. */
    public static void pill(DrawContext context, float x, float y, Identifier icon, String text) {
        float width = pillWidth(icon, text);
        float height = pillHeight();

        Render2D.filledBorder(context, x, y, width, height,
                height / 2f, AspectColors.SURFACE_CARD, BORDER, AspectColors.SURFACE_BORDER);

        float textX = x + PADDING;
        if (icon != null) {
            Render2D.texture(context, icon, textX, y + (height - TITLE_ICON) / 2f, TITLE_ICON, AspectColors.TEXT_SECONDARY);
            textX += TITLE_ICON + TITLE_GAP;
        }
        AspectFont.MEDIUM.draw(context, text, textX,
                y + (height - AspectFont.MEDIUM.lineHeight(TITLE_SIZE)) / 2f, TITLE_SIZE, AspectColors.TEXT_PRIMARY);
    }

    public static float pillWidth(Identifier icon, String text) {
        return PADDING * 2 + (icon != null ? TITLE_ICON + TITLE_GAP : 0f)
                + AspectFont.MEDIUM.width(text, TITLE_SIZE);
    }

    public static float pillHeight() {
        return 27f;
    }
}
