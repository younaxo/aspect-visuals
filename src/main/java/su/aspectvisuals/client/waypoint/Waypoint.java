package su.aspectvisuals.client.waypoint;

import net.minecraft.util.math.BlockPos;

/** Точка на карте: координаты, подпись, иконка и цвет из палитры клиента. */
public record Waypoint(String name, BlockPos position, String dimension, String icon, int color, boolean visible) {

    public Waypoint withVisible(boolean value) {
        return new Waypoint(name, position, dimension, icon, color, value);
    }

    public Waypoint withColor(int value) {
        return new Waypoint(name, position, dimension, icon, value, visible);
    }
}
