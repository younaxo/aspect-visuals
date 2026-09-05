package su.aspectvisuals.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting<String> {
    private final int maxLength;
    private final String placeholder;

    public StringSetting(String name, String description, String value, String placeholder, int maxLength) {
        super(name, description, value);
        this.placeholder = placeholder;
        this.maxLength = maxLength;
    }

    public String placeholder() {
        return placeholder;
    }

    public int maxLength() {
        return maxLength;
    }

    @Override
    public void set(String value) {
        String text = value == null ? "" : value;
        super.set(text.length() > maxLength ? text.substring(0, maxLength) : text);
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
