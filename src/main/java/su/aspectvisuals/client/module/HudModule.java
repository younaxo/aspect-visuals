package su.aspectvisuals.client.module;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.setting.EnumSetting;
import su.aspectvisuals.client.setting.NumberSetting;

import java.util.List;

/**
 * Модуль, который рисует виджет на экране.
 *
 * Позиция хранится долями экрана, а не пикселями: интерфейс одинаково
 * ложится на 1280x720 и на ультраширокий монитор.
 */
public abstract class HudModule extends Module {
    private final NumberSetting posX = register(new NumberSetting("Позиция X", "Доля ширины экрана", 0.02, 0, 1, 0.001));
    private final NumberSetting posY = register(new NumberSetting("Позиция Y", "Доля высоты экрана", 0.02, 0, 1, 0.001));
    private final NumberSetting scale = register(new NumberSetting("Масштаб", "Размер виджета", 1.0, 0.5, 2.0, 0.05));
    private final EnumSetting anchor = register(new EnumSetting("Привязка", "Угол, к которому прижимается виджет",
            HudAnchor.TOP_LEFT.label(), List.of(
                    HudAnchor.TOP_LEFT.label(),
                    HudAnchor.TOP_RIGHT.label(),
                    HudAnchor.BOTTOM_LEFT.label(),
                    HudAnchor.BOTTOM_RIGHT.label())));

    protected HudModule(String name, String description, Category category, float x, float y, HudAnchor anchor) {
        super(name, description, category);
        this.posX.set((double) x);
        this.posY.set((double) y);
        this.anchor.set(anchor.label());
    }

    public NumberSetting posX() {
        return posX;
    }

    public NumberSetting posY() {
        return posY;
    }

    public float scale() {
        return scale.asFloat();
    }

    public HudAnchor anchor() {
        return HudAnchor.byLabel(anchor.get());
    }

    public void moveTo(float fractionX, float fractionY) {
        posX.set((double) fractionX);
        posY.set((double) fractionY);
    }

    public abstract float widgetWidth();

    public abstract float widgetHeight();

    /** Отрисовка виджета в собственных координатах: (0, 0) — левый верхний угол. */
    public abstract void renderWidget(DrawContext context);

    /** Данные-заглушка для редактора HUD, когда в мире виджету нечего показать. */
    public boolean drawInEditor() {
        return true;
    }
}
