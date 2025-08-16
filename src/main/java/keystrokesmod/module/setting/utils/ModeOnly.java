package keystrokesmod.module.setting.utils;

import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.module.setting.interfaces.InputSetting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModeOnly implements Supplier<Boolean> {
    private final InputSetting mode;
    private final DoubleSet activeMode;

    public ModeOnly(@NotNull InputSetting mode, double @NotNull ... activeMode) {
        this.mode = mode;
        this.activeMode = DoubleOpenHashSet.toSet(Arrays.stream(activeMode));
    }

    public ModeOnly(@NotNull InputSetting mode, @NotNull Collection<Double> activeMode) {
        this.mode = mode;
        this.activeMode = new DoubleOpenHashSet(activeMode);
    }

    public ModeOnly(@NotNull ModeValue mode, String @NotNull ... activeMode) {
        this.mode = mode;
        this.activeMode = new DoubleOpenHashSet(activeMode.length);

        Set<String> modeNames = new ObjectOpenHashSet<>(activeMode);
        List<SubMode<?>> modes = mode.getSubModeValues();
        for (int i = 0; i < modes.size(); i++) {
            if (modeNames.contains(modes.get(i).getName())) {
                this.activeMode.add(i);
            }
        }
    }

    @Override
    public Boolean get() {
        return activeMode.contains(mode.getInput());
    }

    public ModeOnly reserve() {
        int max = (int) mode.getMax();
        List<Double> options = IntStream.rangeClosed(0, max)
                .filter(i -> !activeMode.contains(i))
                .mapToObj(i -> (double) i)
                .collect(Collectors.toList());
        return new ModeOnly(mode, options);
    }

    @Contract(pure = true)
    @SafeVarargs
    public final @NotNull Supplier<Boolean> extend(Supplier<Boolean>... suppliers) {
        if (suppliers == null || suppliers.length == 0) {
            return this;
        }
        return () -> this.get() && Arrays.stream(suppliers).allMatch(Supplier::get);
    }

    @Contract(pure = true)
    public final @NotNull Supplier<Boolean> extend(ButtonSetting... settings) {
        if (settings == null || settings.length == 0) {
            return this;
        }
        return () -> this.get() && Arrays.stream(settings).allMatch(ButtonSetting::isToggled);
    }

    @SuppressWarnings("unused")
    @Contract("_ -> new")
    public final @NotNull ModeOnly extend(double @NotNull ... activeMode) {
        Set<Double> modes = Arrays.stream(activeMode).boxed().collect(Collectors.toSet());
        modes.addAll(this.activeMode);
        return new ModeOnly(mode, modes);
    }
}
