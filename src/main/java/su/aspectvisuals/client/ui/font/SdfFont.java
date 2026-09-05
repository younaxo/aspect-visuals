package su.aspectvisuals.client.ui.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.ui.render.AspectShaders;
import su.aspectvisuals.client.ui.render.UiClip;
import su.aspectvisuals.client.ui.render.UiTextures;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Начертание, нарисованное через знаковое поле расстояния.
 *
 * Растровый атлас фиксированного размера привязывает качество к кеглю: любой
 * GUI Scale больше единицы растягивает готовую картинку глифа. Здесь в атласе
 * лежит расстояние до контура, а край восстанавливается в шейдере, поэтому
 * текст остаётся резким на любом размере и позиционируется дробно.
 *
 * Метрики хранятся в долях кегельной площадки, поэтому один и тот же атлас
 * обслуживает все размеры интерфейса.
 */
public final class SdfFont {

    private record Glyph(int col, int row, float advance, float left, float top, float width, float height) {
    }

    private final Identifier atlas;
    private final Identifier metrics;
    private final Map<Integer, Glyph> glyphs = new HashMap<>();

    private int columns = 16;
    private int rows = 1;
    private float cellEm = 1f;
    private float spread = 0.1f;
    private float ascent = 0.97f;
    private float lineHeight = 1.21f;
    private boolean loaded;
    private boolean failed;

    SdfFont(String name) {
        this.atlas = AspectVisuals.id("textures/font/" + name + "_sdf.png");
        this.metrics = AspectVisuals.id("font/" + name + "_sdf.json");
    }

    public void invalidate() {
        loaded = false;
        failed = false;
        glyphs.clear();
    }

    private boolean ensureLoaded() {
        if (loaded) {
            return true;
        }
        if (failed) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getResourceManager() == null) {
            return false;
        }

        Optional<Resource> resource = client.getResourceManager().getResource(metrics);
        if (resource.isEmpty()) {
            AspectVisuals.LOGGER.error("Метрики шрифта {} не найдены", metrics);
            failed = true;
            return false;
        }

        try (BufferedReader reader = resource.get().getReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            columns = json.get("columns").getAsInt();
            rows = json.get("rows").getAsInt();
            cellEm = json.get("cellEm").getAsFloat();
            spread = json.get("spread").getAsFloat();
            ascent = json.get("ascent").getAsFloat();
            lineHeight = json.get("lineHeight").getAsFloat();

            for (JsonElement element : json.getAsJsonArray("glyphs")) {
                JsonObject glyph = element.getAsJsonObject();
                glyphs.put(glyph.get("code").getAsInt(), new Glyph(
                        glyph.get("col").getAsInt(),
                        glyph.get("row").getAsInt(),
                        glyph.get("advance").getAsFloat(),
                        glyph.get("left").getAsFloat(),
                        glyph.get("top").getAsFloat(),
                        glyph.get("width").getAsFloat(),
                        glyph.get("height").getAsFloat()));
            }
            loaded = true;
            return true;
        } catch (IOException | RuntimeException error) {
            AspectVisuals.LOGGER.error("Не удалось прочитать метрики шрифта: {}", error.getMessage());
            failed = true;
            return false;
        }
    }

    public boolean ready() {
        return ensureLoaded() && AspectShaders.text() != null;
    }

    public float lineHeight(float size) {
        ensureLoaded();
        return lineHeight * size;
    }

    public float ascent(float size) {
        ensureLoaded();
        return ascent * size;
    }

    public float width(String text, float size) {
        if (text == null || text.isEmpty() || !ensureLoaded()) {
            return 0f;
        }

        float total = 0f;
        for (int i = 0; i < text.length(); i++) {
            Glyph glyph = glyphs.get((int) text.charAt(i));
            if (glyph != null) {
                total += glyph.advance() * size;
            }
        }
        return total;
    }

    /**
     * Координаты в физических пикселях, {@code y} — верх строки.
     * Вызывающий уже перевёл контекст в пиксельное пространство.
     */
    public void draw(DrawContext context, String text, float x, float y, float size, int color) {
        if (text == null || text.isEmpty() || !ready()) {
            return;
        }

        ShaderProgram program = AspectShaders.text();
        UiTextures.ensureSmooth(atlas);
        context.draw();

        setVec2(program, "AspectTextParams", spread * size, 0f);
        float[] clip = UiClip.current();
        setVec4(program, "AspectClip", clip[0], clip[1], clip[2], clip[3]);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, atlas);
        RenderSystem.setShader(program);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        float baseline = y + ascent * size;
        float pen = x;
        boolean any = false;

        for (int i = 0; i < text.length(); i++) {
            Glyph glyph = glyphs.get((int) text.charAt(i));
            if (glyph == null) {
                continue;
            }
            if (glyph.width() > 0f && glyph.height() > 0f) {
                appendGlyph(buffer, matrix, glyph, pen, baseline, size, color);
                any = true;
            }
            pen += glyph.advance() * size;
        }

        if (any) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } else {
            buffer.end();
        }
        RenderSystem.disableBlend();
    }

    private void appendGlyph(BufferBuilder buffer, Matrix4f matrix, Glyph glyph,
                             float pen, float baseline, float size, int color) {
        // Клетка атласа центрирована по ограничивающему прямоугольнику глифа,
        // поэтому квад строится вокруг его центра, а не вокруг базовой точки
        float centerX = pen + (glyph.left() + glyph.width() * 0.5f) * size;
        float centerY = baseline + (glyph.top() + glyph.height() * 0.5f) * size;

        float quad = cellEm * size;
        float left = centerX - quad * 0.5f;
        float top = centerY - quad * 0.5f;
        float right = left + quad;
        float bottom = top + quad;

        float u0 = glyph.col() / (float) columns;
        float v0 = glyph.row() / (float) rows;
        float u1 = (glyph.col() + 1) / (float) columns;
        float v1 = (glyph.row() + 1) / (float) rows;

        buffer.vertex(matrix, left, top, 0f).texture(u0, v0).color(color);
        buffer.vertex(matrix, left, bottom, 0f).texture(u0, v1).color(color);
        buffer.vertex(matrix, right, bottom, 0f).texture(u1, v1).color(color);
        buffer.vertex(matrix, right, top, 0f).texture(u1, v0).color(color);
    }

    private static void setVec2(ShaderProgram program, String name, float a, float b) {
        GlUniform uniform = program.getUniform(name);
        if (uniform != null) {
            uniform.set(a, b);
        }
    }

    private static void setVec4(ShaderProgram program, String name, float a, float b, float c, float d) {
        GlUniform uniform = program.getUniform(name);
        if (uniform != null) {
            uniform.set(a, b, c, d);
        }
    }
}
