package su.aspectvisuals.client.setting;

import com.google.gson.JsonElement;

import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    protected T value;
    private BooleanSupplier visibility = () -> true;

    protected Setting(String name, String description, T value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    /** Настройка показывается в интерфейсе, только когда условие выполнено. */
    public Setting<T> visibleWhen(BooleanSupplier condition) {
        this.visibility = condition;
        return this;
    }

    public boolean visible() {
        return visibility.getAsBoolean();
    }

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement json);
}
