package su.aspectvisuals.client.music;

/**
 * Играющий трек.
 *
 * @param position сколько секунд проиграно
 * @param duration длительность в секундах; ноль означает, что она неизвестна
 */
public record MusicTrack(String title, String artist, double position, double duration, boolean playing) {

    public float progress() {
        return duration <= 0 ? 0f : (float) Math.max(0, Math.min(1, position / duration));
    }
}
