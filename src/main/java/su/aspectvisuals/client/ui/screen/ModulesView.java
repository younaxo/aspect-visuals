package su.aspectvisuals.client.ui.screen;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.Module;
import su.aspectvisuals.client.ui.component.Component;
import su.aspectvisuals.client.ui.component.TabBar;
import su.aspectvisuals.client.ui.component.TextField;
import su.aspectvisuals.client.ui.component.Toggle;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;
import su.aspectvisuals.client.util.Animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Сетка модулей и панель настроек — основное содержимое вкладки Settings.
 *
 * Peek: карточки идут в три колонки, поиск по центру над сеткой.
 * Panel: две колонки в одной стеклянной панели, поиск под ней.
 */
public class ModulesView implements ScreenView {
    private static final float CARD_WIDTH = 256f;
    private static final float CARD_HEIGHT = 69f;
    private static final float CARD_GAP = 8f;
    private static final float SEARCH_WIDTH = 310f;
    private static final float SEARCH_HEIGHT = 40f;
    private static final float PANEL_PADDING = 16f;

    private final AspectScreen.Mode mode;
    private final TextField search;
    private final TabBar categories;
    private final List<Toggle> toggles = new ArrayList<>();
    private final Animation appear = new Animation(0.2f);

    private List<Module> shown = List.of();
    private Module selected;
    private SettingsPanel panel;

    private float gridX;
    private float gridY;
    private float gridHeight;
    private int columns;
    private float scroll;
    private float maxScroll;

    public ModulesView(AspectScreen.Mode mode) {
        this.mode = mode;
        this.search = (TextField) new TextField("Search modules…", Icons.SEARCH, SEARCH_WIDTH, SEARCH_HEIGHT, 48)
                .setVisible(true);
        this.search.onChange(value -> refresh());
        this.categories = new TabBar(List.of("HUD", "Info", "Visual", "All"), 26f).onSelect(index -> refresh());
        this.categories.select(3);
        appear.target(1f);
    }

    private Category category() {
        return switch (categories.selected()) {
            case 0 -> Category.HUD;
            case 1 -> Category.INFO;
            case 2 -> Category.VISUAL;
            default -> null;
        };
    }

    private void refresh() {
        shown = AspectVisuals.modules().search(search.value(), category());

        toggles.clear();
        for (Module module : shown) {
            toggles.add(new Toggle(enabledSetting(module)));
        }
        scroll = 0f;
        recomputeScroll();
    }

    /** Тумблер работает не с настройкой, а с самим модулем, поэтому обёртка. */
    private su.aspectvisuals.client.setting.BooleanSetting enabledSetting(Module module) {
        return new su.aspectvisuals.client.setting.BooleanSetting(module.name(), module.description(), module.enabled()) {
            @Override
            public Boolean get() {
                return module.enabled();
            }

            @Override
            public void set(Boolean value) {
                module.setEnabled(value);
            }
        };
    }

    @Override
    public void layout(int screenWidth, int screenHeight, float top, float bottom) {
        columns = mode == AspectScreen.Mode.PEEK ? 3 : 2;
        float gridWidth = columns * CARD_WIDTH + (columns - 1) * CARD_GAP;

        if (mode == AspectScreen.Mode.PEEK) {
            search.bounds((screenWidth - SEARCH_WIDTH) / 2f, top + 96f);
            gridY = search.y() + SEARCH_HEIGHT + 24f;
            categories.bounds((screenWidth - categories.width()) / 2f, bottom - 26f);
            gridHeight = Math.max(CARD_HEIGHT, categories.y() - gridY - 12f);
        } else {
            float panelHeight = Math.min(bottom - top - 120f, 420f);
            gridY = top + 60f + 44f;
            gridHeight = Math.max(CARD_HEIGHT, panelHeight - 44f);
            categories.bounds((screenWidth + gridWidth) / 2f - categories.width() - PANEL_PADDING, top + 60f + 8f);
            search.bounds((screenWidth - SEARCH_WIDTH) / 2f, gridY + gridHeight + 24f);
        }

        gridX = (screenWidth - gridWidth) / 2f;

        if (shown.isEmpty()) {
            refresh();
        }
        recomputeScroll();

        if (selected != null) {
            openSettings(selected);
        }
    }

    private void recomputeScroll() {
        int rows = (int) Math.ceil(shown.size() / (float) Math.max(1, columns));
        float contentHeight = rows * CARD_HEIGHT + Math.max(0, rows - 1) * CARD_GAP;
        maxScroll = Math.max(0f, contentHeight - gridHeight);
        scroll = Math.max(0f, Math.min(scroll, maxScroll));
    }

