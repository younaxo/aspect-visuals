package su.aspectvisuals.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

/** Dropdown из UI Kit: значения задаются списком подписей. */
public class EnumSetting extends Setting<String> {
    private final List<String> options;

    public EnumSetting(String name, String description, String value, List<String> options) {
        super(name, description, value);
        this.options = List.copyOf(options);
    }

    public List<String> options() {
        return options;
    }

    public int index() {
        int index = options.indexOf(get());
        return index < 0 ? 0 : index;
    }

    public void select(int index) {
        if (index >= 0 && index < options.size()) {
            set(options.get(index));
        }
    }

    public void next() {
        select((index() + 1) % options.size());
    }

    public boolean is(String option) {
        return option.equals(get());
    }

    @Override
    public void set(String value) {
        if (options.contains(value)) {
            super.set(value);
        }
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            set(json.getAsString());
        }
    }
}
