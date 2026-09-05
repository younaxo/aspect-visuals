package su.aspectvisuals.client.util;

/**
 * Перевод RGB <-> HSB без java.awt: AWT в клиенте Minecraft тянуть незачем,
 * а на macOS обращение к нему из игрового потока может поднять Toolkit.
 */
public final class ColorMath {
    private ColorMath() {
    }

    public static float[] toHsb(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue = 0f;
        if (delta > 0f) {
            if (max == r) {
                hue = ((g - b) / delta) % 6f;
            } else if (max == g) {
                hue = (b - r) / delta + 2f;
            } else {
                hue = (r - g) / delta + 4f;
            }
            hue /= 6f;
            if (hue < 0f) {
                hue += 1f;
            }
        }

        float saturation = max == 0f ? 0f : delta / max;
        return new float[]{hue, saturation, max};
    }

    public static int toRgb(float hue, float saturation, float brightness) {
        float h = (hue - (float) Math.floor(hue)) * 6f;
        float f = h - (float) Math.floor(h);
        float p = brightness * (1f - saturation);
        float q = brightness * (1f - saturation * f);
        float t = brightness * (1f - saturation * (1f - f));

        return switch ((int) h) {
            case 0 -> pack(brightness, t, p);
            case 1 -> pack(q, brightness, p);
            case 2 -> pack(p, brightness, t);
            case 3 -> pack(p, q, brightness);
            case 4 -> pack(t, p, brightness);
            default -> pack(brightness, p, q);
        };
    }

    private static int pack(float r, float g, float b) {
        return (channel(r) << 16) | (channel(g) << 8) | channel(b);
    }

    private static int channel(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255f)));
    }
}
