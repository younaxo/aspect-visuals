package su.aspectvisuals.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, String description, double value, double min, double max, double step) {
        super(name, description, value);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double step() {
        return step;
    }

    public float asFloat() {
        return get().floatValue();
    }

    public int asInt() {
        return (int) Math.round(get());
    }

    /** Позиция ручки слайдера в диапазоне 0..1. */
    public float progress() {
        return max == min ? 0f : (float) ((get() - min) / (max - min));
    }

    public void setProgress(float progress) {
        set(snap(min + (max - min) * Math.max(0f, Math.min(1f, progress))));
    }

    @Override
    public void set(Double value) {
        super.set(snap(value));
    }

    private double snap(double raw) {
        double clamped = Math.max(min, Math.min(max, raw));
        if (step <= 0) {
            return clamped;
        }
        double snapped = Math.round(clamped / step) * step;
        // Шаг вроде 0.1 накапливает погрешность double, поэтому округляем результат
        return Math.round(Math.max(min, Math.min(max, snapped)) * 1000d) / 1000d;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            set(json.getAsDouble());
        }
    }
}
