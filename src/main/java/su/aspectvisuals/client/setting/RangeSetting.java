package su.aspectvisuals.client.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/** Слайдер с двумя ручками: в Figma это LimitSlider. */
public class RangeSetting extends Setting<double[]> {
    private final double min;
    private final double max;
    private final double step;

    public RangeSetting(String name, String description, double from, double to, double min, double max, double step) {
        super(name, description, new double[]{from, to});
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double from() {
        return get()[0];
    }

    public double to() {
        return get()[1];
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public void setFrom(double value) {
        double snapped = snap(value);
        set(new double[]{Math.min(snapped, to()), to()});
    }

    public void setTo(double value) {
        double snapped = snap(value);
        set(new double[]{from(), Math.max(snapped, from())});
    }

    public float progress(double value) {
        return max == min ? 0f : (float) ((value - min) / (max - min));
    }

    private double snap(double raw) {
        double clamped = Math.max(min, Math.min(max, raw));
        if (step <= 0) {
            return clamped;
        }
        return Math.round(Math.round(clamped / step) * step * 1000d) / 1000d;
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        array.add(from());
        array.add(to());
        return array;
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonArray() && json.getAsJsonArray().size() == 2) {
            JsonArray array = json.getAsJsonArray();
            set(new double[]{snap(array.get(0).getAsDouble()), snap(array.get(1).getAsDouble())});
        }
    }
}
