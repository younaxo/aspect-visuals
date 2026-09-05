package su.aspectvisuals.client.ui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import su.aspectvisuals.client.AspectVisuals;
import su.aspectvisuals.client.ui.component.IconButton;
import su.aspectvisuals.client.ui.component.TextField;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Icons;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;
import su.aspectvisuals.client.waypoint.Waypoint;

import java.util.List;

/** Список точек с координатами, цветом и переключателем видимости. */
public class WaypointsView implements ScreenView {
    private static final float WIDTH = 420f;
    private static final float ROW_HEIGHT = 44f;
    private static final float PADDING = 16f;

    private final TextField name = new TextField("Название точки", Icons.WAYPOINTS, 260f, 32f, 32);
    private IconButton add;

    private float x;
    private float y;
    private float height;
    private float scroll;

    @Override
    public void layout(int screenWidth, int screenHeight, float top, float bottom) {
        x = (screenWidth - WIDTH) / 2f;
        y = top + 24f;
        height = Math.max(ROW_HEIGHT * 2, bottom - y - 64f);

        name.bounds(x + PADDING, y + height + 12f);
        add = (IconButton) new IconButton(Icons.PLUS, 32f, true, this::create)
                .bounds(x + PADDING + name.width() + 8f, y + height + 12f);
    }

    private void create() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || name.value().isBlank()) {
            return;
        }

        BlockPos pos = client.player.getBlockPos();
        String dimension = client.world == null ? "" : client.world.getRegistryKey().getValue().toString();
        AspectVisuals.waypoints().add(new Waypoint(name.value().trim(), pos, dimension, "waypoints",
                AspectColors.ACCENT_PRIMARY, true));
        name.setValue("");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Render2D.roundedRect(context, x, y, WIDTH, height, 12f, AspectColors.SURFACE_MAIN_GLASS);
        Render2D.border(context, x, y, WIDTH, height, 0.5f, AspectColors.SURFACE_BORDER);

        List<Waypoint> waypoints = AspectVisuals.waypoints().all();
        if (waypoints.isEmpty()) {
            AspectFont.MEDIUM.drawCentered(context, "Точек пока нет", x + WIDTH / 2f, y + height / 2f,
                    AspectColors.TEXT_TERTIARY);
        }

        context.enableScissor(Math.round(x), Math.round(y), Math.round(x + WIDTH), Math.round(y + height));
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint waypoint = waypoints.get(i);
            float rowY = y + PADDING + i * ROW_HEIGHT - scroll;
            if (rowY + ROW_HEIGHT < y || rowY > y + height) {
                continue;
            }

            boolean hover = Render2D.hovered(mouseX, mouseY, x + PADDING, rowY, WIDTH - PADDING * 2, ROW_HEIGHT - 6f);
            if (hover) {
                Render2D.roundedRect(context, x + PADDING, rowY, WIDTH - PADDING * 2, ROW_HEIGHT - 6f, 8f,
                        AspectColors.SURFACE_CARD_HOVER);
            }

            int tint = waypoint.visible() ? waypoint.color() : AspectColors.TEXT_DISABLED;
            Render2D.texture(context, Icons.WAYPOINTS, x + PADDING + 8f, rowY + 9f, 16f, tint);

            AspectFont.SEMIBOLD.draw(context, waypoint.name(), x + PADDING + 32f, rowY + 6f,
                    waypoint.visible() ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_DISABLED);

            BlockPos pos = waypoint.position();
            AspectFont.MEDIUM.draw(context, pos.getX() + ", " + pos.getY() + ", " + pos.getZ(),
                    x + PADDING + 32f, rowY + 21f, AspectColors.TEXT_TERTIARY);

            Render2D.texture(context, Icons.TRASH, x + WIDTH - PADDING - 24f, rowY + 9f, 16f,
                    Render2D.hovered(mouseX, mouseY, x + WIDTH - PADDING - 24f, rowY + 9f, 16f, 16f)
                            ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_TERTIARY);
        }
        context.disableScissor();

        name.draw(context, mouseX, mouseY, delta);
        add.draw(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (name.mouseClicked(mouseX, mouseY, button) || add.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        List<Waypoint> waypoints = AspectVisuals.waypoints().all();
        for (int i = 0; i < waypoints.size(); i++) {
            float rowY = y + PADDING + i * ROW_HEIGHT - scroll;

            if (Render2D.hovered(mouseX, mouseY, x + WIDTH - PADDING - 24f, rowY + 9f, 16f, 16f)) {
                AspectVisuals.waypoints().remove(i);
                return true;
            }
            if (Render2D.hovered(mouseX, mouseY, x + PADDING, rowY, WIDTH - PADDING * 2, ROW_HEIGHT - 6f)) {
                Waypoint waypoint = waypoints.get(i);
                AspectVisuals.waypoints().replace(i, waypoint.withVisible(!waypoint.visible()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return add.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!Render2D.hovered(mouseX, mouseY, x, y, WIDTH, height)) {
            return false;
        }
        float content = AspectVisuals.waypoints().all().size() * ROW_HEIGHT + PADDING * 2;
        scroll = Math.max(0f, Math.min(Math.max(0f, content - height), scroll - (float) amount * 20f));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        return name.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(char symbol, int modifiers) {
        return name.charTyped(symbol, modifiers);
    }
}
