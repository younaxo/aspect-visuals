package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.util.TimeFormat;

import java.util.ArrayList;
import java.util.List;

public class PotionEffectsModule extends HudModule {
    private static final List<HudCard.Row> SAMPLE = List.of(
            new HudCard.Row(Icons.ASTERISK, "Fire Res II", "1m10s"),
            new HudCard.Row(Icons.ASTERISK, "Hunger", "1m44s"),
            new HudCard.Row(Icons.ASTERISK, "Speed II", "3s"));

    public PotionEffectsModule() {
        super("Potion Effects", "Активные эффекты", Category.HUD, 0.02f, 0.34f, HudAnchor.TOP_LEFT);
    }

    private List<HudCard.Row> rows() {
        if (mc.player == null || mc.player.getStatusEffects().isEmpty()) {
            return SAMPLE;
        }

        List<HudCard.Row> rows = new ArrayList<>();
        for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
            String name = Text.translatable(effect.getEffectType().value().getTranslationKey()).getString();
            if (effect.getAmplifier() > 0) {
                name += " " + roman(effect.getAmplifier() + 1);
            }
            rows.add(new HudCard.Row(Icons.ASTERISK, name, TimeFormat.ticks(effect.getDuration())));
        }
        return rows;
    }

    private static String roman(int level) {
        return switch (level) {
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }

    @Override
    public float widgetWidth() {
        return HudCard.width("Potions", rows());
    }

    @Override
    public float widgetHeight() {
        return HudCard.height("Potions", rows());
    }

    @Override
    public void renderWidget(DrawContext context) {
        HudCard.draw(context, 0f, 0f, Icons.ASTERISK, "Potions", rows());
    }
}
