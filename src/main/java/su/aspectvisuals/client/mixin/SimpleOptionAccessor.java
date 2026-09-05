package su.aspectvisuals.client.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Ванильная гамма ограничена диапазоном 0..1 колбэками SimpleOption,
 * поэтому FullBright пишет значение напрямую в поле.
 */
@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor<T> {
    @Accessor("value")
    void aspect$setValue(T value);
}
