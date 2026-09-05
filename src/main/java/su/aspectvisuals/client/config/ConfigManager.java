package su.aspectvisuals.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.module.Module;
import su.aspectvisuals.client.module.ModuleManager;
import su.aspectvisuals.client.setting.Setting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Состояния модулей, настройки и позиции HUD.
 *
 * Формат намеренно терпимый: незнакомые ключи игнорируются, пропавшие
 * остаются значением по умолчанию. Это позволяет добавлять и убирать
 * настройки между версиями, не ломая конфиг пользователя.
 */
public final class ConfigManager {
    private static final String FILE = "client.json";
    private static final int VERSION = 1;

    private final ModuleManager modules;
    private final Path path;

    public ConfigManager(ModuleManager modules) {
        this.modules = modules;
        this.path = JsonStore.directory().resolve(FILE);
    }

    public void load() {
        JsonObject root = JsonStore.read(path);
        if (root == null) {
            return;
        }
        apply(root);
        AspectVisuals.LOGGER.info("Конфигурация загружена");
    }

    private void apply(JsonObject root) {
        JsonObject moduleStates = object(root, "modules");
        if (moduleStates == null) {
            return;
        }

        for (Module module : modules.all()) {
            JsonObject stored = object(moduleStates, module.name());
            if (stored == null) {
                continue;
            }

            module.setEnabled(JsonStore.booleanValue(stored, "enabled", false));
            module.keybind().fromJson(stored.get("keybind"));

            JsonObject settings = object(stored, "settings");
            if (settings == null) {
                continue;
            }

            for (Setting<?> setting : module.settings()) {
                JsonElement value = settings.get(setting.name());
                if (value != null) {
                    applySafely(module, setting, value);
                }
            }
        }
    }

    /** Одна испорченная настройка не должна отменять загрузку всего конфига. */
    private void applySafely(Module module, Setting<?> setting, JsonElement value) {
        try {
            setting.fromJson(value);
        } catch (RuntimeException error) {
            AspectVisuals.LOGGER.warn("Настройка «{}» модуля {} пропущена: неверное значение",
                    setting.name(), module.name());
        }
    }

    public void save() {
        JsonStore.write(path, snapshot());
    }

    private JsonObject snapshot() {
        JsonObject moduleStates = new JsonObject();

        for (Module module : modules.all()) {
            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.settings()) {
                settings.add(setting.name(), setting.toJson());
            }

            JsonObject stored = new JsonObject();
            stored.addProperty("enabled", module.enabled());
            stored.add("keybind", module.keybind().toJson());
            stored.add("settings", settings);

            moduleStates.add(module.name(), stored);
        }

        JsonObject root = new JsonObject();
        root.addProperty("version", VERSION);
        root.add("modules", moduleStates);
        return root;
    }

    // --- Именованные пресеты ---

    private static final String PRESET_PREFIX = "config-";

    /** Имя пресета попадает в имя файла, поэтому в нём допустимы только безопасные символы. */
    private static String safeName(String name) {
        String cleaned = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-zа-я0-9 _-]", "").trim();
        return cleaned.length() > 32 ? cleaned.substring(0, 32) : cleaned;
    }

    private Path presetPath(String name) {
        return JsonStore.directory().resolve(PRESET_PREFIX + safeName(name) + ".json");
    }

    public List<String> presets() {
        List<String> names = new ArrayList<>();
        try (Stream<Path> files = Files.list(JsonStore.directory())) {
            files.map(file -> file.getFileName().toString())
                    .filter(file -> file.startsWith(PRESET_PREFIX) && file.endsWith(".json"))
                    .map(file -> file.substring(PRESET_PREFIX.length(), file.length() - 5))
                    .sorted()
                    .forEach(names::add);
        } catch (IOException error) {
            AspectVisuals.LOGGER.warn("Не удалось прочитать список конфигов: {}", error.getMessage());
        }
        return names;
    }

    public boolean saveNamed(String name) {
        String safe = safeName(name);
        if (safe.isEmpty()) {
            return false;
        }
        JsonStore.write(presetPath(safe), snapshot());
        return true;
    }

    public boolean loadNamed(String name) {
        JsonObject root = JsonStore.read(presetPath(name));
        if (root == null) {
            return false;
        }
        apply(root);
        save();
        return true;
    }

    public void deleteNamed(String name) {
        JsonStore.delete(presetPath(name));
    }

    private static JsonObject object(JsonObject parent, String key) {
        if (parent.has(key) && parent.get(key).isJsonObject()) {
            return parent.getAsJsonObject(key);
        }
        return null;
    }
}
