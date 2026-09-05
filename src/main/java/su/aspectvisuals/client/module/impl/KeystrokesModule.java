package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.render.Icons;

import java.util.ArrayList;
import java.util.List;

/** Нажатия клавиш: список привязок игры и их текущее состояние. */
public class KeystrokesModule extends HudModule {
    public KeystrokesModule() {
        super("Keystrokes", "Нажатия клавиш", Category.HUD, 0.86f, 0.22f, HudAnchor.TOP_RIGHT);
    }

    private List<HudCard.Row> rows() {
        List<HudCard.Row> rows = new ArrayList<>();
        addRow(rows, "Вперёд", mc.options.forwardKey);
        addRow(rows, "Назад", mc.options.backKey);
        addRow(rows, "Влево", mc.options.leftKey);
        addRow(rows, "Вправо", mc.options.rightKey);
        addRow(rows, "Прыжок", mc.options.jumpKey);
        addRow(rows, "Присесть", mc.options.sneakKey);
        return rows;
    }

    private void addRow(List<HudCard.Row> rows, String label, KeyBinding binding) {
        rows.add(new HudCard.Row(null, label, binding.getBoundKeyLocalizedText().getString().toUpperCase(java.util.Locale.ROOT)));
    }

    @Override
    public float widgetWidth() {
        return HudCard.width("Keybinds", rows());
    }

    @Override
    public float widgetHeight() {
        return HudCard.height("Keybinds", rows());
    }

    @Override
    public void renderWidget(DrawContext context) {
        HudCard.draw(context, 0f, 0f, Icons.COMMAND, "Keybinds", rows());
    }
}
