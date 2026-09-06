package su.aspectvisuals.client.ui.render;

import net.minecraft.util.Identifier;
import su.aspectvisuals.client.AspectVisuals;

/** Набор Aspect Icons из UI Kit. Имя файла совпадает с именем варианта в макете. */
public final class Icons {
    private Icons() {
    }

    private static Identifier icon(String name) {
        return AspectVisuals.id("textures/icon/" + name + ".png");
    }

    public static final Identifier LOGO = AspectVisuals.id("textures/logo.png");

    public static final Identifier LOGOTYPE = icon("logotype");
    public static final Identifier DESIGNER = icon("designer");
    public static final Identifier SEARCH = icon("search");
    public static final Identifier LAYOUT = icon("layout");
    public static final Identifier PAUSE = icon("pause");
    public static final Identifier TARGET = icon("target");
    public static final Identifier SETTINGS = icon("settings");
    public static final Identifier DAY_NIGHT = icon("day_night");
    public static final Identifier CHECK = icon("check");
    public static final Identifier DOWN = icon("down");
    public static final Identifier CLOSE = icon("close");
    public static final Identifier PLUS = icon("plus");
    public static final Identifier WAYPOINTS = icon("waypoints");
    public static final Identifier ASTERISK = icon("asterisk");
    public static final Identifier TIMER = icon("timer");
    public static final Identifier SHIELD = icon("shield");
    public static final Identifier COMMAND = icon("command");
    public static final Identifier HANDSHAKE = icon("handshake");
    public static final Identifier TRASH = icon("trash");
    public static final Identifier HOME = icon("home");
    public static final Identifier BONE = icon("bone");
    public static final Identifier RABBIT = icon("rabbit");
    public static final Identifier CONFIGS = icon("configs");
    public static final Identifier COPY = icon("copy");
    public static final Identifier SEND = icon("send");

    // Флаги языков лежат отдельным набором макета: они цветные, поэтому
    // рисуются без подкраски, в отличие от одноцветных иконок
    public static final Identifier FLAG_EN = icon("flag_lang_en");
    public static final Identifier FLAG_RU = icon("flag_lang_ru");
    public static final Identifier FLAG_UA = icon("flag_ua");
}
