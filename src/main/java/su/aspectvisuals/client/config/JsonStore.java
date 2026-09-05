package su.aspectvisuals.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import su.aspectvisuals.client.AspectVisuals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Чтение и запись JSON клиента.
 *
 * Битый или частично записанный файл не должен ронять игру: любая проблема
 * означает «настроек нет», а не исключение при запуске. Запись идёт во
 * временный файл с последующей заменой, чтобы падение в момент сохранения
 * не оставило обрезанный конфиг.
 */
public final class JsonStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonStore() {
    }

    public static Path directory() {
        Path directory = FabricLoader.getInstance().getConfigDir().resolve(AspectVisuals.MOD_ID);
        try {
            Files.createDirectories(directory);
        } catch (IOException error) {
            AspectVisuals.LOGGER.error("Не удалось создать папку конфигурации: {}", error.getMessage());
        }
        return directory;
    }

    public static JsonObject read(Path path) {
        if (!Files.exists(path)) {
            return null;
        }

        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            JsonElement element = JsonParser.parseString(content);
            return element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (IOException | JsonSyntaxException error) {
            AspectVisuals.LOGGER.warn("Файл {} повреждён и будет перезаписан", path.getFileName());
            return null;
        }
    }

    public static void write(Path path, JsonObject json) {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(json), StandardCharsets.UTF_8);
            Files.move(temporary, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            AspectVisuals.LOGGER.error("Не удалось сохранить {}: {}", path.getFileName(), error.getMessage());
        }
    }

    public static void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException error) {
            AspectVisuals.LOGGER.warn("Не удалось удалить {}", path.getFileName());
        }
    }

    public static String string(JsonObject json, String key) {
        if (json != null && json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsString();
        }
        return "";
    }

    public static long longValue(JsonObject json, String key) {
        if (json != null && json.has(key) && json.get(key).isJsonPrimitive()) {
            try {
                return json.get(key).getAsLong();
            } catch (NumberFormatException error) {
                return 0L;
            }
        }
        return 0L;
    }

    public static boolean booleanValue(JsonObject json, String key, boolean fallback) {
        if (json != null && json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsBoolean();
        }
        return fallback;
    }
}
