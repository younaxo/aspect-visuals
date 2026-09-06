package su.aspectvisuals.client.ui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.ui.theme.AspectColors;

/**
 * Подложка под меню: размытие сцены и затемнение.
 *
 * Размытие делает штатный проход игры — он работает по цепочке кадровых
 * буферов и не читает тот буфер, в который сейчас пишет. Собственная
 * реализация потребовала бы своих целей рендера ради того же результата.
 *
 * Сила размытия берётся из настройки игры «Размытие фона меню»: при нуле
 * проход ничего не делает, поэтому затемнение остаётся единственным
 * отделением интерфейса от сцены и не должно от размытия зависеть.
 */
public final class Backdrop {
    private Backdrop() {
    }

    public static void render(DrawContext context, int width, int height) {
        MinecraftClient client = MinecraftClient.getInstance();

        // В главном меню сцены нет, размывать нечего
        if (client.world != null) {
            client.gameRenderer.renderBlur();
        }
        context.fill(0, 0, width, height, AspectColors.BACKGROUND_OVERLAY);
    }
}
