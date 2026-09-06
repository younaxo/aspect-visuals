package su.aspectvisuals.client.music;

import su.aspectvisuals.client.AspectVisuals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Трек из системного медиа-API Windows.
 *
 * Windows ведёт список сеансов проигрывания: туда попадает всё, что сообщает
 * системе о воспроизведении — Spotify, браузер, любой плеер. Это снимает
 * привязку к конкретному сервису и не требует ни авторизации, ни подписки.
 *
 * Запрос идёт отдельным процессом на фоновом потоке: обращение к API
 * асинхронное и небыстрое, а кадр ждать не может. Опрос редкий — трек
 * меняется раз в минуты, а не в кадры.
 */
public final class WindowsMediaSource implements MediaSource {
    private static final long PERIOD_MS = 2000;
    private static final long TIMEOUT_S = 4;

    /**
     * Запрос к системному API. Ожидание асинхронных операций делается через
     * преобразование в задачу: иначе результат недоступен из сценария.
     */
    private static final String QUERY = String.join("; ",
            "$ErrorActionPreference='Stop'",
            "Add-Type -AssemblyName System.Runtime.WindowsRuntime",
            "$as=([System.WindowsRuntimeSystemExtensions].GetMethods()|"
                    + "?{$_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and "
                    + "$_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'})[0]",
            "function Await($t,$rt){$m=$as.MakeGenericMethod($rt);$n=$m.Invoke($null,@($t));"
                    + "$n.Wait(-1)|Out-Null;$n.Result}",
            "$mgr=Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,"
                    + "Windows.Media,ContentType=WindowsRuntime]::RequestAsync()) "
                    + "([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])",
            "$s=$mgr.GetCurrentSession()",
            "if($s){$p=Await ($s.TryGetMediaPropertiesAsync()) "
                    + "([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties]);"
                    + "$t=$s.GetTimelineProperties();$b=$s.GetPlaybackInfo();"
                    + "\"$($p.Title)`t$($p.Artist)`t$($t.Position.TotalSeconds)`t"
                    + "$($t.EndTime.TotalSeconds)`t$($b.PlaybackStatus)\"}");

    private final AtomicReference<MusicTrack> track = new AtomicReference<>();
    private final Thread worker;
    private volatile boolean running = true;
    private volatile boolean supported = true;
    private boolean reported;

    public WindowsMediaSource() {
        worker = new Thread(this::loop, "aspect-media");
        worker.setDaemon(true);
        worker.start();
    }

    public static boolean windows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    @Override
    public MusicTrack current() {
        return track.get();
    }

    @Override
    public boolean available() {
        return supported;
    }

    @Override
    public void close() {
        running = false;
        worker.interrupt();
    }

    private void loop() {
        while (running) {
            try {
                track.set(query());
                Thread.sleep(PERIOD_MS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception error) {
                fail(error.toString());
                return;
            }
        }
    }

    private MusicTrack query() throws Exception {
        ProcessBuilder builder = new ProcessBuilder(
                "powershell.exe", "-NoProfile", "-NonInteractive", "-Command", QUERY);
        builder.redirectErrorStream(false);
        Process process = builder.start();

        String line;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            line = reader.readLine();
        }
        if (!process.waitFor(TIMEOUT_S, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            return track.get();
        }
        if (process.exitValue() != 0) {
            fail("запрос вернул код " + process.exitValue());
            return null;
        }
        return parse(line);
    }

    private static MusicTrack parse(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split("\t", -1);
        if (parts.length < 5) {
            return null;
        }
        return new MusicTrack(parts[0].trim(), parts[1].trim(),
                number(parts[2]), number(parts[3]), "Playing".equalsIgnoreCase(parts[4].trim()));
    }

    private static double number(String value) {
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /** Источник отключается насовсем: повторять неработающий запрос незачем. */
    private void fail(String reason) {
        supported = false;
        running = false;
        track.set(null);
        if (!reported) {
            reported = true;
            AspectVisuals.LOGGER.info("Системный источник музыки недоступен: {}", reason);
        }
    }
}
