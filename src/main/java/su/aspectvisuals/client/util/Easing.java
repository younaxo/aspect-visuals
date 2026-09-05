package su.aspectvisuals.client.util;

import java.util.function.DoubleUnaryOperator;

public enum Easing {
    LINEAR(t -> t),
    OUT(t -> 1 - Math.pow(1 - t, 3)),
    IN_OUT(t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2);

    private final DoubleUnaryOperator curve;

    Easing(DoubleUnaryOperator curve) {
        this.curve = curve;
    }

    public float apply(float progress) {
        float clamped = Math.max(0f, Math.min(1f, progress));
        return (float) curve.applyAsDouble(clamped);
    }
}
