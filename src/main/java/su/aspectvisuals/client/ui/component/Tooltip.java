package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

public final class Tooltip {
    private static final float PADDING = 8f;

    private Tooltip() {
    }

    public static void draw(DrawContext context, String text, int mouseX, int mouseY, int screenWidth) {
        if (text == null || text.isBlank()) {
            return;
        }

        float width = AspectFont.MEDIUM.width(text) + PADDING * 2;
        float height = AspectFont.MEDIUM.lineHeight() + PADDING;
        float x = Math.min(mouseX + 12f, screenWidth - width - 4f);
        float y = mouseY - height - 6f;

        Render2D.roundedRect(context, x, y, width, height, 8f, AspectColors.SURFACE_CARD);
        Render2D.border(context, x, y, width, height, 0.5f, AspectColors.SURFACE_BORDER);
        AspectFont.MEDIUM.draw(context, text, x + PADDING, y + PADDING / 2f, AspectColors.TEXT_SECONDARY);
    }
}
