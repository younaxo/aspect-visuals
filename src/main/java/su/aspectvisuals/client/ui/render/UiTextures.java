package su.aspectvisuals.client.ui.render;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.AspectVisuals;

import java.util.HashSet;
import java.util.Set;

/**
 * Фильтрация текстур интерфейса.
 *
 * Текстуры Minecraft создаются с точечной выборкой: для блоков это правильно,
 * для интерфейса — нет. Иконка, нарисованная в другом размере, получает
 * лесенку при увеличении и муар при уменьшении. Поэтому UI-текстурам
 * выставляется линейная фильтрация отдельно от текстур мира.
 *
 * Mipmap не включаем: ресурсные текстуры загружаются одним уровнем, и
 * минификационный фильтр по несуществующим уровням даёт чёрный результат.
 * Достаточную плотность обеспечивает исходник повышенного разрешения.
 */
public final class UiTextures {
    private static final Set<Identifier> configured = new HashSet<>();

    private UiTextures() {
    }

    public static void register() {
        // После перезагрузки ресурсов текстуры пересоздаются, настройки теряются
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return AspectVisuals.id("ui_textures");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        configured.clear();
                    }
                });
    }

    public static void ensureSmooth(Identifier texture) {
        if (texture == null || !configured.add(texture)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            configured.remove(texture);
            return;
        }

        AbstractTexture loaded = client.getTextureManager().getTexture(texture);
        if (loaded == null) {
            configured.remove(texture);
            return;
        }
        loaded.setFilter(true, false);
    }
}
