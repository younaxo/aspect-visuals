import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Сборка SDF-атласа шрифта для интерфейса.
 *
 * Растровый атлас фиксированного размера — главная причина, по которой текст
 * выглядел как увеличенная картинка: при любом GUI Scale больше единицы глиф
 * растягивался. Знаковое поле расстояния снимает привязку к размеру: в шейдере
 * край восстанавливается по расстоянию и сглаживается по производной.
 *
 * Поле считается точно по контуру глифа, а не по растру: величина — минимальное
 * расстояние до отрезков сплющенного контура, знак — попадание внутрь по
 * правилу ненулевого обхода. Так сохраняются тонкие штрихи и углы.
 *
 * Взят одноканальный SDF, а не MSDF: многоканальное поле выигрывает на очень
 * крупном тексте, где важны идеально острые углы, а интерфейс печатает
 * 11-14 единиц макета, то есть до полусотни физических пикселей. Точность
 * одноканального поля там избыточна, а пайплайн заметно надёжнее.
 *
 * Запуск: javac -d out SdfAtlas.java && java -cp out SdfAtlas <ttf> <png> <json>
 */
public final class SdfAtlas {

    /** Размер клетки атласа в текселях. */
    private static final int CELL = 64;
    /**
     * Высота кегельной площадки в текселях. Подобрана так, чтобы самый крупный
     * глиф вместе с полем расстояния помещался в клетку: 36 + 2 * 6 = 48 из 64.
     */
    private static final double EM = 36.0;
    /** Ширина поля расстояния в текселях. */
    private static final double SPREAD = 6.0;
    private static final int COLUMNS = 16;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Использование: SdfAtlas <ttf> <png> <json>");
            System.exit(1);
        }

        Font font = Font.createFont(Font.TRUETYPE_FONT, new File(args[0])).deriveFont((float) EM);
        int[] codepoints = charset();

        FontRenderContext frc = new FontRenderContext(null, true, true);
        LineMetrics metrics = font.getLineMetrics("Hxy", frc);

        int rows = (codepoints.length + COLUMNS - 1) / COLUMNS;
        int atlasWidth = COLUMNS * CELL;
        int atlasHeight = rows * CELL;
        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);

        Map<Integer, int[]> placement = new LinkedHashMap<>();
        StringBuilder glyphs = new StringBuilder();

        for (int index = 0; index < codepoints.length; index++) {
            int codepoint = codepoints[index];
            int col = index % COLUMNS;
            int row = index / COLUMNS;

            GlyphVector vector = font.createGlyphVector(frc, new String(Character.toChars(codepoint)));
            double advance = vector.getGlyphMetrics(0).getAdvanceX();

            Path2D outline = new Path2D.Double(vector.getGlyphOutline(0));
            Rectangle2D bounds = outline.getBounds2D();

            if (!bounds.isEmpty()) {
                // Клетку центрируем по ограничивающему прямоугольнику глифа,
                // а не по базовой линии: иначе выносные элементы прописных
                // и подстрочные хвосты выходят за пределы клетки
                AffineTransform toCell = AffineTransform.getTranslateInstance(
                        col * CELL + CELL / 2.0 - bounds.getCenterX(),
                        row * CELL + CELL / 2.0 - bounds.getCenterY());
                Path2D placed = new Path2D.Double(outline);
                placed.transform(toCell);
                rasterize(atlas, placed, col * CELL, row * CELL);

                if (bounds.getWidth() + 2 * SPREAD > CELL || bounds.getHeight() + 2 * SPREAD > CELL) {
                    System.err.printf("Глиф U+%04X не помещается в клетку: %.1fx%.1f%n",
                            codepoint, bounds.getWidth(), bounds.getHeight());
                }
            }

            placement.put(codepoint, new int[]{col, row});
            glyphs.append(String.format(
                    "    { \"code\": %d, \"col\": %d, \"row\": %d, \"advance\": %.4f,"
                            + " \"left\": %.4f, \"top\": %.4f, \"width\": %.4f, \"height\": %.4f }%s%n",
                    codepoint, col, row, advance / EM,
                    bounds.isEmpty() ? 0.0 : bounds.getMinX() / EM,
                    bounds.isEmpty() ? 0.0 : bounds.getMinY() / EM,
                    bounds.isEmpty() ? 0.0 : bounds.getWidth() / EM,
                    bounds.isEmpty() ? 0.0 : bounds.getHeight() / EM,
                    index == codepoints.length - 1 ? "" : ","));
        }

        ImageIO.write(atlas, "png", new File(args[1]));

        String json = "{\n"
                + String.format("  \"cell\": %d,%n", CELL)
                + String.format("  \"columns\": %d,%n", COLUMNS)
                + String.format("  \"rows\": %d,%n", rows)
                + String.format("  \"cellEm\": %.4f,%n", CELL / EM)
                + String.format("  \"spread\": %.4f,%n", SPREAD / EM)
                + String.format("  \"ascent\": %.4f,%n", metrics.getAscent() / EM)
                + String.format("  \"descent\": %.4f,%n", metrics.getDescent() / EM)
                + String.format("  \"lineHeight\": %.4f,%n", metrics.getHeight() / EM)
                + "  \"glyphs\": [\n"
                + glyphs
                + "  ]\n}\n";
        Files.writeString(Path.of(args[2]), json, StandardCharsets.UTF_8);

        System.out.printf("Глифов: %d, атлас %dx%d%n", codepoints.length, atlasWidth, atlasHeight);
    }

    /** Заполняет клетку атласа полем расстояния до контура. */
    private static void rasterize(BufferedImage atlas, Path2D outline, int originX, int originY) {
        List<double[]> segments = flatten(outline);
        if (segments.isEmpty()) {
            return;
        }

        for (int y = 0; y < CELL; y++) {
            for (int x = 0; x < CELL; x++) {
                double px = originX + x + 0.5;
                double py = originY + y + 0.5;

                double distance = Double.MAX_VALUE;
                for (double[] segment : segments) {
                    distance = Math.min(distance, Line2D.ptSegDist(
                            segment[0], segment[1], segment[2], segment[3], px, py));
                    if (distance == 0.0) {
                        break;
                    }
                }

                // Знак берём по правилу ненулевого обхода — Path2D считает его сам
                double signed = outline.contains(px, py) ? distance : -distance;

                // 0.5 — сам контур, растяжка нормируется шириной поля
                double normalized = clamp(signed / SPREAD * 0.5 + 0.5, 0.0, 1.0);
                int value = (int) Math.round(normalized * 255.0);
                atlas.setRGB(originX + x, originY + y, 0xFF000000 | (value << 16) | (value << 8) | value);
            }
        }
    }

    private static List<double[]> flatten(Path2D outline) {
        List<double[]> segments = new ArrayList<>();
        double[] coords = new double[6];
        double startX = 0;
        double startY = 0;
        double currentX = 0;
        double currentY = 0;

        // Сплющивание кривых: 0.05 текселя достаточно, чтобы расстояние
        // от полилинии не отличалось от расстояния до кривой на глаз
        PathIterator iterator = outline.getPathIterator(null, 0.05);
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO -> {
                    startX = coords[0];
                    startY = coords[1];
                    currentX = startX;
                    currentY = startY;
                }
                case PathIterator.SEG_LINETO -> {
                    segments.add(new double[]{currentX, currentY, coords[0], coords[1]});
                    currentX = coords[0];
                    currentY = coords[1];
                }
                case PathIterator.SEG_CLOSE -> {
                    segments.add(new double[]{currentX, currentY, startX, startY});
                    currentX = startX;
                    currentY = startY;
                }
                default -> {
                }
            }
            iterator.next();
        }
        return segments;
    }

    private static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    private static int[] charset() {
        List<Integer> codes = new ArrayList<>();
        for (int c = 0x20; c <= 0x7E; c++) {
            codes.add(c);
        }
        for (int c = 0x410; c <= 0x44F; c++) {
            codes.add(c);
        }
        int[] extra = {
                0x00A0, 0x00A9, 0x00B0, 0x00B7, 0x00AB, 0x00BB,
                0x0401, 0x0451, 0x0490, 0x0491, 0x0406, 0x0456, 0x0407, 0x0457, 0x0404, 0x0454,
                0x2013, 0x2014, 0x2018, 0x2019, 0x201C, 0x201D, 0x201E, 0x2026,
                0x2116, 0x20BD, 0x2192, 0x2713, 0x2714, 0x00D7};
        for (int code : extra) {
            codes.add(code);
        }

        int[] result = new int[codes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = codes.get(i);
        }
        return result;
    }

    private SdfAtlas() throws IOException {
    }
}