    private void openSettings(Module module) {
        selected = module;
        panel = module.settings().isEmpty() ? null : new SettingsPanel(module, gridX - CARD_WIDTH - CARD_GAP * 2, gridY, gridHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float progress = appear.eased();
        float gridWidth = columns * CARD_WIDTH + (columns - 1) * CARD_GAP;

        if (mode == AspectScreen.Mode.PANEL) {
            float panelTop = categories.y() - 8f;
            float panelHeight = gridY + gridHeight - panelTop + PANEL_PADDING;
            Render2D.filledBorder(context, gridX - PANEL_PADDING, panelTop, gridWidth + PANEL_PADDING * 2, panelHeight,
                    12f, AspectColors.SURFACE_MAIN_GLASS, 0.5f, AspectColors.SURFACE_BORDER);
            Render2D.texture(context, Icons.LOGO, gridX, panelTop + 12f, 18f, AspectColors.ACCENT_PRIMARY);
        } else {
            Render2D.texture(context, Icons.LOGO, (gridX + gridWidth / 2f) - 14f, search.y() - 52f, 28f,
                    AspectColors.withAlpha(AspectColors.ACCENT_PRIMARY, progress));
        }

        search.draw(context, mouseX, mouseY, delta);
        categories.draw(context, mouseX, mouseY, delta);

        Render2D.pushClip(context, gridX, gridY, gridWidth, gridHeight);

        for (int i = 0; i < shown.size(); i++) {
            float cardX = gridX + (i % columns) * (CARD_WIDTH + CARD_GAP);
            float cardY = gridY + (i / columns) * (CARD_HEIGHT + CARD_GAP) - scroll;

            if (cardY + CARD_HEIGHT < gridY || cardY > gridY + gridHeight) {
                continue;
            }
            drawCard(context, shown.get(i), toggles.get(i), cardX, cardY, mouseX, mouseY, delta);
        }
        Render2D.popClip(context);

        if (shown.isEmpty()) {
            AspectFont.MEDIUM.drawCentered(context, "Ничего не найдено", gridX + gridWidth / 2f,
                    gridY + gridHeight / 2f, AspectColors.TEXT_TERTIARY);
        }

        if (panel != null) {
            panel.render(context, mouseX, mouseY, delta);
        }
    }

    private void drawCard(DrawContext context, Module module, Toggle toggle, float x, float y,
                          int mouseX, int mouseY, float delta) {
        boolean hover = Render2D.hovered(mouseX, mouseY, x, y, CARD_WIDTH, CARD_HEIGHT);
        boolean active = module.enabled();

        int background = hover
                ? AspectColors.SURFACE_CARD_HOVER
                : (active ? AspectColors.SURFACE_CARD : AspectColors.withAlpha(AspectColors.SURFACE_CARD, 0.75f));
        Render2D.filledBorder(context, x, y, CARD_WIDTH, CARD_HEIGHT,
                12f, background, 0.5f, active ? AspectColors.SURFACE_BORDER_TYPE : AspectColors.SURFACE_BORDER);

        AspectFont.SEMIBOLD.draw(context, module.name(), x + 16f, y + 16f,
                active ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_SECONDARY);
        AspectFont.MEDIUM.draw(context, AspectFont.MEDIUM.clip(module.description(), (int) CARD_WIDTH - 110),
                x + 16f, y + 34f, AspectColors.TEXT_TERTIARY);

        boolean settingsHover = Render2D.hovered(mouseX, mouseY, x + CARD_WIDTH - 84f, y + 20f, 16f, 16f);
        Render2D.texture(context, Icons.SETTINGS, x + CARD_WIDTH - 84f, y + 20f, 16f,
                settingsHover ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_TERTIARY);

        toggle.bounds(x + CARD_WIDTH - 56f, y + 18f);
        toggle.draw(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderOverlay(DrawContext context, int mouseX, int mouseY) {
        if (panel != null) {
            panel.renderOverlay(context, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (panel != null && panel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (search.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (categories.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        float gridWidth = columns * CARD_WIDTH + (columns - 1) * CARD_GAP;
        if (!Render2D.hovered(mouseX, mouseY, gridX, gridY, gridWidth, gridHeight)) {
            return false;
        }

        for (int i = 0; i < shown.size(); i++) {
            float cardX = gridX + (i % columns) * (CARD_WIDTH + CARD_GAP);
            float cardY = gridY + (i / columns) * (CARD_HEIGHT + CARD_GAP) - scroll;

            if (!Render2D.hovered(mouseX, mouseY, cardX, cardY, CARD_WIDTH, CARD_HEIGHT)) {
                continue;
            }

            if (toggles.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (Render2D.hovered(mouseX, mouseY, cardX + CARD_WIDTH - 84f, cardY + 20f, 16f, 16f)) {
                openSettings(shown.get(i));
                return true;
            }
            shown.get(i).toggle();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Component toggle : toggles) {
            toggle.mouseReleased(mouseX, mouseY, button);
        }
        return panel != null && panel.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return panel != null && panel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (panel != null && panel.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }

        float gridWidth = columns * CARD_WIDTH + (columns - 1) * CARD_GAP;
        if (!Render2D.hovered(mouseX, mouseY, gridX, gridY, gridWidth, gridHeight)) {
            return false;
        }

        scroll = Math.max(0f, Math.min(maxScroll, scroll - (float) amount * 24f));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (panel != null && panel.keyPressed(key, scancode, modifiers)) {
            return true;
        }
        return search.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(char symbol, int modifiers) {
        if (panel != null && panel.charTyped(symbol, modifiers)) {
            return true;
        }
        return search.charTyped(symbol, modifiers);
    }
}
