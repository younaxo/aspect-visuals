package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/**
 * Счётчик тотемов: капсула с изображением предмета и количеством.
 *
 * Размеры из макета: капсула 54x29, содержимое 37x20 с отступом 8.5,
 * значок 20x20, число со сдвигом 24 и кеглем 12.
 */
public class TotemCounterModule extends HudModule {
    private static final float WIDTH = 54f;
    private static final float HEIGHT = 29f;
    private static final float PADDING = 8.5f;
    private static final float TEXT_LEFT = 24f;
    private static final float TEXT_TOP = 2.5f;
    private static final float TEXT_SIZE = 12f;

    public TotemCounterModule() {
        super("Totem Counter", "Количество тотемов", Category.HUD, 0.5f, 0.86f, HudAnchor.TOP_LEFT);
    }

    private int count() {
        if (mc.player == null) {
            return 5;
        }
        int total = 0;
        for (int slot = 0; slot < mc.player.getInventory().size(); slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                total += stack.getCount();
            }
        }
        return total;
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
    public void renderWidget(DrawContext context) {
        // Капсула: радиус равен половине высоты, как задано скруглением макета
        Render2D.filledBorder(context, 0f, 0f, WIDTH, HEIGHT, HEIGHT / 2f,
                AspectColors.SURFACE_CARD, 1f, AspectColors.SURFACE_BORDER);

        // Предмет рисуется штатным путём игры: он идёт со своей моделью и
        // освещением, повторять их своим рендером незачем
        float contentTop = PADDING - 4f;
        context.drawItem(new ItemStack(Items.TOTEM_OF_UNDYING), Math.round(PADDING), Math.round(contentTop));

        AspectFont.SEMIBOLD.draw(context, "x" + count(),
                PADDING + TEXT_LEFT, contentTop + TEXT_TOP, TEXT_SIZE, AspectColors.TEXT_PRIMARY);
    }
}
