package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

import java.util.ArrayList;
import java.util.List;

/** Броня и предмет в руке: иконки предметов с остатком прочности. */
public class ArmorStatusModule extends HudModule {
    private static final float SLOT = 18f;
    private static final float GAP = 4f;

    private static final List<EquipmentSlot> SLOTS = List.of(
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND);

    public ArmorStatusModule() {
        super("Armor Status", "Состояние брони", Category.HUD, 0.5f, 0.86f, HudAnchor.TOP_LEFT);
    }

    private List<ItemStack> stacks() {
        List<ItemStack> stacks = new ArrayList<>();
        if (mc.player == null) {
            return stacks;
        }

        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    @Override
    public float widgetWidth() {
        int count = Math.max(1, stacks().size());
        return count * SLOT + (count - 1) * GAP;
    }

    @Override
    public float widgetHeight() {
        return SLOT + AspectFont.MEDIUM.lineHeight() + 2f;
    }

    @Override
    public void renderWidget(DrawContext context) {
        List<ItemStack> stacks = stacks();
        float cursor = 0f;

        for (ItemStack stack : stacks) {
            context.drawItem(stack, Math.round(cursor + 1f), 1);

            if (stack.isDamageable()) {
                int left = stack.getMaxDamage() - stack.getDamage();
                int percent = Math.round(left * 100f / stack.getMaxDamage());
                AspectFont.MEDIUM.drawCentered(context, percent + "%", cursor + SLOT / 2f, SLOT + 2f,
                        percent > 25 ? AspectColors.TEXT_TERTIARY : AspectColors.SYSTEM_INFO);
            }
            cursor += SLOT + GAP;
        }

        if (stacks.isEmpty()) {
            Render2D.roundedRect(context, 0f, 0f, SLOT, SLOT, 6f, AspectColors.SURFACE_MAIN_GLASS);
        }
    }
}
