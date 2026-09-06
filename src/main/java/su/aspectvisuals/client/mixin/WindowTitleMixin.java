package su.aspectvisuals.client.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.aspectvisuals.client.ui.WindowBranding;

/**
 * Заголовок окна называет клиент.
 *
 * Игра пересобирает заголовок при смене мира и сервера, поэтому одной
 * установки при запуске не хватило бы: следующее обновление вернуло бы
 * стандартный текст.
 */
@Mixin(MinecraftClient.class)
public abstract class WindowTitleMixin {

    @Inject(method = "getWindowTitle", at = @At("RETURN"), cancellable = true)
    private void aspect$title(CallbackInfoReturnable<String> info) {
        info.setReturnValue(WindowBranding.title());
    }
}
