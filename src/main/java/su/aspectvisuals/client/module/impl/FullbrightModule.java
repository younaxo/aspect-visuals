package su.aspectvisuals.client.module.impl;

import net.minecraft.client.option.SimpleOption;
import su.aspectvisuals.client.mixin.SimpleOptionAccessor;
import su.aspectvisuals.client.module.Category;
import su.aspectvisuals.client.module.Module;
import su.aspectvisuals.client.setting.NumberSetting;

public class FullbrightModule extends Module {
    private final NumberSetting brightness = register(new NumberSetting("Яркость", "Значение гаммы", 10.0, 1.0, 20.0, 0.5));

    private Double previousGamma;

    public FullbrightModule() {
        super("Fullbright", "Полная яркость мира", Category.VISUAL);
    }

    @Override
    protected void onEnable() {
        SimpleOption<Double> gamma = mc.options.getGamma();
        if (previousGamma == null) {
            previousGamma = gamma.getValue();
        }
        write(gamma, brightness.get());
    }

    @Override
    protected void onDisable() {
        if (previousGamma != null) {
            write(mc.options.getGamma(), previousGamma);
            previousGamma = null;
        }
    }

    @Override
    public void onTick() {
        write(mc.options.getGamma(), brightness.get());
    }

    @SuppressWarnings("unchecked")
    private void write(SimpleOption<Double> option, double value) {
        ((SimpleOptionAccessor<Double>) (Object) option).aspect$setValue(value);
    }
}
