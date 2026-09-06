package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.render.Icons;

import java.util.Locale;

public class BiomeModule extends HudModule {
    public BiomeModule() {
        super("Biome", "Текущий биом", Category.INFO, 0.02f, 0.28f, HudAnchor.TOP_LEFT);
    }

    private String value() {
        if (mc.world == null || mc.player == null) {
            return "—";
        }
        Identifier id = mc.world.getRegistryManager()
                .getOrThrow(RegistryKeys.BIOME)
                .getId(mc.world.getBiome(mc.player.getBlockPos()).value());
        if (id == null) {
            return "—";
        }
        String path = id.getPath().replace('_', ' ');
        return path.substring(0, 1).toUpperCase(Locale.ROOT) + path.substring(1);
    }

    @Override
    public float widgetWidth() {
        return HudCard.pillWidth(Icons.LAYOUT, value());
    }

    @Override
    public float widgetHeight() {
        return HudCard.pillHeight();
    }

    @Override
    public void renderWidget(DrawContext context) {
        HudCard.pill(context, 0f, 0f, Icons.LAYOUT, value());
    }
}
