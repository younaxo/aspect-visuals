package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.setting.RangeSetting;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/** LimitSlider из макета: две ручки на одной дорожке. */
public class RangeSlider extends Component {
    private static final float TRACK_HEIGHT = 3f;
    private static final float KNOB = 9f;

    private final RangeSetting setting;
    private boolean draggingTo;

    public RangeSlider(RangeSetting setting, float width) {
        super(width, 12f);
        this.setting = setting;
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float trackY = y + (height - TRACK_HEIGHT) / 2f;
        Render2D.roundedRect(context, x, trackY, width, TRACK_HEIGHT, TRACK_HEIGHT / 2f, AspectColors.BOOLEAN_SOFT);

        float from = knobX(setting.from());
        float to = knobX(setting.to());
        Render2D.roundedRect(context, from, trackY, Math.max(0f, to - from), TRACK_HEIGHT, TRACK_HEIGHT / 2f, AspectColors.ACCENT_PRIMARY);

        Render2D.roundedRect(context, from, y + (height - KNOB) / 2f, KNOB, KNOB, KNOB / 2f, AspectColors.ACCENT_PRIMARY);
        Render2D.roundedRect(context, to, y + (height - KNOB) / 2f, KNOB, KNOB, KNOB / 2f, AspectColors.ACCENT_PRIMARY);
    }

    private float knobX(double value) {
        return x + (width - KNOB) * setting.progress(value);
    }

    @Override
    protected void onPress(double mouseX, double mouseY) {
        draggingTo = Math.abs(mouseX - knobX(setting.to())) < Math.abs(mouseX - knobX(setting.from()));
        apply(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY) {
        apply(mouseX);
    }

    private void apply(double mouseX) {
        double progress = Math.max(0d, Math.min(1d, (mouseX - x) / width));
        double value = setting.min() + (setting.max() - setting.min()) * progress;
        if (draggingTo) {
            setting.setTo(value);
        } else {
            setting.setFrom(value);
        }
    }
}
