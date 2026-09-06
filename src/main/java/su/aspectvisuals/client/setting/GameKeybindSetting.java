package su.aspectvisuals.client.setting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * Привязка клавиши, живущая в настройках игры.
 *
 * Значение не хранится здесь: источник один — привязка Minecraft. Иначе
 * клавиша, изменённая в настройках игры, разошлась бы с той, что показывает
 * клиент, и наоборот.
 *
 * В конфиг клиента такая настройка не пишется: её сохраняет сама игра.
 */
public class GameKeybindSetting extends KeybindSetting {
    private final KeyBinding binding;

    public GameKeybindSetting(String name, String description, KeyBinding binding) {
        super(name, description, NONE);
        this.binding = binding;
    }

    @Override
    public Integer get() {
        return InputUtil.fromTranslationKey(binding.getBoundKeyTranslationKey()).getCode();
    }

    @Override
    public void set(Integer value) {
        binding.setBoundKey(InputUtil.fromKeyCode(value, 0));
        KeyBinding.updateKeysByCode();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.options != null) {
            client.options.write();
        }
    }

    @Override
    public com.google.gson.JsonElement toJson() {
        // Значение принадлежит настройкам игры, дублировать его незачем
        return com.google.gson.JsonNull.INSTANCE;
    }

    @Override
    public void fromJson(com.google.gson.JsonElement json) {
    }
}
