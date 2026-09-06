import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Сравнение старого и нового рендера скруглений.
 *
 * Слева — прежний путь: построчная растеризация в целые логические пиксели
 * через DrawContext.fill с последующим увеличением до плотности буфера
 * (GUI Scale 3 — то, что Minecraft выбирает автоматически на 1080p).
 * Справа — тот же прямоугольник через поле расстояния в физических пикселях.
 *
 * Запуск: javac -d out ComparePreview.java && java -cp out ComparePreview out.png
 */
public final class ComparePreview {

    private static final int GUI_SCALE = 3;

    public static void main(String[] args) throws Exception {
        int cardW = 84;
        int cardH = 40;
        float radius = 12;

        int width = cardW * GUI_SCALE * 2 + 60;
        int height = cardH * GUI_SCALE + 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        fill(image, 0xFF14141A);

        drawLegacy(image, 20, 20, cardW, cardH, radius);
        drawSdf(image, 40 + cardW * GUI_SCALE, 20, cardW * GUI_SCALE, cardH * GUI_SCALE, radius * GUI_SCALE);

        ImageIO.write(image, "png", new File(args.length > 0 ? args[0] : "compare.png"));

        BufferedImage zoom = zoom(image, 14, 14, 60, 60, 6);
        ImageIO.write(zoom, "png", new File(args.length > 1 ? args[1] : "compare-zoom-old.png"));

        BufferedImage zoomNew = zoom(image, 34 + cardW * GUI_SCALE, 14, 60, 60, 6);
        ImageIO.write(zoomNew, "png", new File(args.length > 2 ? args[2] : "compare-zoom-new.png"));
        System.out.println("Готово");
    }

    /**
     * Прежний алгоритм: покрытие считается на строку логического пикселя,
     * затем результат увеличивается в GUI_SCALE раз без пересчёта.
     */
    private static void drawLegacy(BufferedImage target, int originX, int originY, int w, int h, float radius) {
        BufferedImage logical = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        int rows = (int) Math.ceil(radius);
        int fill = 0x9E111113;

        for (int y = rows; y < h - rows; y++) {
            for (int x = 0; x < w; x++) {
                logical.setRGB(x, y, fill);
            }
        }

        for (int row = 0; row < rows; row++) {
            float centerOffset = radius - (row + 0.5f);
            float inset = radius - (float) Math.sqrt(Math.max(0f, radius * radius - centerOffset * centerOffset));
            int solid = (int) Math.ceil(inset);
            float coverage = 1f - (solid - inset);

            int innerLeft = solid;
            int innerRight = w - solid;
            if (innerRight <= innerLeft) {
                continue;
            }

            for (int x = innerLeft; x < innerRight; x++) {
                logical.setRGB(x, row, fill);
                logical.setRGB(x, h - row - 1, fill);
            }

            if (coverage > 0.02f && solid > 0) {
                int edge = withAlpha(fill, coverage);
                logical.setRGB(innerLeft - 1, row, edge);
                logical.setRGB(innerRight, row, edge);
                logical.setRGB(innerLeft - 1, h - row - 1, edge);
                logical.setRGB(innerRight, h - row - 1, edge);
            }
        }

        // увеличение до физического разрешения — точечное, как у GUI Minecraft
        for (int y = 0; y < h * GUI_SCALE; y++) {
            for (int x = 0; x < w * GUI_SCALE; x++) {
                int argb = logical.getRGB(x / GUI_SCALE, y / GUI_SCALE);
                blend(target, originX + x, originY + y, argb);
            }
        }
    }

    /** Новый путь: поле расстояния считается прямо в физических пикселях. */
    private static void drawSdf(BufferedImage target, int originX, int originY, int w, int h, float radius) {
        for (int y = -2; y < h + 2; y++) {
            for (int x = -2; x < w + 2; x++) {
                float px = x + 0.5f - w * 0.5f;
                float py = y + 0.5f - h * 0.5f;

                float corner = Math.min(radius, Math.min(w * 0.5f, h * 0.5f));
                float qx = Math.abs(px) - w * 0.5f + corner;
                float qy = Math.abs(py) - h * 0.5f + corner;
                float dist = Math.min(Math.max(qx, qy), 0f)
                        + (float) Math.hypot(Math.max(qx, 0f), Math.max(qy, 0f)) - corner;

                float alpha = 1f - smoothstep(-1f, 1f, dist);
                if (alpha <= 0.002f) {
                    continue;
                }
                blend(target, originX + x, originY + y, withAlpha(0x9E111113, alpha));
            }
        }
    }

    private static int withAlpha(int argb, float factor) {
        int a = Math.round(((argb >>> 24) & 0xFF) * clamp(factor));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    private static void blend(BufferedImage image, int x, int y, int argb) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
            return;
        }
        float a = ((argb >>> 24) & 0xFF) / 255f;
        if (a <= 0f) {
            return;
        }

        int dst = image.getRGB(x, y);
        int r = Math.round((((argb >> 16) & 0xFF) * a + ((dst >> 16) & 0xFF) * (1 - a)));
        int g = Math.round((((argb >> 8) & 0xFF) * a + ((dst >> 8) & 0xFF) * (1 - a)));
        int b = Math.round(((argb & 0xFF) * a + (dst & 0xFF) * (1 - a)));
        image.setRGB(x, y, 0xFF000000 | (r << 16) | (g << 8) | b);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = clamp((value - edge0) / (edge1 - edge0));
        return t * t * (3f - 2f * t);
    }

    private static float clamp(float value) {
        return value < 0f ? 0f : Math.min(value, 1f);
    }

    private static void fill(BufferedImage image, int rgb) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, rgb);
            }
        }
    }

    private static BufferedImage zoom(BufferedImage source, int x, int y, int w, int h, int factor) {
        BufferedImage result = new BufferedImage(w * factor, h * factor, BufferedImage.TYPE_INT_RGB);
        for (int py = 0; py < h * factor; py++) {
            for (int px = 0; px < w * factor; px++) {
                int sx = Math.min(source.getWidth() - 1, Math.max(0, x + px / factor));
                int sy = Math.min(source.getHeight() - 1, Math.max(0, y + py / factor));
                result.setRGB(px, py, source.getRGB(sx, sy));
            }
        }
        return result;
    }
}
