package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.setting.BooleanSetting;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;
import su.aspectvisuals.client.util.Animation;

/** Переключатель Boolean из UI Kit: 40x16, ручка 22x13. */
public class Toggle extends Component {
    private static final float TRACK_WIDTH = 40f;
    private static final float TRACK_HEIGHT = 16f;
    private static final float KNOB_WIDTH = 22f;
    private static final float KNOB_HEIGHT = 13f;
    private static final float PADDING = 1.5f;

    private final BooleanSetting setting;
    private final Animation slide = new Animation(0.14f);

    public Toggle(BooleanSetting setting) {
        super(TRACK_WIDTH, TRACK_HEIGHT);
        this.setting = setting;
        slide.snap(setting.get() ? 1f : 0f);
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        slide.target(setting.get() ? 1f : 0f);
        float progress = slide.eased();

        int track = AspectColors.lerp(AspectColors.BOOLEAN_SOFT, AspectColors.BOOLEAN_PRIMARY, progress);
        Render2D.filledBorder(context, x, y, width, height,
                height / 2f, track, 0.5f, AspectColors.SURFACE_BORDER);

        float travel = width - KNOB_WIDTH - PADDING * 2;
        float knobX = x + PADDING + travel * progress;
        float knobY = y + (height - KNOB_HEIGHT) / 2f;

        int knobColor = progress > 0.5f
                ? AspectColors.STRONG_BLACK
                : AspectColors.withAlpha(AspectColors.BOOLEAN_PRIMARY, 0.2f);
        Render2D.roundedRect(context, knobX, knobY, KNOB_WIDTH, KNOB_HEIGHT, KNOB_HEIGHT / 2f, knobColor);
    }

    @Override
    protected void onPress(double mouseX, double mouseY) {
        setting.toggle();
    }
}
