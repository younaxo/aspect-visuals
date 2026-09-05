package su.aspectvisuals.client.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Набор чекбоксов: несколько вариантов включаются независимо. */
public class MultiSelectSetting extends Setting<Set<String>> {
    private final List<String> options;

    public MultiSelectSetting(String name, String description, List<String> options, Set<String> selected) {
        super(name, description, new LinkedHashSet<>(selected));
        this.options = List.copyOf(options);
    }

    public List<String> options() {
        return options;
    }

    public boolean selected(String option) {
        return get().contains(option);
    }

    public void toggle(String option) {
        if (!options.contains(option)) {
            return;
        }
        if (!get().remove(option)) {
            get().add(option);
        }
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        get().forEach(array::add);
        return array;
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json == null || !json.isJsonArray()) {
            return;
        }
        Set<String> restored = new LinkedHashSet<>();
        for (JsonElement element : json.getAsJsonArray()) {
            String option = element.getAsString();
            if (options.contains(option)) {
                restored.add(option);
            }
        }
        set(restored);
    }
}
