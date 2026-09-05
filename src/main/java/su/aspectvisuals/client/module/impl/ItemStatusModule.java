package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.render.Icons;

import java.util.List;

public class ItemStatusModule extends HudModule {
    public ItemStatusModule() {
        super("Item Status", "Состояние предмета", Category.HUD, 0.58f, 0.86f, HudAnchor.TOP_LEFT);
    }

    private List<HudCard.Row> rows() {
        ItemStack stack = mc.player != null ? mc.player.getMainHandStack() : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            return List.of(HudCard.Row.of("Рука пуста", ""));
        }

        String name = stack.getName().getString();
        if (!stack.isDamageable()) {
            return List.of(HudCard.Row.of(name, "x" + stack.getCount()));
        }

        int left = stack.getMaxDamage() - stack.getDamage();
        return List.of(HudCard.Row.of(name, left + " / " + stack.getMaxDamage()));
    }

    @Override
    public float widgetWidth() {
        return HudCard.width(null, rows());
    }

    @Override
    public float widgetHeight() {
        return HudCard.height(null, rows());
    }

    @Override
    public void renderWidget(DrawContext context) {
        HudCard.draw(context, 0f, 0f, Icons.SHIELD, null, rows());
    }
}
