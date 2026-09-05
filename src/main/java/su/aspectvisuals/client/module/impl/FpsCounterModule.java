package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.render.Icons;

import java.util.List;

public class FpsCounterModule extends HudModule {
    public FpsCounterModule() {
        super("FPS Counter", "Счётчик FPS", Category.INFO, 0.02f, 0.1f, HudAnchor.TOP_LEFT);
    }

    private String value() {
        return mc.getCurrentFps() + " fps";
    }

    @Override
    public float widgetWidth() {
        return HudCard.pillWidth(Icons.TIMER, value());
    }

    @Override
    public float widgetHeight() {
        return HudCard.pillHeight();
    }

    @Override
    public void renderWidget(DrawContext context) {
        HudCard.pill(context, 0f, 0f, Icons.TIMER, value());
    }
}
