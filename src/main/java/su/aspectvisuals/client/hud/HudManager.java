package su.aspectvisuals.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.module.Module;
import su.aspectvisuals.client.module.ModuleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Отрисовка HUD и раскладка виджетов.
 *
 * Позиция каждого виджета хранится долей экрана, поэтому раскладка
 * переживает смену разрешения, GUI Scale и полноэкранный режим.
 */
public final class HudManager {
    private final ModuleManager modules;
    private boolean editing;

    public HudManager(ModuleManager modules) {
        this.modules = modules;
    }

    public boolean editing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public List<HudModule> widgets() {
        List<HudModule> widgets = new ArrayList<>();
        for (Module module : modules.all()) {
            if (module instanceof HudModule widget) {
                widgets.add(widget);
            }
        }
        return widgets;
    }

    public List<HudModule> visibleWidgets() {
        List<HudModule> visible = new ArrayList<>();
        for (HudModule widget : widgets()) {
            if (widget.enabled()) {
                visible.add(widget);
            }
        }
        return visible;
    }

    public float resolveX(HudModule widget, int screenWidth) {
        float raw = widget.posX().asFloat() * screenWidth;
        float width = widget.widgetWidth() * widget.scale();
        float x = widget.anchor().right() ? raw - width : raw;
        return clamp(x, screenWidth - width);
    }

    public float resolveY(HudModule widget, int screenHeight) {
        float raw = widget.posY().asFloat() * screenHeight;
        float height = widget.widgetHeight() * widget.scale();
        float y = widget.anchor().bottom() ? raw - height : raw;
        return clamp(y, screenHeight - height);
    }

    private static float clamp(float value, float max) {
        return Math.max(0f, Math.min(value, Math.max(0f, max)));
    }

    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        // Во время редактирования HUD рисует сам редактор, иначе виджеты задвоятся
        if (client.options.hudHidden || editing) {
            return;
        }
        draw(context, visibleWidgets());
    }

    /** Редактор рисует и выключенные виджеты, чтобы их можно было расставить. */
    public void drawAll(DrawContext context) {
        draw(context, widgets());
    }

    private void draw(DrawContext context, List<HudModule> widgets) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        for (HudModule widget : widgets) {
            float x = resolveX(widget, screenWidth);
            float y = resolveY(widget, screenHeight);
            float scale = widget.scale();

            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0f);
            if (scale != 1f) {
                context.getMatrices().scale(scale, scale, 1f);
            }
            widget.renderWidget(context);
            context.getMatrices().pop();
        }
    }
}
