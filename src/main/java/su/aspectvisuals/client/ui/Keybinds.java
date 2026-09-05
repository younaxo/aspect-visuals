package su.aspectvisuals.client.ui;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.ui.screen.AspectScreen;
import su.aspectvisuals.client.ui.screen.HudEditorScreen;

public final class Keybinds {
    private static KeyBinding openGui;
    private static KeyBinding hudEditor;

    private Keybinds() {
    }

    public static void register() {
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aspectvisuals.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.aspectvisuals"));

        hudEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aspectvisuals.hud_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_CONTROL,
                "key.categories.aspectvisuals"));
    }

    public static void tick(MinecraftClient client) {
        while (openGui.wasPressed()) {
            client.setScreen(new AspectScreen());
        }
        while (hudEditor.wasPressed()) {
            client.setScreen(new HudEditorScreen(null));
        }

        // Привязки модулей срабатывают только вне экранов клиента
        if (client.currentScreen == null) {
            long window = client.getWindow().getHandle();
            AspectVisuals.modules().all().forEach(module -> {
                int key = module.keybind().get();
                if (module.keybind().bound() && InputUtil.isKeyPressed(window, key)) {
                    pressed(module.name(), key);
                }
            });
        }
        release();
    }

    // GLFW сообщает удержание, а не нажатие, поэтому фиксируем фронт сами
    private static final java.util.Set<String> held = new java.util.HashSet<>();
    private static final java.util.Set<String> seenThisTick = new java.util.HashSet<>();

    private static void pressed(String module, int key) {
        seenThisTick.add(module);
        if (held.add(module)) {
            AspectVisuals.modules().onKeyPressed(key);
        }
    }

    private static void release() {
        held.retainAll(seenThisTick);
        seenThisTick.clear();
    }
}
