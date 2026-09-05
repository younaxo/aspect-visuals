package su.aspectvisuals.client.ui.render;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import su.aspectvisuals.client.AspectVisuals;

/**
 * Собственные core-шейдеры интерфейса.
 *
 * Программы живут между перезагрузками ресурсов не сами по себе: Minecraft
 * пересоздаёт их и вызывает колбэк заново, поэтому ссылку каждый раз обновляем,
 * а рендер до первой загрузки умеет работать без шейдера.
 */
public final class AspectShaders {
    private static ShaderProgram shape;

    private AspectShaders() {
    }

    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context ->
                context.register(AspectVisuals.id("aspect_shape"), VertexFormats.POSITION_COLOR,
                        program -> shape = program));
    }

    public static ShaderProgram shape() {
        return shape;
    }
}
