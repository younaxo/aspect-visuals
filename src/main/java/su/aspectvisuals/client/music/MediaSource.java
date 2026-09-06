package su.aspectvisuals.client.music;

/** Источник сведений о проигрываемом треке. */
public interface MediaSource {
    /** Текущий трек или null, если ничего не играет. */
    MusicTrack current();

    /** Доступен ли источник в этой системе. */
    boolean available();

    void close();
}
