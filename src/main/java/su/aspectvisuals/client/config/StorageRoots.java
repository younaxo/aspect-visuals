package su.aspectvisuals.client.config;

import net.fabricmc.loader.api.FabricLoader;
import su.aspectvisuals.client.AspectVisuals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Где лежат данные клиента.
 *
 * Основное место — папка конфигурации игры. Копии кладутся рядом с игрой и в
 * общесистемную папку, чтобы настройки переживали переустановку сборки.
 *
 * Копии делаются только для настроек. Токен сессии в них не попадает:
 * общесистемная папка читается всеми учётными записями машины, и токен там
 * стал бы доступен любому пользователю компьютера.
 *
 * Недоступная для записи папка — не ошибка: Program Files требует прав
 * администратора, которых у игры нет. О таком месте сообщается один раз, и
 * оно исключается из списка.
 */
public final class StorageRoots {
    private static final Set<Path> unavailable = new HashSet<>();
    private static List<Path> mirrors;

    private StorageRoots() {
    }

    /** Основная папка: её читает клиент. */
    public static Path primary() {
        return create(FabricLoader.getInstance().getConfigDir().resolve(AspectVisuals.MOD_ID));
    }

    /** Папки для копий настроек. Секреты сюда не пишутся. */
    public static synchronized List<Path> mirrors() {
        if (mirrors != null) {
            return mirrors;
        }

        List<Path> candidates = new ArrayList<>();
        // Рядом с игрой: переживает замену папки config
        candidates.add(Paths.get(".").toAbsolutePath().normalize().resolve(AspectVisuals.MOD_ID));

        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            addIfSet(candidates, System.getenv("ProgramData"));
            addIfSet(candidates, System.getenv("ProgramFiles(x86)"));
        }

        mirrors = candidates;
        return mirrors;
    }

    private static void addIfSet(List<Path> candidates, String base) {
        if (base != null && !base.isBlank()) {
            candidates.add(Paths.get(base).resolve("AspectVisuals"));
        }
    }

    /** Создаёт папку и возвращает её; при отказе возвращает null. */
    public static Path prepare(Path directory) {
        if (unavailable.contains(directory)) {
            return null;
        }
        try {
            Files.createDirectories(directory);
            if (!Files.isWritable(directory)) {
                throw new IOException("папка доступна только для чтения");
            }
            return directory;
        } catch (IOException | SecurityException error) {
            // Один раз: иначе запись повторялась бы при каждом сохранении
            unavailable.add(directory);
            AspectVisuals.LOGGER.info("Копия настроек в {} не создаётся: {}",
                    directory, error.getMessage());
            return null;
        }
    }

    private static Path create(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException error) {
            AspectVisuals.LOGGER.error("Не удалось создать папку конфигурации: {}", error.getMessage());
        }
        return directory;
    }
}
