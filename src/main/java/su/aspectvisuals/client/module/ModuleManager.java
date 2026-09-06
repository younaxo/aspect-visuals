package su.aspectvisuals.client.module;

import su.aspectvisuals.client.module.impl.ArmorStatusModule;
import su.aspectvisuals.client.module.impl.BiomeModule;
import su.aspectvisuals.client.module.impl.ClockModule;
import su.aspectvisuals.client.module.impl.CooldownsModule;
import su.aspectvisuals.client.module.impl.CoordinatesModule;
import su.aspectvisuals.client.module.impl.CrosshairModule;
import su.aspectvisuals.client.module.impl.DirectionModule;
import su.aspectvisuals.client.module.impl.FpsCounterModule;
import su.aspectvisuals.client.module.impl.FullbrightModule;
import su.aspectvisuals.client.module.impl.MusicBarModule;
import su.aspectvisuals.client.module.impl.TargetHudModule;
import su.aspectvisuals.client.module.impl.TotemCounterModule;
import su.aspectvisuals.client.module.impl.ItemStatusModule;
import su.aspectvisuals.client.module.impl.KeystrokesModule;
import su.aspectvisuals.client.module.impl.PotionEffectsModule;
import su.aspectvisuals.client.module.impl.SessionStatsModule;
import su.aspectvisuals.client.module.impl.WatermarkModule;
import su.aspectvisuals.client.module.impl.WorldInfoModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        add(new WatermarkModule());
        add(new SessionStatsModule());
        add(new KeystrokesModule());
        add(new PotionEffectsModule());
        add(new CooldownsModule());
        add(new ArmorStatusModule());
        add(new ItemStatusModule());

        add(new FpsCounterModule());
        add(new ClockModule());
        add(new CoordinatesModule());
        add(new DirectionModule());
        add(new BiomeModule());
        add(new WorldInfoModule());

        add(new CrosshairModule());
        add(new MusicBarModule());
        add(new TargetHudModule());
        add(new TotemCounterModule());
        add(new FullbrightModule());
    }

    private void add(Module module) {
        modules.add(module);
    }

    public List<Module> all() {
        return modules;
    }

    public List<Module> byCategory(Category category) {
        return modules.stream().filter(module -> module.category() == category).toList();
    }

    public List<Module> search(String query, Category category) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return modules.stream()
                .filter(module -> category == null || module.category() == category)
                .filter(module -> needle.isEmpty()
                        || module.name().toLowerCase(Locale.ROOT).contains(needle)
                        || module.description().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    public Module byName(String name) {
        return modules.stream().filter(module -> module.name().equals(name)).findFirst().orElse(null);
    }

    public <T extends Module> T byType(Class<T> type) {
        return modules.stream().filter(type::isInstance).map(type::cast).findFirst().orElseThrow();
    }

    public void tick() {
        for (Module module : modules) {
            if (module.enabled()) {
                module.onTick();
            }
        }
    }

    public void onKeyPressed(int key) {
        for (Module module : modules) {
            if (module.keybind().matches(key)) {
                module.toggle();
            }
        }
    }
}
