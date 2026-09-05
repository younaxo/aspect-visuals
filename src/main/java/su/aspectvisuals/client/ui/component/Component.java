package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.ui.render.Render2D;

/**
 * Базовый интерактивный элемент интерфейса.
 * Состояния из UI Kit: default, hover, pressed, focused, active, disabled.
 */
public abstract class Component {
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    private boolean enabled = true;
    private boolean visible = true;
    private boolean pressed;
    private boolean focused;

    protected Component(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public Component bounds(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Component size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public boolean enabled() {
        return enabled;
    }

    public Component setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean visible() {
        return visible;
    }

    public Component setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean pressed() {
        return pressed;
    }

    public boolean focused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean hovered(double mouseX, double mouseY) {
        return visible && enabled && Render2D.hovered(mouseX, mouseY, x, y, width, height);
    }

    public final void draw(DrawContext context, int mouseX, int mouseY, float delta) {
        if (visible) {
            render(context, mouseX, mouseY, delta);
        }
    }

    protected abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!hovered(mouseX, mouseY) || button != 0) {
            return false;
        }
        pressed = true;
        onPress(mouseX, mouseY);
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!pressed) {
            return false;
        }
        pressed = false;
        onRelease(mouseX, mouseY);
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!pressed) {
            return false;
        }
        onDrag(mouseX, mouseY);
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    public boolean keyPressed(int key, int scancode, int modifiers) {
        return false;
    }

    public boolean charTyped(char symbol, int modifiers) {
        return false;
    }

    protected void onPress(double mouseX, double mouseY) {
    }

    protected void onRelease(double mouseX, double mouseY) {
    }

    protected void onDrag(double mouseX, double mouseY) {
    }
}
