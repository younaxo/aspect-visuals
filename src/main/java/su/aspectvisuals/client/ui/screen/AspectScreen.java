package su.aspectvisuals.client.ui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.ui.component.Component;
import su.aspectvisuals.client.ui.component.IconButton;
import su.aspectvisuals.client.ui.component.ProfileControl;
import su.aspectvisuals.client.ui.component.TabBar;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.theme.AspectColors;

import java.util.ArrayList;
import java.util.List;

/**
 * Общий каркас интерфейса из макета: затемнение, верхняя панель,
 * профиль и переключатель режимов Peek / Panel.
 *
 * Раскладка считается от текущего размера экрана, а не от 1920x1080:
 * панель одинаково собирается на 1280x720 и на ультраширокой картинке.
 */
public class AspectScreen extends Screen {
    public enum Mode {
        PEEK,
        PANEL
    }

    protected static final float EDGE = 24f;
    protected static final float TOP = 24f;
    protected static final float BAR_HEIGHT = 32f;

    private static Mode mode = Mode.PEEK;
    private static int activeTab;

    private final List<Component> components = new ArrayList<>();

    private ProfileControl profile;
    private TabBar sections;
    private TabBar modes;
    private ScreenView view;

    public AspectScreen() {
        super(Text.literal(AspectVisuals.NAME));
    }

    public static Mode mode() {
        return mode;
    }

    @Override
    protected void init() {
        components.clear();

        profile = new ProfileControl(AspectVisuals.account());
        profile.bounds(EDGE + BAR_HEIGHT + 8f, TOP);
        components.add(profile);

        components.add((IconButton) new IconButton(Icons.LOGO, BAR_HEIGHT, true, this::close)
                .bounds(EDGE, TOP));

        sections = new TabBar(List.of("Settings", "Friends", "Waypoints", "Configs"), BAR_HEIGHT);
        sections.bounds((width - sections.width()) / 2f, TOP);
        sections.select(activeTab);
        sections.onSelect(index -> {
            activeTab = index;
            rebuildView();
        });
        components.add(sections);

        modes = new TabBar(List.of("Peek", "Panel"), BAR_HEIGHT);
        modes.bounds(width - EDGE - modes.width(), TOP);
        modes.select(mode == Mode.PEEK ? 0 : 1);
        modes.onSelect(index -> {
            mode = index == 0 ? Mode.PEEK : Mode.PANEL;
            rebuildView();
        });
        components.add(modes);

        components.add((IconButton) new IconButton(Icons.LAYOUT, BAR_HEIGHT, true, this::openHudEditor)
                .bounds(width - EDGE - modes.width() - 8f - BAR_HEIGHT, TOP));

        AspectVisuals.account().ensureFresh();
        rebuildView();
    }

    private void openHudEditor() {
        if (client != null) {
            client.setScreen(new HudEditorScreen(this));
        }
    }

    private void rebuildView() {
        view = switch (activeTab) {
            case 1 -> new PlaceholderView("Friends", "Список друзей появится вместе с обновлением сайта");
            case 2 -> new WaypointsView();
            case 3 -> new ConfigsView();
            default -> new ModulesView(mode);
        };
        view.layout(width, height, contentTop(), contentBottom());
    }

    protected float contentTop() {
        return TOP + BAR_HEIGHT + 16f;
    }

    protected float contentBottom() {
        return height - EDGE;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Backdrop.render(context, width, height);

        if (view != null) {
            view.render(context, mouseX, mouseY, delta);
        }

        for (Component component : components) {
            component.draw(context, mouseX, mouseY, delta);
        }

        if (view != null) {
            view.renderOverlay(context, mouseX, mouseY);
        }
        profile.renderCard(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (profile.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (view != null && view.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (Component component : components) {
            if (component != profile && component.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (view != null) {
            view.mouseReleased(mouseX, mouseY, button);
        }
        for (Component component : components) {
            component.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (view != null && view.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        for (Component component : components) {
            if (component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (view != null && view.mouseScrolled(mouseX, mouseY, vertical)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (view != null && view.keyPressed(key, scancode, modifiers)) {
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(char symbol, int modifiers) {
        if (view != null && view.charTyped(symbol, modifiers)) {
            return true;
        }
        return super.charTyped(symbol, modifiers);
    }

    @Override
    public void close() {
        AspectVisuals.config().save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
