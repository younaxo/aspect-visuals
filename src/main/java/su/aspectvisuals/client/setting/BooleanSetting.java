package su.aspectvisuals.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, boolean value) {
        super(name, description, value);
    }

    public void toggle() {
        set(!get());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            set(json.getAsBoolean());
        }
    }
}
