package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/** Button Circle из UI Kit: круглая кнопка 32x32 или иконка 16x16 без фона. */
public class IconButton extends Component {
    private final Identifier icon;
    private final Runnable action;
    private final boolean filled;
    private final float iconSize;

    public IconButton(Identifier icon, float size, boolean filled, Runnable action) {
        super(size, size);
        this.icon = icon;
        this.action = action;
        this.filled = filled;
        this.iconSize = filled ? size * 0.5f : size;
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hover = hovered(mouseX, mouseY);

        if (filled) {
            int background = hover ? AspectColors.SURFACE_CARD_HOVER : AspectColors.SURFACE_CARD;
            Render2D.roundedRect(context, x, y, width, height, width / 2f, background);
            Render2D.border(context, x, y, width, height, 0.5f, AspectColors.SURFACE_BORDER);
        }

        int tint = enabled()
                ? (hover ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_SECONDARY)
                : AspectColors.TEXT_DISABLED;
        Render2D.texture(context, icon, x + (width - iconSize) / 2f, y + (height - iconSize) / 2f, iconSize, tint);
    }

    @Override
    protected void onPress(double mouseX, double mouseY) {
        action.run();
    }
}
