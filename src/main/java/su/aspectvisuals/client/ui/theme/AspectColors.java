package su.aspectvisuals.client.ui.theme;

/**
 * Цвета из Figma-переменных Aspect/Colors.
 * В Figma значения записаны как #RRGGBBAA, здесь они переведены в ARGB.
 */
public final class AspectColors {
    private AspectColors() {
    }

    public static final int BACKGROUND_OVERLAY = 0x2E000000;
    public static final int BACKGROUND_BASE_STRONG = 0xFF080809;
    public static final int BACKGROUND_BASE_NULL = 0x03080809;

    public static final int SURFACE_MAIN_GLASS = 0xAD0B0B0D;
    public static final int SURFACE_CARD = 0x9E111113;
    public static final int SURFACE_CARD_HOVER = 0xB819191B;
    public static final int SURFACE_INPUT = 0xB80B0B0D;
    public static final int SURFACE_BORDER = 0x0DFFFFFF;
    public static final int SURFACE_BORDER_TYPE = 0x29FFFFFF;
    public static final int SURFACE_BORDER_NULL = 0x00FFFFFF;

    public static final int TEXT_PRIMARY = 0xF0FFFFFF;
    public static final int TEXT_SECONDARY = 0x94FFFFFF;
    public static final int TEXT_TERTIARY = 0x5CFFFFFF;
    public static final int TEXT_DISABLED = 0x3DFFFFFF;

    public static final int ACCENT_PRIMARY = 0xFFFFFFFF;
    public static final int ACCENT_GLOW = 0x1AFFFFFF;

    public static final int BOOLEAN_PRIMARY = 0xFFFFFFFF;
    public static final int BOOLEAN_SOFT = 0x401A1A1A;

    public static final int STRONG_BLACK = 0xFF080809;
    public static final int SYSTEM_INFO = 0xFF89A5FF;

    public static final int SHADOW = 0x0A000000;

    public static int withAlpha(int argb, float alpha) {
        int a = Math.round(((argb >>> 24) & 0xFF) * clamp(alpha));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    /** Линейная интерполяция по всем каналам, включая альфу. */
    public static int lerp(int from, int to, float t) {
        float f = clamp(t);
        int a = lerpChannel(from >>> 24, to >>> 24, f);
        int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, f);
        int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, f);
        int b = lerpChannel(from & 0xFF, to & 0xFF, f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }

    private static float clamp(float value) {
        return value < 0f ? 0f : Math.min(value, 1f);
    }
}
