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

    private final Screen parent;
    private final HudManager hud;

    private final su.aspectvisuals.client.hud.HudDragger dragger =
            new su.aspectvisuals.client.hud.HudDragger(su.aspectvisuals.client.AspectVisuals.hud());

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
                    ? (hover || widget == dragger.dragged() ? AspectColors.ACCENT_PRIMARY : AspectColors.SURFACE_BORDER_TYPE)
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
        if (dragger.dragged() == null) {
            return;
        }
        Render2D.rect(context, width / 2f, 0f, 1f, height, AspectColors.ACCENT_GLOW);
        Render2D.rect(context, 0f, height / 2f, width, 1f, AspectColors.ACCENT_GLOW);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        HudModule widget = dragger.at(mouseX, mouseY, width, height);
        if (widget == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (button == 1) {
            widget.toggle();
            return true;
        }
        return dragger.press(mouseX, mouseY, width, height);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return dragger.move(mouseX, mouseY, width, height)
                || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragger.release();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        return dragger.resize(mouseX, mouseY, vertical, width, height)
                || super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
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
