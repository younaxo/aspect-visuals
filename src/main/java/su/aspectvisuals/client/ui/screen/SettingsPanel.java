package su.aspectvisuals.client.ui.screen;

import net.minecraft.client.gui.DrawContext;
import su.aspectvisuals.client.module.Module;
import su.aspectvisuals.client.setting.BooleanSetting;
import su.aspectvisuals.client.setting.ColorSetting;
import su.aspectvisuals.client.setting.EnumSetting;
import su.aspectvisuals.client.setting.KeybindSetting;
import su.aspectvisuals.client.setting.MultiSelectSetting;
import su.aspectvisuals.client.setting.NumberSetting;
import su.aspectvisuals.client.setting.RangeSetting;
import su.aspectvisuals.client.setting.Setting;
import su.aspectvisuals.client.setting.StringSetting;
import su.aspectvisuals.client.ui.component.ColorPicker;
import su.aspectvisuals.client.ui.component.Component;
import su.aspectvisuals.client.ui.component.Dropdown;
import su.aspectvisuals.client.ui.component.KeybindPicker;
import su.aspectvisuals.client.ui.component.RangeSlider;
import su.aspectvisuals.client.ui.component.Slider;
import su.aspectvisuals.client.ui.component.TextField;
import su.aspectvisuals.client.ui.component.Toggle;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Панель настроек модуля из макета: слева подпись, справа элемент управления.
 * Тип контрола выбирается по типу настройки, а не задаётся вручную у модуля.
 */
public class SettingsPanel {
    private static final float WIDTH = 216f;
    private static final float PADDING = 16f;
    private static final float ROW_HEIGHT = 34f;
    private static final float CONTROL_WIDTH = 96f;

    private final Module module;
    private final Map<Setting<?>, Component> controls = new LinkedHashMap<>();
    private final List<Setting<?>> order = new ArrayList<>();

    private final float x;
    private final float y;
    private final float maxHeight;
    private float scroll;

    public SettingsPanel(Module module, float x, float y, float maxHeight) {
        this.module = module;
        this.x = Math.max(8f, x);
        this.y = y;
        this.maxHeight = maxHeight;
        build();
    }

    private void build() {
        for (Setting<?> setting : module.settings()) {
            Component control = createControl(setting);
            if (control != null) {
                order.add(setting);
                controls.put(setting, control);
            }
        }
        order.add(module.keybind());
        controls.put(module.keybind(), new KeybindPicker(module.keybind(), CONTROL_WIDTH));
    }

    private Component createControl(Setting<?> setting) {
        if (setting instanceof BooleanSetting value) {
            return new Toggle(value);
        }
        if (setting instanceof NumberSetting value) {
            return new Slider(value, CONTROL_WIDTH);
        }
        if (setting instanceof RangeSetting value) {
            return new RangeSlider(value, CONTROL_WIDTH);
        }
        if (setting instanceof EnumSetting value) {
            return new Dropdown(value, CONTROL_WIDTH);
        }
        if (setting instanceof ColorSetting value) {
            return new ColorPicker(value, CONTROL_WIDTH);
        }
        if (setting instanceof KeybindSetting value) {
            return new KeybindPicker(value, CONTROL_WIDTH);
        }
        if (setting instanceof StringSetting value) {
            TextField field = new TextField(value.placeholder(), null, CONTROL_WIDTH, 20f, value.maxLength());
            field.setValue(value.get());
            field.onChange(value::set);
            return field;
        }
        if (setting instanceof MultiSelectSetting) {
            // Несколько чекбоксов в одной строке не помещаются — раскрывается отдельным списком
            return null;
        }
        return null;
    }

    private float rowHeight(Setting<?> setting) {
        Component control = controls.get(setting);
        if (control instanceof ColorPicker picker) {
            return picker.height() + 26f;
        }
        return ROW_HEIGHT;
    }

    private float contentHeight() {
        float total = PADDING * 2 + 26f;
        for (Setting<?> setting : visible()) {
            total += rowHeight(setting);
        }
        return total;
    }

    private List<Setting<?>> visible() {
        List<Setting<?>> result = new ArrayList<>();
        for (Setting<?> setting : order) {
            if (setting.visible()) {
                result.add(setting);
            }
        }
        return result;
    }

    private float height() {
        return Math.min(maxHeight, contentHeight());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float height = height();

        Render2D.filledBorder(context, x, y, WIDTH, height,
                12f, AspectColors.SURFACE_MAIN_GLASS, 0.5f, AspectColors.SURFACE_BORDER);

        AspectFont.SEMIBOLD.draw(context, module.name(), x + PADDING, y + PADDING, AspectColors.TEXT_PRIMARY);
        Render2D.texture(context, Icons.SETTINGS, x + WIDTH - PADDING - 14f, y + PADDING, 14f, AspectColors.TEXT_TERTIARY);

        Render2D.pushClip(context, x, y + PADDING + 22f, WIDTH, height - PADDING - 22f);

        float cursorY = y + PADDING + 26f - scroll;
        for (Setting<?> setting : visible()) {
            Component control = controls.get(setting);
            float rowHeight = rowHeight(setting);

            AspectFont.MEDIUM.draw(context, AspectFont.MEDIUM.clip(setting.name(), (int) (WIDTH - CONTROL_WIDTH - PADDING * 2 - 8)),
                    x + PADDING, cursorY + 4f, AspectColors.TEXT_SECONDARY);

            if (control instanceof ColorPicker) {
                control.bounds(x + PADDING, cursorY + 20f);
            } else {
                control.bounds(x + WIDTH - PADDING - control.width(), cursorY + (ROW_HEIGHT - control.height()) / 2f - 6f);
            }
            control.draw(context, mouseX, mouseY, delta);

            if (setting instanceof NumberSetting number) {
                AspectFont.MEDIUM.drawRight(context, format(number.get()), x + WIDTH - PADDING, cursorY + 16f,
                        AspectColors.TEXT_TERTIARY);
            }
            cursorY += rowHeight;
        }
        Render2D.popClip(context);
    }

    private static String format(double value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    public void renderOverlay(DrawContext context, int mouseX, int mouseY) {
        for (Component control : controls.values()) {
            if (control instanceof Dropdown dropdown) {
                dropdown.renderOverlay(context, mouseX, mouseY);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Component control : controls.values()) {
            if (control instanceof Dropdown dropdown && dropdown.expanded()
                    && dropdown.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        if (!Render2D.hovered(mouseX, mouseY, x, y, WIDTH, height())) {
            return false;
        }
        for (Component control : controls.values()) {
            if (control.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = false;
        for (Component control : controls.values()) {
            handled |= control.mouseReleased(mouseX, mouseY, button);
        }
        return handled;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Component control : controls.values()) {
            if (control.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!Render2D.hovered(mouseX, mouseY, x, y, WIDTH, height())) {
            return false;
        }
        float maxScroll = Math.max(0f, contentHeight() - height());
        scroll = Math.max(0f, Math.min(maxScroll, scroll - (float) amount * 20f));
        return true;
    }

    public boolean keyPressed(int key, int scancode, int modifiers) {
        for (Component control : controls.values()) {
            if (control.keyPressed(key, scancode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public boolean charTyped(char symbol, int modifiers) {
        for (Component control : controls.values()) {
            if (control.charTyped(symbol, modifiers)) {
                return true;
            }
        }
        return false;
    }
}
