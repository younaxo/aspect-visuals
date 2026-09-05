package su.aspectvisuals.client.ui.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.ui.theme.AspectColors;

/**
 * Примитивы интерфейса Aspect Visuals.
 *
 * Скругления рисуются построчно: DrawContext умеет только целочисленные
 * прямоугольники, поэтому сглаживание углов делается покрытием — крайние
 * пиксели строки получают пониженную альфу. Это дешевле собственного
 * шейдера и не ломает состояние рендера Minecraft.
 */
public final class Render2D {
    /** Иконки растеризованы в 4x от 16 пикселей макета. */
    private static final int ICON_SOURCE = 64;

    private Render2D() {
    }

    public static void rect(DrawContext context, float x, float y, float width, float height, int color) {
        if (width <= 0 || height <= 0 || alpha(color) == 0) {
            return;
        }
        context.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
    }

    public static void gradient(DrawContext context, float x, float y, float width, float height, int top, int bottom) {
        if (width <= 0 || height <= 0) {
            return;
        }
        context.fillGradient(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), top, bottom);
    }

    public static void roundedRect(DrawContext context, float x, float y, float width, float height, float radius, int color) {
        if (width <= 0 || height <= 0 || alpha(color) == 0) {
            return;
        }

        float r = Math.min(radius, Math.min(width, height) / 2f);
        if (r < 0.5f) {
            rect(context, x, y, width, height, color);
            return;
        }

        int left = Math.round(x);
        int right = Math.round(x + width);
        int top = Math.round(y);
        int bottom = Math.round(y + height);
        int rows = (int) Math.ceil(r);

        // Середина одним прямоугольником, скруглённые полосы — построчно
        context.fill(left, top + rows, right, bottom - rows, color);

        for (int row = 0; row < rows; row++) {
            float centerOffset = r - (row + 0.5f);
            float inset = r - (float) Math.sqrt(Math.max(0f, r * r - centerOffset * centerOffset));
            int solid = (int) Math.ceil(inset);
            float coverage = 1f - (solid - inset);

            int innerLeft = left + solid;
            int innerRight = right - solid;
            if (innerRight <= innerLeft) {
                continue;
            }

            context.fill(innerLeft, top + row, innerRight, top + row + 1, color);
            context.fill(innerLeft, bottom - row - 1, innerRight, bottom - row, color);

            if (coverage > 0.02f && solid > 0) {
                int edge = AspectColors.withAlpha(color, coverage);
                context.fill(innerLeft - 1, top + row, innerLeft, top + row + 1, edge);
                context.fill(innerRight, top + row, innerRight + 1, top + row + 1, edge);
                context.fill(innerLeft - 1, bottom - row - 1, innerLeft, bottom - row, edge);
                context.fill(innerRight, bottom - row - 1, innerRight + 1, bottom - row, edge);
            }
        }
    }

    /**
     * Рамка карточки: контур рисуется четырьмя полосами, чтобы не затирать
     * уже нарисованный фон внутри.
     */
    public static void border(DrawContext context, float x, float y, float width, float height, float thickness, int color) {
        if (alpha(color) == 0 || thickness <= 0 || width <= 0 || height <= 0) {
            return;
        }
        float t = Math.max(1f, thickness);
        rect(context, x, y, width, t, color);
        rect(context, x, y + height - t, width, t, color);
        rect(context, x, y + t, t, height - t * 2, color);
        rect(context, x + width - t, y + t, t, height - t * 2, color);
    }

    /** Мягкая тень из макета: смещение вниз и растушёвка слоями. */
    public static void shadow(DrawContext context, float x, float y, float width, float height, float radius) {
        int layers = 6;
        for (int i = layers; i > 0; i--) {
            float spread = i * 2f;
            int color = AspectColors.withAlpha(AspectColors.SHADOW, 1f - (i - 1) / (float) layers);
            roundedRect(context, x - spread, y - spread + 4f, width + spread * 2, height + spread * 2, radius + spread, color);
        }
    }

    public static void texture(DrawContext context, Identifier texture, float x, float y, float size, int color) {
        texture(context, texture, x, y, size, size, color);
    }

    public static void texture(DrawContext context, Identifier texture, float x, float y, float width, float height, int color) {
        // Иконки набора хранятся в 4x от размера макета
        texture(context, texture, x, y, width, height, ICON_SOURCE, ICON_SOURCE, color);
    }

    /**
     * Отрисовка текстуры произвольного размера: аватар с сайта приходит
     * не в 64x64, поэтому исходный размер задаётся явно.
     */
    public static void texture(DrawContext context, Identifier texture, float x, float y, float width, float height,
                               int sourceWidth, int sourceHeight, int color) {
        if (alpha(color) == 0 || width <= 0 || height <= 0 || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0f);
        context.getMatrices().scale(width / sourceWidth, height / sourceHeight, 1f);
        context.drawTexture(RenderLayer::getGuiTextured, texture, 0, 0, 0f, 0f,
                sourceWidth, sourceHeight, sourceWidth, sourceHeight, color);
        context.getMatrices().pop();
    }

    public static boolean hovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int alpha(int color) {
        return (color >>> 24) & 0xFF;
    }
}
