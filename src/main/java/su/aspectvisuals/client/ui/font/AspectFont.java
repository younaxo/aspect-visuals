package su.aspectvisuals.client.ui.font;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.ui.render.UiScale;

/**
 * Текст интерфейса.
 *
 * Начертания взяты из макета — Inter Medium и SemiBold. Глифы рисуются через
 * знаковое поле расстояния в пространстве физических пикселей, поэтому текст
 * не растягивается при GUI Scale и позиционируется дробно: важно для
 * центрирования, прокрутки и анимаций.
 *
 * Кегль по умолчанию — 12 единиц макета, как задано в Figma.
 */
public enum AspectFont {
    REGULAR("inter_regular"),
    MEDIUM("inter_medium"),
    SEMIBOLD("inter_semibold");

    /** Размер текста интерфейса в единицах макета. */
    public static final float SIZE = 12f;

    private final SdfFont font;
    private final Identifier fallbackId;

    AspectFont(String name) {
        this.font = new SdfFont(name);
        this.fallbackId = AspectVisuals.id(name);
    }

    public static void invalidate() {
        for (AspectFont value : values()) {
            value.font.invalidate();
        }
    }

    public float width(String text) {
        return width(text, SIZE);
    }

    public float width(String text, float size) {
        if (text == null || text.isEmpty()) {
            return 0f;
        }
        if (!font.ready()) {
            return fallbackWidth(text, size);
        }
        return font.width(text, size);
    }

    public float lineHeight() {
        return lineHeight(SIZE);
    }

    public float lineHeight(float size) {
        return font.ready() ? font.lineHeight(size) : size * 1.2f;
    }

    public void draw(DrawContext context, String text, float x, float y, int color) {
        draw(context, text, x, y, SIZE, color);
    }

    public void draw(DrawContext context, String text, float x, float y, float size, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (!font.ready()) {
            drawFallback(context, text, x, y, color);
            return;
        }

        float scale = UiScale.push(context);
        try {
            font.draw(context, text, x * scale, y * scale, size * scale, color);
        } finally {
            UiScale.pop(context);
        }
    }

    public void drawCentered(DrawContext context, String text, float centerX, float y, int color) {
        draw(context, text, centerX - width(text) / 2f, y, color);
    }

    public void drawRight(DrawContext context, String text, float rightX, float y, int color) {
        drawRight(context, text, rightX, y, SIZE, color);
    }

    public void drawRight(DrawContext context, String text, float rightX, float y, float size, int color) {
        draw(context, text, rightX - width(text, size), y, size, color);
    }

    /** Обрезает строку по ширине и дописывает многоточие. */
    public String clip(String text, float maxWidth) {
        if (text == null || text.isEmpty() || width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "…";
        float limit = Math.max(0f, maxWidth - width(ellipsis));
        StringBuilder builder = new StringBuilder();
        float used = 0f;

        for (int i = 0; i < text.length(); i++) {
            float advance = width(String.valueOf(text.charAt(i)));
            if (used + advance > limit) {
                break;
            }
            builder.append(text.charAt(i));
            used += advance;
        }
        return builder.append(ellipsis).toString();
    }

    // --- Запасной путь ---
    //
    // Пока атлас или шейдер не загружены, интерфейс не должен оставаться без
    // подписей. Ванильный рендер текста хуже по качеству, но виден и читаем.

    private float fallbackWidth(String text, float size) {
        return MinecraftClient.getInstance().textRenderer.getWidth(styled(text)) * (size / 9f);
    }

    private void drawFallback(DrawContext context, String text, float x, float y, int color) {
        context.drawText(MinecraftClient.getInstance().textRenderer, styled(text),
                Math.round(x), Math.round(y), color, false);
    }

    private Text styled(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(fallbackId));
    }
}
