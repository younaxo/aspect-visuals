package su.aspectvisuals.client.ui.screen;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.config.ConfigManager;
import su.aspectvisuals.client.ui.component.IconButton;
import su.aspectvisuals.client.ui.component.TextField;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

import java.util.List;

/** Список локальных конфигураций клиента: создание, применение и удаление. */
public class ConfigsView implements ScreenView {
    private static final float WIDTH = 420f;
    private static final float ROW_HEIGHT = 40f;
    private static final float PADDING = 16f;

    private final TextField name = new TextField("Название конфига", Icons.CONFIGS, 260f, 32f, 32);
    private IconButton save;

    private float x;
    private float y;
    private float height;
    private float scroll;
    private String status = "";

    private ConfigManager configs() {
        return AspectVisuals.config();
    }

    @Override
    public void layout(int screenWidth, int screenHeight, float top, float bottom) {
        x = (screenWidth - WIDTH) / 2f;
        y = top + 24f;
        height = Math.max(ROW_HEIGHT * 2, bottom - y - 64f);

        name.bounds(x + PADDING, y + height + 12f);
        save = (IconButton) new IconButton(Icons.PLUS, 32f, true, this::create)
                .bounds(x + PADDING + name.width() + 8f, y + height + 12f);
    }

    private void create() {
        if (name.value().isBlank()) {
            return;
        }
        status = configs().saveNamed(name.value().trim()) ? "Конфиг сохранён" : "Не удалось сохранить";
        name.setValue("");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Render2D.filledBorder(context, x, y, WIDTH, height,
                12f, AspectColors.SURFACE_MAIN_GLASS, 0.5f, AspectColors.SURFACE_BORDER);

        List<String> presets = configs().presets();
        if (presets.isEmpty()) {
            AspectFont.MEDIUM.drawCentered(context, "Сохранённых конфигов нет", x + WIDTH / 2f, y + height / 2f,
                    AspectColors.TEXT_TERTIARY);
        }

        Render2D.pushClip(context, x, y, WIDTH, height);
        for (int i = 0; i < presets.size(); i++) {
            String preset = presets.get(i);
            float rowY = y + PADDING + i * ROW_HEIGHT - scroll;
            if (rowY + ROW_HEIGHT < y || rowY > y + height) {
                continue;
            }

            boolean hover = Render2D.hovered(mouseX, mouseY, x + PADDING, rowY, WIDTH - PADDING * 2, ROW_HEIGHT - 6f);
            if (hover) {
                Render2D.roundedRect(context, x + PADDING, rowY, WIDTH - PADDING * 2, ROW_HEIGHT - 6f, 8f,
                        AspectColors.SURFACE_CARD_HOVER);
            }

            Render2D.texture(context, Icons.CONFIGS, x + PADDING + 8f, rowY + 9f, 16f, AspectColors.TEXT_SECONDARY);
            AspectFont.SEMIBOLD.draw(context, preset, x + PADDING + 32f, rowY + 11f, AspectColors.TEXT_PRIMARY);

            Render2D.texture(context, Icons.TRASH, x + WIDTH - PADDING - 24f, rowY + 9f, 16f, AspectColors.TEXT_TERTIARY);
        }
        Render2D.popClip(context);

        if (!status.isEmpty()) {
            AspectFont.MEDIUM.draw(context, status, x + PADDING, y + height - 20f, AspectColors.SYSTEM_INFO);
        }

        name.draw(context, mouseX, mouseY, delta);
        save.draw(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (name.mouseClicked(mouseX, mouseY, button) || save.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        List<String> presets = configs().presets();
        for (int i = 0; i < presets.size(); i++) {
            float rowY = y + PADDING + i * ROW_HEIGHT - scroll;

            if (Render2D.hovered(mouseX, mouseY, x + WIDTH - PADDING - 24f, rowY + 9f, 16f, 16f)) {
                configs().deleteNamed(presets.get(i));
                status = "Конфиг удалён";
                return true;
            }
            if (Render2D.hovered(mouseX, mouseY, x + PADDING, rowY, WIDTH - PADDING * 2, ROW_HEIGHT - 6f)) {
                status = configs().loadNamed(presets.get(i)) ? "Конфиг применён" : "Не удалось применить";
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return save.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!Render2D.hovered(mouseX, mouseY, x, y, WIDTH, height)) {
            return false;
        }
        float content = configs().presets().size() * ROW_HEIGHT + PADDING * 2;
        scroll = Math.max(0f, Math.min(Math.max(0f, content - height), scroll - (float) amount * 20f));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        return name.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(char symbol, int modifiers) {
        return name.charTyped(symbol, modifiers);
    }
}
