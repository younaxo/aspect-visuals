package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.theme.AspectColors;

/** Координаты из макета: подписи осей приглушены, значения — основным цветом. */
public class CoordinatesModule extends HudModule {
    private static final float GAP = 10f;

    public CoordinatesModule() {
        super("Coordinates", "Координаты игрока", Category.INFO, 0.02f, 0.95f, HudAnchor.BOTTOM_LEFT);
    }

    private BlockPos position() {
        return mc.player != null ? mc.player.getBlockPos() : BlockPos.ORIGIN;
    }

    private String[] parts() {
        BlockPos pos = position();
        return new String[]{String.valueOf(pos.getX()), String.valueOf(pos.getY()), String.valueOf(pos.getZ())};
    }

    @Override
    public float widgetWidth() {
        String[] values = parts();
        float total = 0f;
        String[] axes = {"X", "Y", "Z"};
        for (int i = 0; i < 3; i++) {
            total += AspectFont.MEDIUM.width(axes[i]) + 6f + AspectFont.SEMIBOLD.width(values[i]) + GAP;
        }
        return total - GAP;
    }

    @Override
    public float widgetHeight() {
        return AspectFont.MEDIUM.lineHeight();
    }

    @Override
    public void renderWidget(DrawContext context) {
        String[] values = parts();
        String[] axes = {"X", "Y", "Z"};
        float cursor = 0f;

        for (int i = 0; i < 3; i++) {
            AspectFont.MEDIUM.draw(context, axes[i], cursor, 0f, AspectColors.TEXT_TERTIARY);
            cursor += AspectFont.MEDIUM.width(axes[i]) + 6f;
            AspectFont.SEMIBOLD.draw(context, values[i], cursor, 0f, AspectColors.TEXT_PRIMARY);
            cursor += AspectFont.SEMIBOLD.width(values[i]) + GAP;
        }
    }
}
