package su.aspectvisuals.client.module.impl;

import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.Module;
import su.aspectvisuals.client.setting.GameKeybindSetting;
import su.aspectvisuals.client.ui.Keybinds;

/**
 * Настройки самого клиента.
 *
 * Клавиши открытия интерфейса и редактора HUD заведены как привязки игры,
 * поэтому меняются и здесь, и в настройках управления Minecraft — значение
 * у них общее.
 */
public class ClientModule extends Module {

    public ClientModule() {
        super("Client", "Настройки клиента", Category.VISUAL);
        register(new GameKeybindSetting("Открыть интерфейс",
                "Клавиша, открывающая интерфейс клиента", Keybinds.openGui()));
        register(new GameKeybindSetting("Редактор HUD",
                "Клавиша, открывающая редактор раскладки", Keybinds.hudEditor()));
        setEnabled(true);
    }
}
