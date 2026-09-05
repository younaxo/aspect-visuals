package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;
import su.aspectvisuals.client.util.Animation;

import java.util.List;
import java.util.function.IntConsumer;

/** Selector из UI Kit: пилюля с бегущим выделением активной вкладки. */
public class TabBar extends Component {
    private static final float PADDING = 4f;
    private static final float TAB_PADDING = 14f;

    private final List<String> tabs;
    private final Animation indicator = new Animation(0.18f);
    private IntConsumer onSelect = index -> {
    };
    private int selected;

    public TabBar(List<String> tabs, float height) {
        super(0f, height);
        this.tabs = List.copyOf(tabs);
        this.width = measure();
    }

    public TabBar onSelect(IntConsumer listener) {
        this.onSelect = listener;
        return this;
    }

    public int selected() {
        return selected;
    }

    public void select(int index) {
        if (index >= 0 && index < tabs.size()) {
            selected = index;
            onSelect.accept(index);
        }
    }

    private float measure() {
        float total = PADDING * 2;
        for (String tab : tabs) {
            total += AspectFont.MEDIUM.width(tab) + TAB_PADDING * 2;
        }
        return total;
    }

    private float tabWidth(int index) {
        return AspectFont.MEDIUM.width(tabs.get(index)) + TAB_PADDING * 2;
    }

    private float tabX(int index) {
        float offset = x + PADDING;
        for (int i = 0; i < index; i++) {
            offset += tabWidth(i);
        }
        return offset;
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Render2D.roundedRect(context, x, y, width, height, height / 2f, AspectColors.SURFACE_INPUT);
        Render2D.border(context, x, y, width, height, 0.5f, AspectColors.SURFACE_BORDER);

        indicator.target(selected);
        float position = indicator.value();
        int from = (int) Math.floor(position);
        int to = Math.min(tabs.size() - 1, from + 1);
        float blend = position - from;

        float highlightX = tabX(from) + (tabX(to) - tabX(from)) * blend;
        float highlightWidth = tabWidth(from) + (tabWidth(to) - tabWidth(from)) * blend;
        float inner = height - PADDING * 2;
        Render2D.roundedRect(context, highlightX, y + PADDING, highlightWidth, inner, inner / 2f, AspectColors.ACCENT_PRIMARY);

        for (int i = 0; i < tabs.size(); i++) {
            float centerX = tabX(i) + tabWidth(i) / 2f;
            boolean active = i == selected;
            boolean hover = Render2D.hovered(mouseX, mouseY, tabX(i), y, tabWidth(i), height);
            int color = active
                    ? AspectColors.STRONG_BLACK
                    : (hover ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_SECONDARY);
            AspectFont.MEDIUM.drawCentered(context, tabs.get(i), centerX,
                    y + (height - AspectFont.MEDIUM.lineHeight()) / 2f, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !hovered(mouseX, mouseY)) {
            return false;
        }
        for (int i = 0; i < tabs.size(); i++) {
            if (Render2D.hovered(mouseX, mouseY, tabX(i), y, tabWidth(i), height)) {
                select(i);
                return true;
            }
        }
        return true;
    }
}
