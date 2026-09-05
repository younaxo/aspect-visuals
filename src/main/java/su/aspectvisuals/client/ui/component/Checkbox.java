package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.setting.BooleanSetting;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;
import su.aspectvisuals.client.util.Animation;

/** Checkbox из UI Kit: 16x16, скругление 6, галочка внутри. */
public class Checkbox extends Component {
    private final BooleanSetting setting;
    private final Animation fade = new Animation(0.12f);

    public Checkbox(BooleanSetting setting) {
        super(16f, 16f);
        this.setting = setting;
        fade.snap(setting.get() ? 1f : 0f);
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        fade.target(setting.get() ? 1f : 0f);
        float progress = fade.eased();

        int background = AspectColors.lerp(AspectColors.SURFACE_INPUT, AspectColors.BOOLEAN_PRIMARY, progress);
        Render2D.filledBorder(context, x, y, width, height,
                6f, background, 0.5f, AspectColors.SURFACE_BORDER);

        if (progress > 0.02f) {
            int tint = AspectColors.withAlpha(AspectColors.STRONG_BLACK, progress);
            Render2D.texture(context, Icons.CHECK, x + 2f, y + 2f, width - 4f, height - 4f, tint);
        }
    }

    @Override
    protected void onPress(double mouseX, double mouseY) {
        setting.toggle();
    }
}
