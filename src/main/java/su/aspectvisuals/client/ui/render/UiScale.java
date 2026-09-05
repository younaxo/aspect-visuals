package su.aspectvisuals.client.ui.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;

/**
 * Перевод раскладки в физические пиксели.
 *
 * Раскладка считается в единицах макета: одна единица равна одному логическому
 * пикселю Minecraft, поэтому размеры из Figma переносятся один к одному и
 * настройка GUI Scale продолжает работать привычно.
 *
 * Растеризация же должна идти по реальной плотности кадрового буфера. Иначе
 * при GUI Scale 3 одна единица превращается в квадрат 3x3, и любое сглаживание
 * считается втрое грубее, чем позволяет экран. Поэтому перед отрисовкой матрица
 * ужимается в {@code 1 / scale}, а координаты умножаются на {@code scale}:
 * на экране размер тот же, но одна единица отрисовки равна одному пикселю.
 */
public final class UiScale {
    private UiScale() {
    }

    /** Сколько физических пикселей приходится на единицу раскладки. */
    public static float factor() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return 1f;
        }

        Window window = client.getWindow();
        if (window == null) {
            return 1f;
        }

        int scaled = window.getScaledWidth();
        int framebuffer = window.getFramebufferWidth();
        if (scaled <= 0 || framebuffer <= 0) {
            return 1f;
        }

        // Считаем отношение напрямую: getScaleFactor округляется до целого,
        // а реальная плотность буфера при системном масштабировании дробная
        return (float) framebuffer / scaled;
    }

    public static float toPixels(float layoutUnits) {
        return layoutUnits * factor();
    }

    public static float toLayout(float pixels) {
        return pixels / factor();
    }

    /**
     * Переводит контекст в пространство физических пикселей и возвращает
     * применённый масштаб. Координаты после этого задаются в пикселях.
     * Область закрывается через {@link #pop(DrawContext)}.
     */
    public static float push(DrawContext context) {
        float scale = factor();
        context.getMatrices().push();
        context.getMatrices().scale(1f / scale, 1f / scale, 1f);
        return scale;
    }

    public static void pop(DrawContext context) {
        context.getMatrices().pop();
    }
}
