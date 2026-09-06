import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Предпросмотр рендера форм вне игры.
 *
 * Повторяет математику aspect_shape.fsh на CPU, чтобы качество краёв можно было
 * увидеть без запуска Minecraft. Одна единица здесь равна одному пикселю, как и
 * в игре после перевода отрисовки в пространство кадрового буфера.
 *
 * Запуск: javac -d out ShapePreview.java && java -cp out ShapePreview out.png
 */
public final class ShapePreview {

    record Shape(float x, float y, float w, float h, float[] radius,
                 int fill, int gradient, float border, int borderColor, float softness) {
    }

    public static void main(String[] args) throws Exception {
        int width = 900;
        int height = 420;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int background = 0xFF14141A;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, background);
            }
        }

        float[] r12 = {12, 12, 12, 12};
        float[] r8 = {8, 8, 8, 8};
        float[] pill = {1024, 1024, 1024, 1024};
        float[] mixed = {16, 2, 16, 2};

        Shape[] shapes = {
                // тень под карточкой
                new Shape(60, 56, 256, 69, r12, 0x66000000, 0x66000000, 0, 0, 14),
                // карточка модуля из макета
                new Shape(60, 48, 256, 69, r12, 0x9E111113, 0xB819191B, 1.0f, 0x29FFFFFF, 0),
                // переключатель 40x16
                new Shape(360, 60, 40, 16, pill, 0xFFFFFFFF, 0xFFFFFFFF, 0, 0, 0),
                new Shape(361.5f, 61.5f, 22, 13, pill, 0xFF080809, 0xFF080809, 0, 0, 0),
                // чекбокс 16x16 радиус 6
                new Shape(430, 60, 16, 16, new float[]{6, 6, 6, 6}, 0xFFFFFFFF, 0xFFFFFFFF, 0, 0, 0),
                // поле ввода
                new Shape(480, 48, 310, 40, r12, 0xB80B0B0D, 0xB80B0B0D, 1.0f, 0x0DFFFFFF, 0),
                // круглая кнопка
                new Shape(60, 160, 32, 32, pill, 0x9E111113, 0x9E111113, 1.0f, 0x29FFFFFF, 0),
                // дробная позиция: проверка, что край не прыгает на целый пиксель
                new Shape(120.37f, 160.62f, 32, 32, pill, 0x9E111113, 0x9E111113, 1.0f, 0x29FFFFFF, 0),
                new Shape(180.74f, 160.24f, 32, 32, pill, 0x9E111113, 0x9E111113, 1.0f, 0x29FFFFFF, 0),
                // разные радиусы по углам
                new Shape(260, 160, 120, 48, mixed, 0x9E111113, 0x9E111113, 1.0f, 0x29FFFFFF, 0),
                // тонкая полоса слайдера
                new Shape(60, 240, 200, 3, pill, 0x401A1A1A, 0x401A1A1A, 0, 0, 0),
                new Shape(60, 240, 120, 3, pill, 0xFFFFFFFF, 0xFFFFFFFF, 0, 0, 0),
                new Shape(175.5f, 237, 9, 9, pill, 0xFFFFFFFF, 0xFFFFFFFF, 0, 0, 0),
                // градиент
                new Shape(420, 160, 200, 48, r8, 0xFF2A2A33, 0xFF0B0B0D, 0, 0, 0),
                // крупная панель со скруглением 12
                new Shape(60, 290, 730, 90, r12, 0xAD0B0B0D, 0xAD0B0B0D, 1.0f, 0x0DFFFFFF, 0),
        };

        for (Shape shape : shapes) {
            render(image, shape);
        }

        ImageIO.write(image, "png", new File(args.length > 0 ? args[0] : "preview.png"));

        // Увеличенный фрагмент: край карточки и переключатель
        BufferedImage zoom = zoom(image, 44, 32, 200, 100, 4);
        ImageIO.write(zoom, "png", new File(args.length > 1 ? args[1] : "preview-zoom.png"));
        System.out.println("Готово");
    }

    private static void render(BufferedImage image, Shape shape) {
        float pad = Math.max(4f, shape.softness() * 3f + shape.border() + 4f);
        int minX = (int) Math.floor(shape.x() - pad);
        int minY = (int) Math.floor(shape.y() - pad);
        int maxX = (int) Math.ceil(shape.x() + shape.w() + pad);
        int maxY = (int) Math.ceil(shape.y() + shape.h() + pad);

        for (int py = Math.max(0, minY); py < Math.min(image.getHeight(), maxY); py++) {
            for (int px = Math.max(0, minX); px < Math.min(image.getWidth(), maxX); px++) {
                // центр пикселя, как gl_FragCoord
                float[] rgba = shade(px + 0.5f, py + 0.5f, shape);
                if (rgba[3] <= 0.0005f) {
                    continue;
                }
                image.setRGB(px, py, over(rgba, image.getRGB(px, py)));
            }
        }
    }

    /** Повторяет main() фрагментного шейдера. */
    private static float[] shade(float x, float y, Shape shape) {
        float halfW = shape.w() * 0.5f;
        float halfH = shape.h() * 0.5f;
        float pointX = x - (shape.x() + halfW);
        float pointY = y - (shape.y() + halfH);

        float dist = roundedBoxSdf(pointX, pointY, halfW, halfH, shape.radius());
        // на GPU это fwidth(dist); для корректного поля расстояния градиент равен 1
        float aa = 1.0f;

        float gradientT = clamp((y - shape.y()) / Math.max(shape.h(), 0.0001f), 0f, 1f);
        float[] base = mix(unpack(shape.fill()), unpack(shape.gradient()), gradientT);

        if (shape.softness() > 0f) {
            float shadow = 1f - smoothstep(-shape.softness(), shape.softness(), dist);
            return new float[]{base[0], base[1], base[2], base[3] * shadow * shadow};
        }

        float outer = 1f - smoothstep(-aa, aa, dist);
        float inner = 1f - smoothstep(-aa, aa, dist + shape.border());
        float ring = clamp(outer - inner, 0f, 1f);

        float[] edgeColor = unpack(shape.borderColor());
        float bodyA = base[3] * inner;
        float edgeA = edgeColor[3] * ring;

        float alpha = edgeA + bodyA * (1f - edgeA);
        if (alpha <= 0f) {
            return new float[]{0, 0, 0, 0};
        }
        float red = (edgeColor[0] * edgeA + base[0] * bodyA * (1f - edgeA)) / alpha;
        float green = (edgeColor[1] * edgeA + base[1] * bodyA * (1f - edgeA)) / alpha;
        float blue = (edgeColor[2] * edgeA + base[2] * bodyA * (1f - edgeA)) / alpha;
        return new float[]{red, green, blue, alpha};
    }

    private static float roundedBoxSdf(float px, float py, float halfW, float halfH, float[] radius) {
        float corner = px > 0f
                ? (py > 0f ? radius[2] : radius[1])
                : (py > 0f ? radius[3] : radius[0]);
        corner = Math.min(corner, Math.min(halfW, halfH));

        float qx = Math.abs(px) - halfW + corner;
        float qy = Math.abs(py) - halfH + corner;
        float outside = (float) Math.hypot(Math.max(qx, 0f), Math.max(qy, 0f));
        return Math.min(Math.max(qx, qy), 0f) + outside - corner;
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = clamp((value - edge0) / (edge1 - edge0), 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    private static float[] unpack(int argb) {
        return new float[]{
                ((argb >> 16) & 0xFF) / 255f,
                ((argb >> 8) & 0xFF) / 255f,
                (argb & 0xFF) / 255f,
                ((argb >>> 24) & 0xFF) / 255f};
    }

    private static float[] mix(float[] a, float[] b, float t) {
        return new float[]{
                a[0] + (b[0] - a[0]) * t,
                a[1] + (b[1] - a[1]) * t,
                a[2] + (b[2] - a[2]) * t,
                a[3] + (b[3] - a[3]) * t};
    }

    private static int over(float[] src, int dstRgb) {
        float dr = ((dstRgb >> 16) & 0xFF) / 255f;
        float dg = ((dstRgb >> 8) & 0xFF) / 255f;
        float db = (dstRgb & 0xFF) / 255f;
        float a = src[3];

        int r = Math.round(clamp(src[0] * a + dr * (1 - a), 0f, 1f) * 255f);
        int g = Math.round(clamp(src[1] * a + dg * (1 - a), 0f, 1f) * 255f);
        int b = Math.round(clamp(src[2] * a + db * (1 - a), 0f, 1f) * 255f);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static BufferedImage zoom(BufferedImage source, int x, int y, int w, int h, int factor) {
        BufferedImage result = new BufferedImage(w * factor, h * factor, BufferedImage.TYPE_INT_RGB);
        for (int py = 0; py < h * factor; py++) {
            for (int px = 0; px < w * factor; px++) {
                result.setRGB(px, py, source.getRGB(x + px / factor, y + py / factor));
            }
        }
        return result;
    }
}
