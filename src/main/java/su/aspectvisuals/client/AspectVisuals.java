package su.aspectvisuals.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AspectVisuals implements ClientModInitializer {
    public static final String MOD_ID = "aspectvisuals";
    public static final String NAME = "Aspect Visuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("{} загружен", NAME);
    }
}
