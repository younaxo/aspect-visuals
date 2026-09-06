package su.aspectvisuals.client.ui.screen;

import net.minecraft.client.gui.DrawContext;

/** Содержимое вкладки главного экрана: Settings, Friends, Waypoints или Configs. */
public interface ScreenView {
    void layout(int screenWidth, int screenHeight, float top, float bottom);

    void render(DrawContext context, int mouseX, int mouseY, float delta);

    /** Второй проход для выпадающих списков и подсказок поверх карточек. */
    default void renderOverlay(DrawContext context, int mouseX, int mouseY) {
    }

    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    default boolean keyPressed(int key, int scancode, int modifiers) {
        return false;
    }

    default boolean charTyped(char symbol, int modifiers) {
        return false;
    }
}
