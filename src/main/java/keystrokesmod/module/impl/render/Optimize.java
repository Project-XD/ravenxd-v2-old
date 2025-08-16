package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;

public class Optimize extends Module {
    private final ButtonSetting fastLight;
    private final ButtonSetting deSync;
    private final ButtonSetting multiThread;

    public Optimize() {
        super("Optimize", category.client);
        this.registerSetting(new DescriptionSetting("Light update"));
        this.registerSetting(fastLight = new ButtonSetting("Fast light", true));
        this.registerSetting(deSync = new ButtonSetting("DeSync", true, fastLight::isToggled));
        this.registerSetting(multiThread = new ButtonSetting("Multi-threading", false,
                () -> fastLight.isToggled() && deSync.isToggled()));
    }

    public static boolean isFastLight() {
        return canOptimize() && ModuleManager.optimize.fastLight.isToggled();
    }

    public static boolean isFastLightDeSync() {
        return isFastLight() && ModuleManager.optimize.deSync.isToggled();
    }

    public static boolean isFastLightMultiThread() {
        return isFastLightDeSync() && ModuleManager.optimize.multiThread.isToggled();
    }

    private static boolean canOptimize() {
        if (ModuleManager.optimize == null) return false;
        if (!ModuleManager.optimize.isEnabled()) return false;
        return true;
    }
}
