package su.aspectvisuals.client.ui.component;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import su.aspectvisuals.client.setting.KeybindSetting;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

/** Bind из макета: нажатие переводит поле в режим ожидания клавиши. */
public class KeybindPicker extends Component {
    private final KeybindSetting setting;
    private boolean listening;

    public KeybindPicker(KeybindSetting setting, float width) {
        super(width, 20f);
        this.setting = setting;
    }

    public boolean listening() {
        return listening;
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hover = hovered(mouseX, mouseY);
        Render2D.filledBorder(context, x, y, width, height,
                8f, hover || listening ? AspectColors.SURFACE_CARD_HOVER : AspectColors.SURFACE_INPUT, 0.5f, listening ? AspectColors.SURFACE_BORDER_TYPE : AspectColors.SURFACE_BORDER);

        String label = listening ? "…" : setting.label();
        AspectFont.MEDIUM.drawCentered(context, label, x + width / 2f,
                y + (height - AspectFont.MEDIUM.lineHeight()) / 2f,
                listening ? AspectColors.TEXT_PRIMARY : AspectColors.TEXT_SECONDARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!hovered(mouseX, mouseY)) {
            listening = false;
            return false;
        }
        if (button == 1) {
            setting.set(KeybindSetting.NONE);
            listening = false;
            return true;
        }
        listening = !listening;
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (!listening) {
            return false;
        }
        setting.set(key == GLFW.GLFW_KEY_ESCAPE ? KeybindSetting.NONE : key);
        listening = false;
        return true;
    }
}
