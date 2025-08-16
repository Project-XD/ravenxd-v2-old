package keystrokesmod.module.impl.player.selfdamage;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

public class FakeSelfDamage extends SubMode<Module> implements ISelfDamage {
    public FakeSelfDamage(String name, @NotNull Module parent) {
        super(name, parent);
    }

    @Override
    public void damage() {
        mc.thePlayer.performHurtAnimation();
    }
}
