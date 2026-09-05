package su.aspectvisuals.client.ui.font;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.AspectVisuals;

/**
 * Текст интерфейса рисуется настоящим Inter из макета.
 *
 * Шрифты подключены штатным ttf-провайдером Minecraft: он сам держит атлас
 * глифов, поддерживает кириллицу и сглаживание, поэтому собственный
 * растеризатор здесь не нужен.
 */
public enum AspectFont {
    REGULAR("inter_regular"),
    MEDIUM("inter_medium"),
    SEMIBOLD("inter_semibold");

    private final Identifier id;

    AspectFont(String path) {
        this.id = AspectVisuals.id(path);
    }

    public Identifier id() {
        return id;
    }

    public MutableText apply(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(id));
    }

    private static TextRenderer renderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public int width(String text) {
        return renderer().getWidth(apply(text));
    }

    public int lineHeight() {
        return renderer().fontHeight;
    }

    public void draw(DrawContext context, String text, float x, float y, int color) {
        context.drawText(renderer(), apply(text), Math.round(x), Math.round(y), color, false);
    }

    public void drawCentered(DrawContext context, String text, float centerX, float y, int color) {
        draw(context, text, centerX - width(text) / 2f, y, color);
    }

    public void drawRight(DrawContext context, String text, float rightX, float y, int color) {
        draw(context, text, rightX - width(text), y, color);
    }

    /** Обрезает строку по ширине и дописывает многоточие. */
    public String clip(String text, int maxWidth) {
        if (width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        int limit = Math.max(0, maxWidth - width(ellipsis));
        StringBuilder builder = new StringBuilder();
        for (char symbol : text.toCharArray()) {
            if (width(builder.toString() + symbol) > limit) {
                break;
            }
            builder.append(symbol);
        }
        return builder.append(ellipsis).toString();
    }
}
