package su.aspectvisuals.client.ui.render;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Область обрезки в физических пикселях.
 *
 * Ножницы OpenGL режут по целым логическим пикселям, поэтому на границе
 * прокручиваемых списков срезалось сглаживание. Прямоугольник передаётся
 * в шейдер и применяется аналитически, вместе с остальным сглаживанием.
 */
public final class UiClip {
    private static final float[] NONE = {0f, 0f, 0f, 0f};

    private static final Deque<float[]> stack = new ArrayDeque<>();

    private UiClip() {
    }

    /** Координаты в физических пикселях. Вложенные области пересекаются. */
    public static void push(float x, float y, float width, float height) {
        float[] rect = {x, y, Math.max(0f, width), Math.max(0f, height)};

        float[] parent = stack.peek();
        if (parent != null && parent[2] > 0f && parent[3] > 0f) {
            float left = Math.max(rect[0], parent[0]);
            float top = Math.max(rect[1], parent[1]);
            float right = Math.min(rect[0] + rect[2], parent[0] + parent[2]);
            float bottom = Math.min(rect[1] + rect[3], parent[1] + parent[3]);
            rect = new float[]{left, top, Math.max(0f, right - left), Math.max(0f, bottom - top)};
        }

        stack.push(rect);
    }

    public static void pop() {
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    public static float[] current() {
        float[] rect = stack.peek();
        return rect == null ? NONE : rect;
    }

    public static boolean active() {
        float[] rect = current();
        return rect[2] > 0f && rect[3] > 0f;
    }

    /** Пуст ли прямоугольник настолько, что рисовать внутри нечего. */
    public static boolean cullsEverything() {
        float[] rect = stack.peek();
        return rect != null && (rect[2] <= 0f || rect[3] <= 0f);
    }

    public static void reset() {
        stack.clear();
    }
}
