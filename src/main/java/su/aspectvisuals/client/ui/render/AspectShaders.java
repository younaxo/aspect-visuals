package su.aspectvisuals.client.ui.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import su.aspectvisuals.client.AspectVisuals;

import java.util.HashSet;
import java.util.Set;

/**
 * Собственные core-шейдеры интерфейса.
 *
 * Программа описывается ключом: идентификатор описания, формат вершин и набор
 * директив препроцессора. Загрузчик компилирует её при первом обращении и
 * пересобирает после перезагрузки ресурсов, поэтому ссылку не кешируем —
 * иначе после смены набора ресурсов остался бы указатель на удалённую
 * программу. Пока загрузчика нет (ранняя инициализация) возвращается null,
 * и рендер уходит на запасной путь.
 */
public final class AspectShaders {
    private static final ShaderProgramKey SHAPE = new ShaderProgramKey(
            AspectVisuals.id("core/aspect_shape"),
            VertexFormats.POSITION_COLOR,
            Defines.EMPTY);

    private static final ShaderProgramKey TEXT = new ShaderProgramKey(
            AspectVisuals.id("core/aspect_text"),
            VertexFormats.POSITION_TEXTURE_COLOR,
            Defines.EMPTY);

    /** Ключи, о недоступности которых уже сообщено: иначе запись каждый кадр. */
    private static final Set<ShaderProgramKey> reported = new HashSet<>();

    private AspectShaders() {
    }

    public static ShaderProgram shape() {
        return program(SHAPE);
    }

    public static ShaderProgram text() {
        return program(TEXT);
    }

    /**
     * Униформы задаются по имени: программа могла не объявить её, если шейдер
     * заменён набором ресурсов, и тогда запись просто пропускается.
     */
    public static void setVec4(ShaderProgram program, String name, float x, float y, float z, float w) {
        GlUniform uniform = program.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y, z, w);
        }
    }

    public static void setVec2(ShaderProgram program, String name, float x, float y) {
        GlUniform uniform = program.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y);
        }
    }

    /**
     * Размер буфера кадра. Шейдеры считают положение по gl_FragCoord, а не по
     * вершине: вершина приходит уже умноженной на матрицу интерфейса, её
     * единицы зависят от GUI Scale и от сдвигов в стеке матриц, тогда как
     * фигура и область обрезки заданы в пикселях буфера. Высота нужна, чтобы
     * перевернуть ось: у gl_FragCoord начало внизу, у интерфейса — вверху.
     */
    public static void setScreen(ShaderProgram program) {
        Window window = MinecraftClient.getInstance().getWindow();
        setVec2(program, "AspectScreen", window.getFramebufferWidth(), window.getFramebufferHeight());
    }

    /** Цвет ARGB раскладывается в нормированный RGBA. */
    public static void setColor(ShaderProgram program, String name, int argb) {
        setVec4(program, name,
                ((argb >> 16) & 0xFF) / 255f,
                ((argb >> 8) & 0xFF) / 255f,
                (argb & 0xFF) / 255f,
                ((argb >>> 24) & 0xFF) / 255f);
    }

    private static ShaderProgram program(ShaderProgramKey key) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }
        ShaderLoader loader = client.getShaderLoader();
        if (loader == null) {
            return null;
        }
        // Загрузчик сам держит кеш и сам сообщает об ошибке компиляции
        ShaderProgram program = loader.getOrCreateProgram(key);
        if (program == null) {
            // Без этого отсутствие программы выглядит как «интерфейс просто
            // рисуется хуже»: отрисовка молча уходит на запасной путь
            if (reported.add(key)) {
                AspectVisuals.LOGGER.error(
                        "Шейдер {} недоступен: интерфейс рисуется запасным путём без сглаживания",
                        key.configId());
            }
        } else {
            reported.remove(key);
        }
        return program;
    }
}
