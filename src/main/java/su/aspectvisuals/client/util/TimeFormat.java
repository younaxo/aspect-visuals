package su.aspectvisuals.client.util;

public final class TimeFormat {
    private TimeFormat() {
    }

    /** Тики в компактную запись вида 1m44s, как в макете HUD. */
    public static String ticks(int ticks) {
        int seconds = Math.max(0, ticks) / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        return minutes > 0 ? minutes + "m" + seconds + "s" : seconds + "s";
    }
}
