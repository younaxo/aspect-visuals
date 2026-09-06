package su.aspectvisuals.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import su.aspectvisuals.client.util.ColorMath;

/** Цвет хранится как ARGB и редактируется Color Picker из UI Kit. */
public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, String description, int argb) {
        super(name, description, argb);
    }

    public int alpha() {
        return (get() >>> 24) & 0xFF;
    }

    public int rgb() {
        return get() & 0xFFFFFF;
    }

    public void setAlpha(int alpha) {
        set((Math.max(0, Math.min(255, alpha)) << 24) | rgb());
    }

    public void setRgb(int rgb) {
        set((alpha() << 24) | (rgb & 0xFFFFFF));
    }

    public float[] hsb() {
        return ColorMath.toHsb(rgb());
    }

    public void setHsb(float hue, float saturation, float brightness) {
        setRgb(ColorMath.toRgb(hue, saturation, brightness));
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
