package su.aspectvisuals.client.ui.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import su.aspectvisuals.client.ui.theme.AspectColors;
import su.aspectvisuals.client.ui.theme.AspectSizes;

/**
 * Примитивы интерфейса Aspect Visuals.
 *
 * Все методы принимают координаты раскладки (единица равна логическому пикселю
 * Minecraft), но рисуют в пространстве физических пикселей: матрица ужимается
 * в {@code 1 / scale}, координаты умножаются на {@code scale}. Благодаря этому
 * дробные позиции доживают до растеризации, а сглаживание считается по реальной
 * плотности экрана, а не по сетке логических пикселей.
 *
 * Форма, граница и тень считаются одним шейдером через поле расстояния, поэтому
 * скругления остаются гладкими при любом GUI Scale и разрешении.
 */
public final class Render2D {
    /** Иконки растеризованы с запасом плотности относительно размера в макете. */
    private static final int ICON_SOURCE = 128;

    private static final float[] SQUARE = {0f, 0f, 0f, 0f};

    private Render2D() {
    }

    public static void rect(DrawContext context, float x, float y, float width, float height, int color) {
        shape(context, x, y, width, height, SQUARE, color, color, 0f, 0, 0f);
    }

    public static void gradient(DrawContext context, float x, float y, float width, float height, int top, int bottom) {
        shape(context, x, y, width, height, SQUARE, top, bottom, 0f, 0, 0f);
    }

    public static void roundedRect(DrawContext context, float x, float y, float width, float height,
                                   float radius, int color) {
        shape(context, x, y, width, height, corners(radius), color, color, 0f, 0, 0f);
    }

    public static void roundedGradient(DrawContext context, float x, float y, float width, float height,
                                       float radius, int top, int bottom) {
        shape(context, x, y, width, height, corners(radius), top, bottom, 0f, 0, 0f);
    }

    /** Разные радиусы по углам: левый верхний, правый верхний, правый нижний, левый нижний. */
    public static void roundedRect(DrawContext context, float x, float y, float width, float height,
                                   float[] radius, int color) {
        shape(context, x, y, width, height, radius, color, color, 0f, 0, 0f);
    }

    /**
     * Заливка с границей одной фигурой: контур считается из того же поля
     * расстояния, поэтому на скруглениях нет стыков отдельных полос.
     */
    public static void filledBorder(DrawContext context, float x, float y, float width, float height,
                                    float radius, int fill, float thickness, int borderColor) {
        shape(context, x, y, width, height, corners(radius), fill, fill, thickness, borderColor, 0f);
    }

    /** Только контур, без заливки. */
    public static void border(DrawContext context, float x, float y, float width, float height,
                              float thickness, int color) {
        border(context, x, y, width, height, 0f, thickness, color);
    }

    public static void border(DrawContext context, float x, float y, float width, float height,
                              float radius, float thickness, int color) {
        shape(context, x, y, width, height, corners(radius), 0x00000000, 0x00000000, thickness, color, 0f);
    }

    /**
     * Мягкая тень из макета: смещение вниз на 24 и растушёвка 32, как задано
     * в эффекте Drop Shadow. Растушёвка идёт полем расстояния, а не набором
     * вложенных прямоугольников с падающей альфой.
     */
    public static void shadow(DrawContext context, float x, float y, float width, float height, float radius) {
        shadow(context, x, y, width, height, radius,
                AspectColors.SHADOW, AspectSizes.SHADOW_BLUR, AspectSizes.SHADOW_OFFSET_Y);
    }

    public static void shadow(DrawContext context, float x, float y, float width, float height,
                              float radius, int color, float blur, float offsetY) {
        shape(context, x, y + offsetY, width, height, corners(radius), color, color, 0f, 0, blur);
    }

    private static void shape(DrawContext context, float x, float y, float width, float height,
                              float[] radius, int fill, int gradient, float border, int borderColor, float softness) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (alpha(fill) == 0 && alpha(gradient) == 0 && (border <= 0f || alpha(borderColor) == 0)) {
            return;
        }
        if (UiClip.cullsEverything()) {
            return;
        }

        if (!ShapeRenderer.ready()) {
            fallback(context, x, y, width, height, fill);
            return;
        }

        float scale = UiScale.push(context);
        try {
            float[] scaled = {
                    radius[0] * scale, radius[1] * scale, radius[2] * scale, radius[3] * scale};
            ShapeRenderer.draw(context,
                    x * scale, y * scale, width * scale, height * scale,
                    scaled, fill, gradient, border * scale, borderColor, softness * scale);
        } finally {
            UiScale.pop(context);
        }
    }

    /** Пока шейдер не загружен, интерфейс не должен пропадать. */
    private static void fallback(DrawContext context, float x, float y, float width, float height, int color) {
        if (alpha(color) == 0) {
            return;
        }
        context.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
    }

    private static float[] corners(float radius) {
        return new float[]{radius, radius, radius, radius};
    }

    public static void texture(DrawContext context, Identifier texture, float x, float y, float size, int color) {
        texture(context, texture, x, y, size, size, color);
    }

    public static void texture(DrawContext context, Identifier texture, float x, float y,
                               float width, float height, int color) {
        texture(context, texture, x, y, width, height, ICON_SOURCE, ICON_SOURCE, color);
    }

    /**
     * Отрисовка текстуры произвольного размера. Рисуем в пространстве физических
     * пикселей, поэтому источник высокой плотности уменьшается линейной
     * фильтрацией, а не размазывается точечной выборкой логической сетки.
     */
    public static void texture(DrawContext context, Identifier texture, float x, float y, float width, float height,
                               int sourceWidth, int sourceHeight, int color) {
        if (alpha(color) == 0 || width <= 0 || height <= 0 || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }
        if (UiClip.cullsEverything()) {
            return;
        }

        UiTextures.ensureSmooth(texture);

        float scale = UiScale.push(context);
        try {
            context.getMatrices().translate(x * scale, y * scale, 0f);
            context.getMatrices().scale(width * scale / sourceWidth, height * scale / sourceHeight, 1f);
            context.drawTexture(RenderLayer::getGuiTextured, texture, 0, 0, 0f, 0f,
                    sourceWidth, sourceHeight, sourceWidth, sourceHeight, color);
        } finally {
            UiScale.pop(context);
        }
    }

    /** Обрезка задаётся в координатах раскладки, применяется в физических. */
    public static void pushClip(DrawContext context, float x, float y, float width, float height) {
        context.draw();

        // Область принадлежит экрану, а не месту вызова: координаты проводятся
        // через текущую матрицу, иначе обрезка внутри сдвинутого виджета
        // отрезала бы не там. Стек интерфейса содержит только перенос и
        // масштаб, поэтому достаточно диагонали и переноса.
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float left = x * matrix.m00() + matrix.m30();
        float top = y * matrix.m11() + matrix.m31();
        float right = (x + width) * matrix.m00() + matrix.m30();
        float bottom = (y + height) * matrix.m11() + matrix.m31();

        float scale = UiScale.factor();
        UiClip.push(left * scale, top * scale, (right - left) * scale, (bottom - top) * scale);

        // Ножницы оставляем как грубое отсечение: расширяем наружу, чтобы они
        // не срезали сглаживание, точную границу задаёт шейдер
        context.enableScissor(
                (int) Math.floor(left),
                (int) Math.floor(top),
                (int) Math.ceil(right),
                (int) Math.ceil(bottom));
    }

    public static void popClip(DrawContext context) {
        context.draw();
        context.disableScissor();
        UiClip.pop();
    }

    public static boolean hovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int alpha(int color) {
        return (color >>> 24) & 0xFF;
    }
}
