package su.aspectvisuals.client.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Direction;
import su.aspectvisuals.client.hud.HudAnchor;
import su.aspectvisuals.client.hud.HudCard;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.HudModule;
import su.aspectvisuals.client.ui.render.Icons;

public class DirectionModule extends HudModule {
    public DirectionModule() {
        super("Direction", "Направление взгляда", Category.INFO, 0.02f, 0.22f, HudAnchor.TOP_LEFT);
    }

    private String value() {
        if (mc.player == null) {
            return "—";
        }
        Direction facing = mc.player.getHorizontalFacing();
        String name = switch (facing) {
            case NORTH -> "Север";
            case SOUTH -> "Юг";
            case WEST -> "Запад";
            case EAST -> "Восток";
            default -> facing.asString();
        };
        return name + " (" + facing.asString().toUpperCase(java.util.Locale.ROOT) + ")";
    }

    @Override
    public float widgetWidth() {
        return HudCard.pillWidth(Icons.TARGET, value());
    }

    @Override
    public float widgetHeight() {
        return HudCard.pillHeight();
    }

    @Override
    public void renderWidget(DrawContext context) {
        HudCard.pill(context, 0f, 0f, Icons.TARGET, value());
    }
}
