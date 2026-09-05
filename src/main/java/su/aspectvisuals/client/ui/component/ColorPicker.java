package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.setting.ColorSetting;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;
import su.aspectvisuals.client.util.ColorMath;

/** Color Picker из макета: поле насыщенности, полоса тона и полоса прозрачности. */
public class ColorPicker extends Component {
    private static final float BAR_HEIGHT = 8f;
    private static final float GAP = 6f;

    private enum Target { FIELD, HUE, ALPHA, NONE }

    private final ColorSetting setting;
    private Target dragging = Target.NONE;

    public ColorPicker(ColorSetting setting, float width) {
        super(width, width * 0.62f + (BAR_HEIGHT + GAP) * 2);
        this.setting = setting;
    }

    private float fieldHeight() {
        return height - (BAR_HEIGHT + GAP) * 2;
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float[] hsb = setting.hsb();
        float fieldHeight = fieldHeight();

        // Насыщенность по горизонтали, яркость по вертикали — рисуем полосами
        int pure = 0xFF000000 | ColorMath.toRgb(hsb[0], 1f, 1f);
        Render2D.roundedRect(context, x, y, width, fieldHeight, 8f, 0xFFFFFFFF);
        Render2D.gradient(context, x, y, width, fieldHeight, 0x00FFFFFF, pure);
        Render2D.gradient(context, x, y, width, fieldHeight, 0x00000000, 0xFF000000);

        float markerX = x + width * hsb[1];
        float markerY = y + fieldHeight * (1f - hsb[2]);
        Render2D.roundedRect(context, markerX - 3f, markerY - 3f, 6f, 6f, 3f, AspectColors.ACCENT_PRIMARY);

        float hueY = y + fieldHeight + GAP;
        for (int i = 0; i < (int) width; i++) {
            int color = 0xFF000000 | ColorMath.toRgb(i / width, 1f, 1f);
            Render2D.rect(context, x + i, hueY, 1f, BAR_HEIGHT, color);
        }
        Render2D.rect(context, x + width * hsb[0] - 1f, hueY, 2f, BAR_HEIGHT, AspectColors.ACCENT_PRIMARY);

        float alphaY = hueY + BAR_HEIGHT + GAP;
        int solid = 0xFF000000 | setting.rgb();
        Render2D.rect(context, x, alphaY, width, BAR_HEIGHT, AspectColors.SURFACE_INPUT);
        for (int i = 0; i < (int) width; i++) {
            int color = AspectColors.withAlpha(solid, i / width);
            Render2D.rect(context, x + i, alphaY, 1f, BAR_HEIGHT, color);
        }
        Render2D.rect(context, x + width * (setting.alpha() / 255f) - 1f, alphaY, 2f, BAR_HEIGHT, AspectColors.ACCENT_PRIMARY);
    }

    @Override
    protected void onPress(double mouseX, double mouseY) {
        float fieldHeight = fieldHeight();
        float hueY = y + fieldHeight + GAP;
        float alphaY = hueY + BAR_HEIGHT + GAP;

        if (mouseY < hueY) {
            dragging = Target.FIELD;
        } else if (mouseY < alphaY) {
            dragging = Target.HUE;
        } else {
            dragging = Target.ALPHA;
        }
        apply(mouseX, mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY) {
        apply(mouseX, mouseY);
    }

    @Override
    protected void onRelease(double mouseX, double mouseY) {
        dragging = Target.NONE;
    }

    private void apply(double mouseX, double mouseY) {
        float ratioX = clamp((float) ((mouseX - x) / width));
        float[] hsb = setting.hsb();

        switch (dragging) {
            case FIELD -> {
                float ratioY = clamp((float) ((mouseY - y) / fieldHeight()));
                setting.setHsb(hsb[0], ratioX, 1f - ratioY);
            }
            case HUE -> setting.setHsb(ratioX, hsb[1], hsb[2]);
            case ALPHA -> setting.setAlpha(Math.round(ratioX * 255f));
            case NONE -> {
            }
        }
    }

    private static float clamp(float value) {
        return value < 0f ? 0f : Math.min(value, 1f);
    }
}
