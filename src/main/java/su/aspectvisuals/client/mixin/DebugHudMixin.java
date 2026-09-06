package su.aspectvisuals.client.mixin;

import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.aspectvisuals.client.AspectVisuals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Отладочный экран говорит о клиенте, а не о загрузчике.
 *
 * Строка о версии собирается заново, а не правится по вхождениям: в исходной
 * строке название загрузчика встречается несколько раз, и подмена каждого
 * вхождения давала нечитаемое повторение.
 *
 * Версия игры сохраняется — она нужна для отладки. Пометка о модификации
 * ставится, только если рядом загружены посторонние моды.
 */
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    private static final String PREFIX = "Minecraft ";
    private static final String LOADER = "fabric";

    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void aspect$rebrand(CallbackInfoReturnable<List<String>> info) {
        List<String> lines = info.getReturnValue();
        if (lines == null || lines.isEmpty()) {
            return;
        }

        List<String> result = new ArrayList<>(lines.size());
        boolean stamped = false;

        for (String line : lines) {
            if (line == null) {
                continue;
            }
            if (!stamped && line.startsWith(PREFIX)) {
                result.add(versionLine(line));
                stamped = true;
                continue;
            }
            // Прочие упоминания загрузчика на отладочном экране не нужны
            if (!line.toLowerCase(Locale.ROOT).contains(LOADER)) {
                result.add(line);
            }
        }

        info.setReturnValue(result);
    }

    /** Собирает строку версии из версии игры и версии клиента. */
    private static String versionLine(String original) {
        // Версия игры — первое слово после названия, до пробела или скобки
        String tail = original.substring(PREFIX.length());
        int end = tail.length();
        for (int i = 0; i < tail.length(); i++) {
            char symbol = tail.charAt(i);
            if (symbol == ' ' || symbol == '(') {
                end = i;
                break;
            }
        }
        String game = tail.substring(0, end);

        String version = AspectVisuals.version();
        String base = version;
        String build = "";
        int dash = version.indexOf('-');
        if (dash > 0) {
            base = version.substring(0, dash);
            build = version.substring(dash + 1);
        }

        StringBuilder line = new StringBuilder();
        line.append(PREFIX).append(game)
                .append(" | ").append(AspectVisuals.NAME)
                .append(" (").append(AspectVisuals.MOD_NAME).append(' ').append(base);
        if (!build.isEmpty()) {
            line.append(" (").append(build).append(')');
        }
        if (AspectVisuals.hasForeignMods()) {
            line.append(" Modified");
        }
        return line.append(')').toString();
    }
}
