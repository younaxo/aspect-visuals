package su.aspectvisuals.client.hud;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import su.aspectvisuals.client.AspectVisuals;

/**
 * Перетаскивание HUD поверх открытого чата.
 *
 * Чат — единственный экран, где игрок видит HUD и может по нему кликать, не
 * закрывая игру. Раскладку удобно править прямо здесь, не заходя в редактор.
 *
 * Действие требует Shift: без него клик по чату остаётся кликом по чату —
 * иначе перестали бы работать ссылки и выделение в сообщениях.
 */
public final class ChatHudDrag {
    private ChatHudDrag() {
    }

    public static void register(HudManager hud) {
        HudDragger dragger = new HudDragger(hud);

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof ChatScreen)) {
                return;
            }

            ScreenMouseEvents.allowMouseClick(screen).register((target, mouseX, mouseY, button) -> {
                if (!shiftHeld(client)) {
                    return true;
                }
                // Возврат false отменяет обработку экраном: клик забрал HUD
                return !dragger.press(mouseX, mouseY, width, height);
            });

            ScreenMouseEvents.allowMouseRelease(screen).register((target, mouseX, mouseY, button) -> {
                if (dragger.dragged() == null) {
                    return true;
                }
                dragger.release();
                hud.save();
                return false;
            });

            ScreenMouseEvents.allowMouseScroll(screen).register(
                    (target, mouseX, mouseY, horizontal, vertical) -> {
                        if (!shiftHeld(client)) {
                            return true;
                        }
                        return !dragger.resize(mouseX, mouseY, vertical, width, height);
                    });

            ScreenEvents.afterRender(screen).register((target, context, mouseX, mouseY, delta) -> {
                if (shiftHeld(client)) {
                    dragger.move(mouseX, mouseY, width, height);
                    hud.drawOutlines(context, dragger.at(mouseX, mouseY, width, height));
                }
            });
        });
    }

    private static boolean shiftHeld(net.minecraft.client.MinecraftClient client) {
        long handle = client.getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
