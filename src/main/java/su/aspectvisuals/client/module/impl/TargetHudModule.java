package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

import java.util.ArrayList;
import java.util.List;

/**
 * Сведения о цели: имя, снаряжение и полоса здоровья.
 *
 * Размеры из макета: карточка 173x73, строки на 8.5, 32.5 и 52.5,
 * снаряжение шагом 20, полоса здоровья 133x6 со скруглением в капсулу.
 */
public class TargetHudModule extends HudModule {
    private static final float WIDTH = 173f;
    private static final float HEIGHT = 73f;
    private static final float PADDING = 8.5f;
    private static final float RADIUS = 10f;

    private static final float HEAD = 16f;
    private static final float NAME_LEFT = 20f;
    private static final float NAME_TOP = 0.5f;
    private static final float NAME_SIZE = 12f;

    private static final float ITEMS_TOP = 32.5f;
    private static final float ITEM = 12f;
    private static final float ITEM_STEP = 20f;

    private static final float BAR_TOP = 52.5f;
    private static final float BAR_WIDTH = 133f;
    private static final float BAR_HEIGHT = 6f;
    private static final float BAR_INSET = 3f;
    private static final float HEALTH_LEFT = 137f;
    private static final float HEALTH_SIZE = 10f;

    public TargetHudModule() {
        super("Target HUD", "Сведения о цели", Category.HUD, 0.37f, 0.28f, HudAnchor.TOP_LEFT);
    }

    private LivingEntity target() {
        Entity entity = mc.targetedEntity;
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private List<ItemStack> equipment(LivingEntity living) {
        List<ItemStack> stacks = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = living.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    @Override
    public boolean drawInEditor() {
        return true;
    }

    @Override
    public float widgetWidth() {
        return WIDTH;
    }

    @Override
    public float widgetHeight() {
        return HEIGHT;
    }

    @Override
    public boolean hasContent() {
        return target() != null;
    }

    @Override
    public void renderWidget(DrawContext context) {
        LivingEntity living = target();

        Render2D.filledBorder(context, 0f, 0f, WIDTH, HEIGHT, RADIUS,
                AspectColors.SURFACE_CARD, 1f, AspectColors.SURFACE_BORDER);

        String name = living != null ? living.getName().getString() : "—";
        float health = living != null ? living.getHealth() : 0f;
        float maxHealth = living != null ? Math.max(1f, living.getMaxHealth()) : 1f;

        Render2D.roundedRect(context, PADDING, PADDING, HEAD, HEAD, 2f, AspectColors.SURFACE_INPUT);
        AspectFont.SEMIBOLD.draw(context, AspectFont.SEMIBOLD.clip(name, WIDTH - PADDING - NAME_LEFT - PADDING),
                PADDING + NAME_LEFT, PADDING + NAME_TOP, NAME_SIZE, AspectColors.TEXT_PRIMARY);

        if (living != null) {
            float cursor = PADDING;
            for (ItemStack stack : equipment(living)) {
                context.drawItem(stack, Math.round(cursor), Math.round(ITEMS_TOP));
                cursor += ITEM_STEP;
                if (cursor + ITEM > WIDTH - PADDING) {
                    break;
                }
            }
        }

        float barY = BAR_TOP + BAR_INSET;
        Render2D.roundedRect(context, PADDING, barY, BAR_WIDTH, BAR_HEIGHT,
                BAR_HEIGHT / 2f, AspectColors.SURFACE_INPUT);
        float filled = BAR_WIDTH * Math.max(0f, Math.min(1f, health / maxHealth));
        if (filled > 0f) {
            Render2D.roundedRect(context, PADDING, barY, filled, BAR_HEIGHT,
                    BAR_HEIGHT / 2f, AspectColors.TEXT_PRIMARY);
        }

        AspectFont.MEDIUM.draw(context, Math.round(health) + "hp",
                PADDING + HEALTH_LEFT, BAR_TOP, HEALTH_SIZE, AspectColors.TEXT_SECONDARY);
    }
}
