package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

public class TextButton extends Component {
    private final Identifier icon;
    private final Runnable action;
    private final boolean accent;

    private String label;

    public TextButton(String label, Identifier icon, float width, float height, boolean accent, Runnable action) {
        super(width, height);
        this.label = label;
        this.icon = icon;
        this.accent = accent;
        this.action = action;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hover = hovered(mouseX, mouseY);

        int background;
        int textColor;
        if (accent) {
            background = hover ? AspectColors.ACCENT_PRIMARY : AspectColors.withAlpha(AspectColors.ACCENT_PRIMARY, 0.9f);
            textColor = AspectColors.STRONG_BLACK;
        } else {
            background = hover ? AspectColors.SURFACE_CARD_HOVER : AspectColors.SURFACE_CARD;
            textColor = enabled() ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_DISABLED;
        }

        Render2D.filledBorder(context, x, y, width, height,
                12f, background, 0.5f, AspectColors.SURFACE_BORDER);

        float contentWidth = AspectFont.SEMIBOLD.width(label) + (icon != null ? 16f + 6f : 0f);
        float contentX = x + (width - contentWidth) / 2f;

        if (icon != null) {
            Render2D.texture(context, icon, contentX, y + (height - 16f) / 2f, 16f, textColor);
            contentX += 16f + 6f;
        }
        AspectFont.SEMIBOLD.draw(context, label, contentX, y + (height - AspectFont.SEMIBOLD.lineHeight()) / 2f, textColor);
    }

    @Override
    protected void onPress(double mouseX, double mouseY) {
        if (enabled()) {
            action.run();
        }
    }
}
