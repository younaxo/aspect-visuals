package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.render.Icons;

import java.util.ArrayList;
import java.util.List;

/** Таймеры перезарядки предметов: жемчуг эндера, щит, чоруса и прочее. */
public class CooldownsModule extends HudModule {
    private static final List<net.minecraft.item.Item> TRACKED = List.of(
            Items.ENDER_PEARL, Items.CHORUS_FRUIT, Items.SHIELD, Items.GOAT_HORN);

    private static final List<HudCard.Row> SAMPLE = List.of(
            new HudCard.Row(Icons.TIMER, "Pearl", "2s"),
            new HudCard.Row(Icons.TIMER, "Combat", "1m55s"));

    public CooldownsModule() {
        super("Cooldowns", "Таймеры перезарядки", Category.HUD, 0.86f, 0.6f, HudAnchor.TOP_RIGHT);
    }

    private List<HudCard.Row> rows() {
        if (mc.player == null) {
            return SAMPLE;
        }

        List<HudCard.Row> rows = new ArrayList<>();
        for (net.minecraft.item.Item item : TRACKED) {
            float progress = mc.player.getItemCooldownManager().getCooldownProgress(new ItemStack(item), 0f);
            if (progress > 0f) {
                String name = new ItemStack(item).getName().getString();
                rows.add(new HudCard.Row(Icons.TIMER, name, Math.round(progress * 100f) + "%"));
            }
        }
        return rows.isEmpty() ? SAMPLE : rows;
    }

    @Override
    public boolean drawInEditor() {
        return true;
    }

    @Override
    public float widgetWidth() {
        return HudCard.width("Cooldowns", rows());
    }

    @Override
    public float widgetHeight() {
        return HudCard.height("Cooldowns", rows());
    }

    @Override
    public void renderWidget(DrawContext context) {
        HudCard.draw(context, 0f, 0f, Icons.TIMER, "Cooldowns", rows());
    }
}
