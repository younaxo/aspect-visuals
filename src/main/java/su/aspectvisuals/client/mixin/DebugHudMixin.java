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
 * Версия игры остаётся — она нужна для отладки. Меняется только источник
 * сборки: вместо названия загрузчика подставляется название клиента и его
 * версия. Прочие строки с упоминанием загрузчика отбрасываются.
 */
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    private static final String LOADER = "fabric";

    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void aspect$rebrand(CallbackInfoReturnable<List<String>> info) {
        List<String> lines = info.getReturnValue();
        if (lines == null || lines.isEmpty()) {
            return;
        }

        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            if (line == null || !line.toLowerCase(Locale.ROOT).contains(LOADER)) {
                result.add(line);
                continue;
            }

            String replaced = replaceLoader(line);
            // Если от строки осталось только упоминание загрузчика, она не нужна
            if (!replaced.isBlank()) {
                result.add(replaced);
            }
        }

        info.setReturnValue(result);
    }

    /** Подменяет название загрузчика, сохраняя остальную часть строки. */
    private static String replaceLoader(String line) {
        String badge = AspectVisuals.NAME + " " + AspectVisuals.version();

        StringBuilder builder = new StringBuilder(line.length());
        String lower = line.toLowerCase(Locale.ROOT);
        int from = 0;
        int at;
        while ((at = lower.indexOf(LOADER, from)) >= 0) {
            builder.append(line, from, at).append(badge);
            from = at + LOADER.length();
        }
        builder.append(line, from, line.length());
        return builder.toString();
    }
}
