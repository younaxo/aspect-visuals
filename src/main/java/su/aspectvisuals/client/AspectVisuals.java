package su.aspectvisuals.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.aspectvisuals.client.account.AccountManager;
import su.aspectvisuals.client.config.ConfigManager;
import su.aspectvisuals.client.hud.HudManager;
import su.aspectvisuals.client.module.ModuleManager;
import su.aspectvisuals.client.ui.Keybinds;
import su.aspectvisuals.client.ui.render.UiTextures;
import su.aspectvisuals.client.waypoint.WaypointStore;

public final class AspectVisuals implements ClientModInitializer {
    public static final String MOD_ID = "aspectvisuals";
    public static final String NAME = "Aspect Visuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    private static ModuleManager modules;
    private static HudManager hud;
    private static ConfigManager config;
    private static AccountManager account;
    private static WaypointStore waypoints;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static String version() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("dev");
    }

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        hud = new HudManager(modules);
        config = new ConfigManager(modules);
        account = new AccountManager(version());
        waypoints = new WaypointStore();

        config.load();
        waypoints.load();
        account.load();

        Keybinds.register();
        UiTextures.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Keybinds.tick(client);
            modules.tick();
            account.tick();
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> hud.render(context));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            config.save();
            waypoints.save();
        });

        LOGGER.info("{} загружен: модулей {}", NAME, modules.all().size());
    }

    public static ModuleManager modules() {
        return modules;
    }

    public static HudManager hud() {
        return hud;
    }

    public static ConfigManager config() {
        return config;
    }

    public static AccountManager account() {
        return account;
    }

    public static WaypointStore waypoints() {
        return waypoints;
    }
}
