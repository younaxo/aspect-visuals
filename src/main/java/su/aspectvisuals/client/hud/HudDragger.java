package su.aspectvisuals.client.hud;

import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.render.Render2D;

/**
 * Перетаскивание и масштабирование виджетов HUD.
 *
 * Логика вынесена из редактора, потому что нужна в двух местах: в самом
 * редакторе и поверх открытого чата. Раньше она жила только в редакторе, и
 * подвинуть виджет иначе было нельзя.
 */
public final class HudDragger {
    /** Расстояние, на котором виджет прилипает к краю или центру. */
    private static final float SNAP = 4f;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 2.0f;
    private static final float SCALE_STEP = 0.05f;

    private final HudManager hud;
    private HudModule dragged;
    private float grabX;
    private float grabY;

    public HudDragger(HudManager hud) {
        this.hud = hud;
    }

    public HudModule dragged() {
        return dragged;
    }

    /** Виджет под курсором или null. */
    public HudModule at(double mouseX, double mouseY, float screenWidth, float screenHeight) {
        for (HudModule widget : hud.widgets()) {
            float x = hud.resolveX(widget, (int) screenWidth);
            float y = hud.resolveY(widget, (int) screenHeight);
            float w = widget.widgetWidth() * widget.scale();
            float h = widget.widgetHeight() * widget.scale();
            if (Render2D.hovered(mouseX, mouseY, x, y, w, h)) {
                return widget;
            }
        }
        return null;
    }

    public boolean press(double mouseX, double mouseY, float screenWidth, float screenHeight) {
        HudModule widget = at(mouseX, mouseY, screenWidth, screenHeight);
        if (widget == null) {
            return false;
        }
        dragged = widget;
        grabX = (float) mouseX - hud.resolveX(widget, (int) screenWidth);
        grabY = (float) mouseY - hud.resolveY(widget, (int) screenHeight);
        return true;
    }

    public boolean move(double mouseX, double mouseY, float screenWidth, float screenHeight) {
        if (dragged == null) {
            return false;
        }
        float w = dragged.widgetWidth() * dragged.scale();
        float h = dragged.widgetHeight() * dragged.scale();

        float x = snap((float) mouseX - grabX, w, screenWidth);
        float y = snap((float) mouseY - grabY, h, screenHeight);

        // Точка привязки зависит от угла: правые виджеты держатся за правый край
        float anchorX = dragged.anchor().right() ? x + w : x;
        float anchorY = dragged.anchor().bottom() ? y + h : y;
        dragged.moveTo(anchorX / screenWidth, anchorY / screenHeight);
        return true;
    }

    public void release() {
        dragged = null;
    }

    /** Колесо меняет размер виджета под курсором. */
    public boolean resize(double mouseX, double mouseY, double amount,
                          float screenWidth, float screenHeight) {
        HudModule widget = at(mouseX, mouseY, screenWidth, screenHeight);
        if (widget == null || amount == 0) {
            return false;
        }
        float next = widget.scale() + (float) Math.signum(amount) * SCALE_STEP;
        widget.setScale(Math.max(MIN_SCALE, Math.min(MAX_SCALE, next)));
        return true;
    }

    private static float snap(float value, float size, float screen) {
        float[] targets = {0f, (screen - size) / 2f, screen - size};
        for (float target : targets) {
            if (Math.abs(value - target) < SNAP) {
                return target;
            }
        }
        return Math.max(0f, Math.min(value, screen - size));
    }
}
