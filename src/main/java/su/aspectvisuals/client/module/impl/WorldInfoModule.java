package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.render.Icons;

import java.util.List;

public class WorldInfoModule extends HudModule {
    public WorldInfoModule() {
        super("World Info", "Данные мира", Category.INFO, 0.02f, 0.4f, HudAnchor.TOP_LEFT);
    }

    private List<HudCard.Row> rows() {
        if (mc.world == null) {
            return List.of(HudCard.Row.of("Мир", "не загружен"));
        }

        long time = mc.world.getTimeOfDay() % 24000L;
        String phase = time < 12000L ? "День" : "Ночь";
        String dimension = mc.world.getRegistryKey().getValue().getPath().replace('_', ' ');

        return List.of(
                HudCard.Row.of("Измерение", dimension),
                HudCard.Row.of("Время суток", phase),
                HudCard.Row.of("Сложность", mc.world.getDifficulty().getName()));
    }

    @Override
    public float widgetWidth() {
        return HudCard.width("World", rows());
    }

    @Override
    public float widgetHeight() {
        return HudCard.height("World", rows());
    }

    @Override
    public void renderWidget(DrawContext context) {
        HudCard.draw(context, 0f, 0f, Icons.LAYOUT, "World", rows());
    }
}
