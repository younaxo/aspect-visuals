package su.aspectvisuals.client.ui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.hud.HudManager;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/**
 * Редактор HUD: виджеты перетаскиваются мышью и прилипают к краям и центру.
 * Позиции сохраняются долями экрана, поэтому не ломаются при смене разрешения.
 */
public class HudEditorScreen extends Screen {
    private static final float SNAP = 6f;

    private final Screen parent;
    private final HudManager hud;

    private HudModule dragged;
    private float grabX;
    private float grabY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("Редактор HUD"));
        this.parent = parent;
        this.hud = AspectVisuals.hud();
    }

    @Override
    protected void init() {
        hud.setEditing(true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, AspectColors.BACKGROUND_OVERLAY);
        drawGuides(context);

        for (HudModule widget : hud.widgets()) {
            float x = hud.resolveX(widget, width);
            float y = hud.resolveY(widget, height);
            float w = widget.widgetWidth() * widget.scale();
            float h = widget.widgetHeight() * widget.scale();

            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0f);
            context.getMatrices().scale(widget.scale(), widget.scale(), 1f);
            widget.renderWidget(context);
            context.getMatrices().pop();

            boolean hover = Render2D.hovered(mouseX, mouseY, x, y, w, h);
            int outline = widget.enabled()
                    ? (hover || widget == dragged ? AspectColors.ACCENT_PRIMARY : AspectColors.SURFACE_BORDER_TYPE)
                    : AspectColors.SURFACE_BORDER;
            Render2D.border(context, x - 2f, y - 2f, w + 4f, h + 4f, 1f, outline);

            if (hover) {
                AspectFont.MEDIUM.draw(context, widget.name(), x, y - 14f, AspectColors.TEXT_PRIMARY);
            }
        }

        AspectFont.MEDIUM.drawCentered(context,
                "Перетаскивайте виджеты. Колесо — масштаб. ПКМ — включить или выключить. Esc — выход",
                width / 2f, height - 24f, AspectColors.TEXT_SECONDARY);
    }

    private void drawGuides(DrawContext context) {
        if (dragged == null) {
            return;
        }
        Render2D.rect(context, width / 2f, 0f, 1f, height, AspectColors.ACCENT_GLOW);
        Render2D.rect(context, 0f, height / 2f, width, 1f, AspectColors.ACCENT_GLOW);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (HudModule widget : hud.widgets()) {
            float x = hud.resolveX(widget, width);
            float y = hud.resolveY(widget, height);
            float w = widget.widgetWidth() * widget.scale();
            float h = widget.widgetHeight() * widget.scale();

            if (!Render2D.hovered(mouseX, mouseY, x, y, w, h)) {
                continue;
            }

            if (button == 1) {
                widget.toggle();
                return true;
            }
            dragged = widget;
            grabX = (float) mouseX - x;
            grabY = (float) mouseY - y;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragged == null) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        float w = dragged.widgetWidth() * dragged.scale();
        float h = dragged.widgetHeight() * dragged.scale();

        float x = snap((float) mouseX - grabX, w, width);
        float y = snap((float) mouseY - grabY, h, height);

        // Точка привязки зависит от угла: правые виджеты держатся за правый край
        float anchorX = dragged.anchor().right() ? x + w : x;
        float anchorY = dragged.anchor().bottom() ? y + h : y;
        dragged.moveTo(anchorX / width, anchorY / height);
        return true;
    }

    private float snap(float value, float size, float screen) {
        float[] targets = {0f, (screen - size) / 2f, screen - size};
        for (float target : targets) {
            if (Math.abs(value - target) < SNAP) {
                return target;
            }
        }
        return Math.max(0f, Math.min(value, screen - size));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragged = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        for (HudModule widget : hud.widgets()) {
            float x = hud.resolveX(widget, width);
            float y = hud.resolveY(widget, height);
            float w = widget.widgetWidth() * widget.scale();
            float h = widget.widgetHeight() * widget.scale();

            if (Render2D.hovered(mouseX, mouseY, x, y, w, h)) {
                widget.settings().stream()
                        .filter(setting -> setting.name().equals("Масштаб"))
                        .findFirst()
                        .ifPresent(setting -> {
                            if (setting instanceof su.aspectvisuals.client.setting.NumberSetting scale) {
                                scale.set(scale.get() + vertical * 0.05);
                            }
                        });
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public void close() {
        hud.setEditing(false);
        AspectVisuals.config().save();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
