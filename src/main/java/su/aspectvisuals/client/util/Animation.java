package su.aspectvisuals.client.util;

/**
 * Анимация по времени, а не по кадрам.
 *
 * Значение считается от момента постановки цели, поэтому повторное чтение
 * внутри одного кадра ничего не сдвигает: раньше каждое обращение отматывало
 * собственную дельту, и скорость зависела от того, сколько раз компонент
 * спросил значение.
 *
 * Возвращается дробное значение: округление до целого раньше делало движение
 * ступенчатым, потому что шаг равнялся логическому пикселю.
 */
public final class Animation {
    private final float duration;
    private final Easing easing;

    private float from;
    private float target;
    private long startedAt;

    public Animation(float durationSeconds) {
        this(durationSeconds, 0f, Easing.IN_OUT);
    }

    public Animation(float durationSeconds, float value) {
        this(durationSeconds, value, Easing.IN_OUT);
    }

    public Animation(float durationSeconds, float value, Easing easing) {
        this.duration = Math.max(0.001f, durationSeconds);
        this.easing = easing;
        this.from = value;
        this.target = value;
        this.startedAt = System.nanoTime();
    }

    public void target(float target) {
        if (target == this.target) {
            return;
        }
        // Стартуем от текущего положения, иначе смена цели на полпути дёргает
        this.from = value();
        this.target = target;
        this.startedAt = System.nanoTime();
    }

    public void snap(float value) {
        this.from = value;
        this.target = value;
        this.startedAt = System.nanoTime();
    }

    /** Линейное значение без сглаживания кривой. */
    public float value() {
        return from + (target - from) * progress();
    }

    /** Значение с плавным входом и выходом, как переходы в макете. */
    public float eased() {
        return from + (target - from) * easing.apply(progress());
    }

    public float target() {
        return target;
    }

    public boolean done() {
        return progress() >= 1f;
    }

    private float progress() {
        float elapsed = (System.nanoTime() - startedAt) / 1_000_000_000f;
        return Math.max(0f, Math.min(1f, elapsed / duration));
    }
}
