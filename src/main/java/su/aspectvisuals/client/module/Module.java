package su.aspectvisuals.client.module;

import net.minecraft.client.MinecraftClient;
import su.aspectvisuals.client.setting.KeybindSetting;
import su.aspectvisuals.client.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final KeybindSetting keybind = new KeybindSetting("Клавиша", "Быстрое переключение модуля", KeybindSetting.NONE);

    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Category category() {
        return category;
    }

    public KeybindSetting keybind() {
        return keybind;
    }

    public List<Setting<?>> settings() {
        return settings;
    }

    protected <T extends Setting<?>> T register(T setting) {
        settings.add(setting);
        return setting;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick() {
    }
}
