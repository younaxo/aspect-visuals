package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.setting.EnumSetting;
import su.aspectvisuals.client.ui.render.Icons;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClockModule extends HudModule {
    private static final DateTimeFormatter WITH_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("HH:mm");

    private final EnumSetting format = register(new EnumSetting("Формат", "Показ секунд", "ЧЧ:ММ", List.of("ЧЧ:ММ", "ЧЧ:ММ:СС")));

    public ClockModule() {
        super("Clock", "Текущее время", Category.INFO, 0.02f, 0.16f, HudAnchor.TOP_LEFT);
    }

    private String value() {
        return LocalTime.now().format(format.is("ЧЧ:ММ:СС") ? WITH_SECONDS : SHORT);
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
