import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Предпросмотр текста из SDF-атласа вне игры.
 *
 * Повторяет выборку и сглаживание фрагментного шейдера на CPU, чтобы качество
 * глифов можно было оценить без запуска Minecraft.
 *
 * Запуск: java -cp out TextPreview <atlas.png> <atlas.json> <out.png> <zoom.png>
 */
public final class TextPreview {

    record Glyph(int col, int row, double advance, double left, double top, double width, double height) {
    }

    private static BufferedImage atlas;
    private static int cell;
    private static double cellEm;
    private static double spread;
    private static double ascent;
    private static final Map<Integer, Glyph> glyphs = new HashMap<>();

    public static void main(String[] args) throws Exception {
        atlas = ImageIO.read(new File(args[0]));
        parse(Files.readString(Path.of(args[1]), StandardCharsets.UTF_8));

        int width = 900;
        int height = 300;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, 0xFF14141A);
            }
        }

        // Размеры соответствуют интерфейсу: 12 единиц макета при GUI Scale 1..4
        double[] sizes = {12, 18, 24, 36, 48};
        double y = 34;
        for (double size : sizes) {
            drawText(image, "Aspect Visuals — Счётчик FPS 1234", 24, y, size, 0xF0FFFFFF);
            y += size * 1.5;
        }

        drawText(image, "Нажатия клавиш · Текущий биом · Подписка 37 дней", 24, 268, 14, 0x94FFFFFF);

        ImageIO.write(image, "png", new File(args[2]));
        ImageIO.write(zoom(image, 22, 20, 150, 30, 6), "png", new File(args[3]));
        System.out.println("Готово");
    }

    private static void drawText(BufferedImage image, String text, double x, double baseline,
                                 double size, int color) {
        double pen = x;
        for (int i = 0; i < text.length(); i++) {
            Glyph glyph = glyphs.get((int) text.charAt(i));
            if (glyph == null) {
                continue;
            }
            if (glyph.width() > 0) {
                drawGlyph(image, glyph, pen, baseline, size, color);
            }
            pen += glyph.advance() * size;
        }
    }

    private static void drawGlyph(BufferedImage image, Glyph glyph, double pen, double baseline,
                                  double size, int color) {
        // Клетка центрирована по ограничивающему прямоугольнику глифа
        double boxX = pen + glyph.left() * size;
        double boxY = baseline + glyph.top() * size;
        double centerX = boxX + glyph.width() * size * 0.5;
        double centerY = boxY + glyph.height() * size * 0.5;

        double quad = cellEm * size;
        double quadX = centerX - quad * 0.5;
        double quadY = centerY - quad * 0.5;

        int minX = (int) Math.floor(quadX);
        int minY = (int) Math.floor(quadY);
        int maxX = (int) Math.ceil(quadX + quad);
        int maxY = (int) Math.ceil(quadY + quad);

        // Расстояние восстанавливается в пикселях экрана, поэтому ширина
        // сглаживания постоянна и не зависит от кегля
        double spreadPixels = spread * size;

        for (int py = Math.max(0, minY); py < Math.min(image.getHeight(), maxY); py++) {
            for (int px = Math.max(0, minX); px < Math.min(image.getWidth(), maxX); px++) {
                double u = (px + 0.5 - quadX) / quad;
                double v = (py + 0.5 - quadY) / quad;
                if (u < 0 || u > 1 || v < 0 || v > 1) {
                    continue;
                }

                double sample = sampleAtlas(glyph, u, v);
                double distance = (sample - 0.5) * 2.0 * spreadPixels;
                double alpha = clamp(distance / 1.0 + 0.5, 0, 1);
                if (alpha <= 0.002) {
                    continue;
                }

                blend(image, px, py, color, alpha);
            }
        }
    }

    /** Билинейная выборка клетки атласа. */
    private static double sampleAtlas(Glyph glyph, double u, double v) {
        double tx = glyph.col() * cell + u * cell - 0.5;
        double ty = glyph.row() * cell + v * cell - 0.5;

        int x0 = (int) Math.floor(tx);
        int y0 = (int) Math.floor(ty);
        double fx = tx - x0;
        double fy = ty - y0;

        double s00 = texel(x0, y0);
        double s10 = texel(x0 + 1, y0);
        double s01 = texel(x0, y0 + 1);
        double s11 = texel(x0 + 1, y0 + 1);

        return (s00 * (1 - fx) + s10 * fx) * (1 - fy) + (s01 * (1 - fx) + s11 * fx) * fy;
    }

    private static double texel(int x, int y) {
        if (x < 0 || y < 0 || x >= atlas.getWidth() || y >= atlas.getHeight()) {
            return 0.0;
        }
        return (atlas.getRGB(x, y) & 0xFF) / 255.0;
    }

    private static void blend(BufferedImage image, int x, int y, int color, double alpha) {
        double a = alpha * (((color >>> 24) & 0xFF) / 255.0);
        int dst = image.getRGB(x, y);
        int r = (int) Math.round(((color >> 16) & 0xFF) * a + ((dst >> 16) & 0xFF) * (1 - a));
        int g = (int) Math.round(((color >> 8) & 0xFF) * a + ((dst >> 8) & 0xFF) * (1 - a));
        int b = (int) Math.round((color & 0xFF) * a + (dst & 0xFF) * (1 - a));
        image.setRGB(x, y, 0xFF000000 | (r << 16) | (g << 8) | b);
    }

    private static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    private static void parse(String json) {
        cell = (int) number(json, "cell");
        cellEm = number(json, "cellEm");
        spread = number(json, "spread");
        ascent = number(json, "ascent");

        Pattern pattern = Pattern.compile(
                "\\{ \"code\": (\\d+), \"col\": (\\d+), \"row\": (\\d+), \"advance\": ([-\\d.]+),"
                        + " \"left\": ([-\\d.]+), \"top\": ([-\\d.]+), \"width\": ([-\\d.]+), \"height\": ([-\\d.]+) }");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            glyphs.put(Integer.parseInt(matcher.group(1)), new Glyph(
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    Double.parseDouble(matcher.group(4)),
                    Double.parseDouble(matcher.group(5)),
                    Double.parseDouble(matcher.group(6)),
                    Double.parseDouble(matcher.group(7)),
                    Double.parseDouble(matcher.group(8))));
        }
    }

    private static double number(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\": ([-\\d.]+)").matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;
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

    private TextPreview() {
    }
}
