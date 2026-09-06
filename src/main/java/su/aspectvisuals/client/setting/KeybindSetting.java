package su.aspectvisuals.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.lwjgl.glfw.GLFW;

public class KeybindSetting extends Setting<Integer> {
    public static final int NONE = GLFW.GLFW_KEY_UNKNOWN;

    public KeybindSetting(String name, String description, int key) {
        super(name, description, key);
    }

    public boolean bound() {
        return get() != NONE;
    }

    public boolean matches(int key) {
        return bound() && get() == key;
    }

    public String label() {
        if (!bound()) {
            return "—";
        }
        String name = GLFW.glfwGetKeyName(get(), 0);
        if (name != null && !name.isBlank()) {
            return name.toUpperCase(java.util.Locale.ROOT);
        }
        return switch (get()) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            default -> "KEY " + get();
        };
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            set(json.getAsInt());
        }
    }
}
