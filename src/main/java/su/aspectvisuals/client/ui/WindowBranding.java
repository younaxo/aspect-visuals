package su.aspectvisuals.client.ui;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;
import su.aspectvisuals.client.AspectVisuals;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Значок и заголовок окна игры.
 *
 * Изображение читается стандартным декодером Java, а не загрузчиком текстур
 * игры: значок нужен окну, а не рендеру, и в момент установки менеджер
 * текстур может быть ещё не готов.
 *
 * Пиксели передаются в оконную библиотеку напрямую. Штатный путь игры
 * принимает набор ресурсов с заранее известными путями значков и своим
 * изображением не заменяется.
 */
public final class WindowBranding {
    /**
     * Значок приложения, а не знак для интерфейса: у него собственная
     * подложка. Знак из logo.png белый на прозрачном фоне — он рассчитан на
     * тёмные панели клиента и на светлой панели задач был бы не виден.
     */
    private static final String ICON = "/assets/aspectvisuals/textures/app_icon.png";

    private WindowBranding() {
    }

    public static String title() {
        return AspectVisuals.NAME + " | Minecraft " + gameVersion();
    }

    private static String gameVersion() {
        return net.minecraft.SharedConstants.getGameVersion().getName();
    }

    public static void applyIcon() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return;
        }

        BufferedImage image = read();
        if (image == null) {
            return;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * 4);
        GLFWImage.Buffer icons = GLFWImage.malloc(1);
        try {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);
                    pixels.put((byte) ((argb >> 16) & 0xFF));
                    pixels.put((byte) ((argb >> 8) & 0xFF));
                    pixels.put((byte) (argb & 0xFF));
                    pixels.put((byte) ((argb >>> 24) & 0xFF));
                }
            }
            pixels.flip();

            icons.position(0).width(width).height(height).pixels(pixels);
            GLFW.glfwSetWindowIcon(client.getWindow().getHandle(), icons);
        } finally {
            // Библиотека копирует изображение себе, поэтому память освобождается сразу
            icons.free();
            MemoryUtil.memFree(pixels);
        }
    }

    private static BufferedImage read() {
        try (InputStream stream = WindowBranding.class.getResourceAsStream(ICON)) {
            if (stream == null) {
                AspectVisuals.LOGGER.warn("Значок окна не найден: {}", ICON);
                return null;
            }
            return ImageIO.read(stream);
        } catch (Exception error) {
            AspectVisuals.LOGGER.warn("Значок окна не прочитан: {}", error.toString());
            return null;
        }
    }
}
