package su.aspectvisuals.client.ui.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import su.aspectvisuals.client.ui.font.AspectFont;
import su.aspectvisuals.client.ui.render.Render2D;
import su.aspectvisuals.client.ui.theme.AspectColors;

import java.util.function.Consumer;

/** Input из UI Kit: слева иконка, дальше текст или подсказка. */
public class TextField extends Component {
    private static final float PADDING = 12f;
    private static final float ICON = 16f;
    private static final float GAP = 8f;

    private final String placeholder;
    private final Identifier icon;
    private final int maxLength;
    private Consumer<String> onChange = value -> {
    };

    private String value = "";
    private long caretStart = System.currentTimeMillis();

    public TextField(String placeholder, Identifier icon, float width, float height, int maxLength) {
        super(width, height);
        this.placeholder = placeholder;
        this.icon = icon;
        this.maxLength = maxLength;
    }

    public TextField onChange(Consumer<String> listener) {
        this.onChange = listener;
        return this;
    }

    public String value() {
        return value;
    }

    public void setValue(String text) {
        value = text == null ? "" : text;
        onChange.accept(value);
    }

    @Override
    protected void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hover = hovered(mouseX, mouseY);
        int background = focused() || hover ? AspectColors.SURFACE_CARD_HOVER : AspectColors.SURFACE_INPUT;

        Render2D.filledBorder(context, x, y, width, height,
                12f, background, 0.5f, focused() ? AspectColors.SURFACE_BORDER_TYPE : AspectColors.SURFACE_BORDER);

        float textX = x + PADDING;
        if (icon != null) {
            int tint = value.isEmpty() ? AspectColors.TEXT_TERTIARY : AspectColors.TEXT_SECONDARY;
            Render2D.texture(context, icon, textX, y + (height - ICON) / 2f, ICON, tint);
            textX += ICON + GAP;
        }

        float textY = y + (height - AspectFont.MEDIUM.lineHeight()) / 2f;
        int available = (int) (width - (textX - x) - PADDING);

        if (value.isEmpty() && !focused()) {
            AspectFont.MEDIUM.draw(context, AspectFont.MEDIUM.clip(placeholder, available), textX, textY, AspectColors.TEXT_TERTIARY);
            return;
        }

        String shown = AspectFont.MEDIUM.clip(value, available);
        AspectFont.MEDIUM.draw(context, shown, textX, textY, AspectColors.TEXT_PRIMARY);

        boolean caretVisible = (System.currentTimeMillis() - caretStart) % 1000 < 500;
        if (focused() && caretVisible) {
            float caretX = textX + AspectFont.MEDIUM.width(shown) + 1f;
            Render2D.rect(context, caretX, textY, 1f, AspectFont.MEDIUM.lineHeight(), AspectColors.TEXT_PRIMARY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inside = hovered(mouseX, mouseY);
        setFocused(inside);
        if (inside) {
            caretStart = System.currentTimeMillis();
        }
        return inside;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (!focused()) {
            return false;
        }

        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!value.isEmpty()) {
                setValue(value.substring(0, value.length() - 1));
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
            setFocused(false);
            return true;
        }
        if (key == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
            setValue(trim(value + clipboard));
            return true;
        }
        // Пробел в поиске не должен уходить в управление игроком
        return key == GLFW.GLFW_KEY_SPACE;
    }

    @Override
    public boolean charTyped(char symbol, int modifiers) {
        if (!focused() || symbol < ' ') {
            return false;
        }
        setValue(trim(value + symbol));
        caretStart = System.currentTimeMillis();
        return true;
    }

    private String trim(String text) {
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
