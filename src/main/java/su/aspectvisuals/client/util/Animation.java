package su.aspectvisuals.client.util;

/**
 * Анимация по времени, а не по кадрам: значение подтягивается к цели
 * с постоянной скоростью независимо от FPS.
 */
public final class Animation {
    private final float duration;
    private float value;
    private float target;
    private long lastUpdate = System.nanoTime();

    public Animation(float durationSeconds) {
        this(durationSeconds, 0f);
    }

    public Animation(float durationSeconds, float value) {
        this.duration = Math.max(0.001f, durationSeconds);
        this.value = value;
        this.target = value;
    }

    public void target(float target) {
        this.target = target;
    }

    public void snap(float value) {
        this.value = value;
        this.target = value;
    }

    public float value() {
        long now = System.nanoTime();
        float delta = (now - lastUpdate) / 1_000_000_000f;
        lastUpdate = now;

        // После сворачивания окна кадр может «прыгнуть» на секунды — обрезаем
        delta = Math.min(delta, 0.1f);

        float step = delta / duration;
        if (Math.abs(target - value) <= step) {
            value = target;
        } else {
            value += Math.signum(target - value) * step;
        }
        return value;
    }

    /** Значение с плавным входом-выходом: то же ощущение, что у переходов в макете. */
    public float eased() {
        return Easing.IN_OUT.apply(value());
    }

    public boolean done() {
        return value == target;
    }
}
