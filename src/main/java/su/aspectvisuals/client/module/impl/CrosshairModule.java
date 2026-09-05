package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.setting.BooleanSetting;
import su.aspectvisuals.client.setting.ColorSetting;
import su.aspectvisuals.client.setting.NumberSetting;
import su.aspectvisuals.client.ui.render.Render2D;

/** Прицел из макета: тонкий крест с настраиваемым зазором. */
public class CrosshairModule extends HudModule {
    private final NumberSetting length = register(new NumberSetting("Длина", "Длина луча", 5.0, 1.0, 12.0, 1.0));
    private final NumberSetting gap = register(new NumberSetting("Зазор", "Отступ от центра", 3.0, 0.0, 10.0, 1.0));
    private final NumberSetting thickness = register(new NumberSetting("Толщина", "Толщина луча", 1.0, 1.0, 3.0, 1.0));
    private final BooleanSetting dot = register(new BooleanSetting("Точка", "Точка в центре", false));
    private final ColorSetting color = register(new ColorSetting("Цвет", "Цвет прицела", 0xF0FFFFFF));

    public CrosshairModule() {
        super("Crosshair", "Кастомный прицел", Category.VISUAL, 0.5f, 0.5f, HudAnchor.TOP_LEFT);
    }

    @Override
    public float widgetWidth() {
        return (gap.asFloat() + length.asFloat()) * 2f + thickness.asFloat();
    }

    @Override
    public float widgetHeight() {
        return widgetWidth();
    }

    @Override
    public void renderWidget(DrawContext context) {
        float t = thickness.asFloat();
        float g = gap.asFloat();
        float l = length.asFloat();
        float center = widgetWidth() / 2f;
        int argb = color.get();

        Render2D.rect(context, center - t / 2f, center - g - l, t, l, argb);
        Render2D.rect(context, center - t / 2f, center + g, t, l, argb);
        Render2D.rect(context, center - g - l, center - t / 2f, l, t, argb);
        Render2D.rect(context, center + g, center - t / 2f, l, t, argb);

        if (dot.get()) {
            Render2D.rect(context, center - t / 2f, center - t / 2f, t, t, argb);
        }
    }
}
