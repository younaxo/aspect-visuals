package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.setting.NumberSetting;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

public class Slider extends Component {
    private static final float TRACK_HEIGHT = 3f;
    private static final float KNOB = 9f;

    private final NumberSetting setting;

    public Slider(NumberSetting setting, float width) {
        super(width, 12f);
        this.setting = setting;
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float trackY = y + (height - TRACK_HEIGHT) / 2f;
        Render2D.roundedRect(context, x, trackY, width, TRACK_HEIGHT, TRACK_HEIGHT / 2f, AspectColors.BOOLEAN_SOFT);

        float progress = setting.progress();
        float filled = width * progress;
        Render2D.roundedRect(context, x, trackY, filled, TRACK_HEIGHT, TRACK_HEIGHT / 2f, AspectColors.ACCENT_PRIMARY);

        float knobX = x + Math.max(0f, Math.min(width - KNOB, filled - KNOB / 2f));
        Render2D.roundedRect(context, knobX, y + (height - KNOB) / 2f, KNOB, KNOB, KNOB / 2f, AspectColors.ACCENT_PRIMARY);
    }

    @Override
    protected void onPress(double mouseX, double mouseY) {
        apply(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY) {
        apply(mouseX);
    }

    private void apply(double mouseX) {
        setting.setProgress((float) ((mouseX - x) / width));
    }
}
