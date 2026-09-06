package su.aspectvisuals.client.account;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.account.api.AspectApiClient;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

/**
 * Аватар грузится один раз на профиль и живёт как обычная текстура.
 * Повторная загрузка происходит только при смене адреса или выходе из аккаунта.
 */
public final class AvatarCache {
    private static final int MAX_BYTES = 512 * 1024;
    private static final int MAX_SIDE = 512;
    private static final Identifier TEXTURE = AspectVisuals.id("textures/dynamic/avatar");

    private String loadedUrl;
    private boolean loading;
    private boolean ready;
    private int width;
    private int height;

    public boolean ready() {
        return ready;
    }

    public Identifier texture() {
        return TEXTURE;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void load(AspectApiClient api, String url) {
        if (url == null || url.isBlank()) {
            clear();
            return;
        }
        if (url.equals(loadedUrl) || loading) {
            return;
        }

        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException error) {
            AspectVisuals.LOGGER.warn("Некорректный адрес аватара");
            return;
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        boolean localDev = host.equals("localhost") || host.equals("127.0.0.1");
        if (!scheme.equals("https") && !(scheme.equals("http") && localDev)) {
            AspectVisuals.LOGGER.warn("Аватар не загружен: адрес не по HTTPS");
            return;
        }

        loading = true;
        api.download(uri, MAX_BYTES).whenComplete((bytes, error) -> MinecraftClient.getInstance().execute(() -> {
            loading = false;
            if (error != null || bytes == null) {
                AspectVisuals.LOGGER.warn("Не удалось загрузить аватар");
                return;
            }
            apply(bytes, url);
        }));
    }

    private void apply(byte[] bytes, String url) {
        NativeImage image;
        try {
            image = NativeImage.read(bytes);
        } catch (IOException error) {
            AspectVisuals.LOGGER.warn("Аватар не распознан как изображение");
            return;
        }

        if (image.getWidth() > MAX_SIDE || image.getHeight() > MAX_SIDE) {
            AspectVisuals.LOGGER.warn("Аватар слишком большой: {}x{}", image.getWidth(), image.getHeight());
            image.close();
            return;
        }

        width = image.getWidth();
        height = image.getHeight();
        // Текстура забирает изображение себе и освобождает его при destroyTexture
        MinecraftClient.getInstance().getTextureManager()
                .registerTexture(TEXTURE, new NativeImageBackedTexture(image));
        loadedUrl = url;
        ready = true;
    }

    public void clear() {
        if (ready) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(TEXTURE);
        }
        loadedUrl = null;
        width = 0;
        height = 0;
        ready = false;
    }
}
