package su.aspectvisuals.client.ui.screen;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/** Раздел, для которого на бэкенде ещё нет данных. */
public class PlaceholderView implements ScreenView {
    private static final float WIDTH = 320f;
    private static final float HEIGHT = 120f;

    private final String title;
    private final String text;

    private float x;
    private float y;

    public PlaceholderView(String title, String text) {
        this.title = title;
        this.text = text;
    }

    @Override
    public void layout(int screenWidth, int screenHeight, float top, float bottom) {
        x = (screenWidth - WIDTH) / 2f;
        y = top + (bottom - top - HEIGHT) / 2f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Render2D.filledBorder(context, x, y, WIDTH, HEIGHT,
                12f, AspectColors.SURFACE_MAIN_GLASS, 0.5f, AspectColors.SURFACE_BORDER);

        Render2D.texture(context, Icons.HANDSHAKE, x + WIDTH / 2f - 12f, y + 24f, 24f, AspectColors.TEXT_TERTIARY);
        AspectFont.SEMIBOLD.drawCentered(context, title, x + WIDTH / 2f, y + 58f, AspectColors.TEXT_PRIMARY);
        AspectFont.MEDIUM.drawCentered(context, AspectFont.MEDIUM.clip(text, (int) WIDTH - 32),
                x + WIDTH / 2f, y + 78f, AspectColors.TEXT_TERTIARY);
    }
}
