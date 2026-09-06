package su.aspectvisuals.client.ui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

/**
 * Отрисовка форм интерфейса через знаковое поле расстояния.
 *
 * Заливка, граница, скругления и тень считаются в одном фрагментном шейдере,
 * поэтому край сглаживается по реальной плотности пикселей, а не по сетке
 * логических координат. Геометрия — один прямоугольник с запасом под
 * сглаживание и растушёвку.
 */
public final class ShapeRenderer {
    /** Запас вокруг фигуры, чтобы сглаживание и тень не обрезались квадом. */
    private static final float PADDING = 2f;

    private ShapeRenderer() {
    }

    public static boolean ready() {
        return AspectShaders.shape() != null;
    }

    /**
     * Все координаты в физических пикселях.
     *
     * @param radius     радиусы углов: левый верхний, правый верхний, правый нижний, левый нижний
     * @param fill       цвет заливки ARGB
     * @param gradient   цвет нижней точки градиента ARGB; для однотонной заливки равен fill
     * @param border     толщина границы; 0 — без границы
     * @param borderArgb цвет границы ARGB
     * @param softness   растушёвка тени; больше нуля переводит фигуру в режим тени
     */
    public static void draw(DrawContext context, float x, float y, float width, float height,
                            float[] radius, int fill, int gradient,
                            float border, int borderArgb, float softness) {
        ShaderProgram program = AspectShaders.shape();
        if (program == null || width <= 0 || height <= 0) {
            return;
        }

        // Ванильная геометрия копится в общем буфере: без сброса наши формы
        // окажутся не в том порядке относительно текста и иконок
        context.draw();

        AspectShaders.setVec4(program, "AspectRect", x, y, width, height);
        AspectShaders.setVec4(program, "AspectRadius", radius[0], radius[1], radius[2], radius[3]);
        AspectShaders.setColor(program, "AspectBorderColor", borderArgb);
        AspectShaders.setColor(program, "AspectGradient", gradient);
        AspectShaders.setVec2(program, "AspectParams", border, softness);

        float[] clip = UiClip.current();
        AspectShaders.setVec4(program, "AspectClip", clip[0], clip[1], clip[2], clip[3]);
        AspectShaders.setScreen(program);

        float pad = PADDING + border + softness * 3f;
        float left = x - pad;
        float top = y - pad;
        float right = x + width + pad;
        float bottom = y + height + pad;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(program);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        // Координата фигуры кладётся в атрибут вершины: матрица её не трогает,
        // поэтому сдвиг и масштаб виджета HUD не сбивают поле расстояния
        buffer.vertex(matrix, left, top, 0f).texture(left, top).color(fill);
        buffer.vertex(matrix, left, bottom, 0f).texture(left, bottom).color(fill);
        buffer.vertex(matrix, right, bottom, 0f).texture(right, bottom).color(fill);
        buffer.vertex(matrix, right, top, 0f).texture(right, top).color(fill);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }
}
