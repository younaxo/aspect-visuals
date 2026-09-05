package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.setting.EnumSetting;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;
import su.aspectvisuals.client.util.Animation;

import java.util.List;

public class Dropdown extends Component {
    private static final float ROW_HEIGHT = 20f;
    private static final float PADDING = 8f;

    private final EnumSetting setting;
    private final Animation open = new Animation(0.16f);
    private boolean expanded;

    public Dropdown(EnumSetting setting, float width) {
        super(width, 20f);
        this.setting = setting;
    }

    public boolean expanded() {
        return expanded;
    }

    /** Высота выпадающего списка нужна экрану, чтобы зарезервировать место. */
    public float overlayHeight() {
        return expanded ? setting.options().size() * ROW_HEIGHT + 4f : 0f;
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hover = hovered(mouseX, mouseY);
        Render2D.roundedRect(context, x, y, width, height, 8f,
                hover ? AspectColors.SURFACE_CARD_HOVER : AspectColors.SURFACE_INPUT);
        Render2D.border(context, x, y, width, height, 0.5f, AspectColors.SURFACE_BORDER);

        float textY = y + (height - AspectFont.MEDIUM.lineHeight()) / 2f;
        AspectFont.MEDIUM.draw(context, setting.get(), x + PADDING, textY, AspectColors.TEXT_PRIMARY);
        Render2D.texture(context, Icons.DOWN, x + width - PADDING - 10f, y + (height - 10f) / 2f, 10f, AspectColors.TEXT_SECONDARY);
    }

    /** Список рисуется отдельным проходом поверх остальных карточек. */
    public void renderOverlay(DrawContext context, int mouseX, int mouseY) {
        open.target(expanded ? 1f : 0f);
        float progress = open.eased();
        if (progress < 0.02f) {
            return;
        }

        List<String> options = setting.options();
        float listHeight = options.size() * ROW_HEIGHT * progress;
        float listY = y + height + 4f;

        Render2D.roundedRect(context, x, listY, width, listHeight, 8f, AspectColors.SURFACE_CARD);
        Render2D.border(context, x, listY, width, listHeight, 0.5f, AspectColors.SURFACE_BORDER);

        context.enableScissor(Math.round(x), Math.round(listY), Math.round(x + width), Math.round(listY + listHeight));
        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            float rowY = listY + i * ROW_HEIGHT;
            boolean hover = Render2D.hovered(mouseX, mouseY, x, rowY, width, ROW_HEIGHT);

            if (hover) {
                Render2D.roundedRect(context, x + 2f, rowY + 1f, width - 4f, ROW_HEIGHT - 2f, 6f, AspectColors.ACCENT_GLOW);
            }

            int color = setting.is(option) ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_SECONDARY;
            AspectFont.MEDIUM.draw(context, option, x + PADDING, rowY + (ROW_HEIGHT - AspectFont.MEDIUM.lineHeight()) / 2f, color);

            if (setting.is(option)) {
                Render2D.texture(context, Icons.CHECK, x + width - PADDING - 10f, rowY + (ROW_HEIGHT - 10f) / 2f, 10f, AspectColors.TEXT_PRIMARY);
            }
        }
        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        if (expanded) {
            List<String> options = setting.options();
            float listY = y + height + 4f;
            for (int i = 0; i < options.size(); i++) {
                if (Render2D.hovered(mouseX, mouseY, x, listY + i * ROW_HEIGHT, width, ROW_HEIGHT)) {
                    setting.select(i);
                    expanded = false;
                    return true;
                }
            }
        }

        if (hovered(mouseX, mouseY)) {
            expanded = !expanded;
            return true;
        }

        if (expanded) {
            expanded = false;
            return true;
        }
        return false;
    }
}
