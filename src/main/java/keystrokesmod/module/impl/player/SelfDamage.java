package keystrokesmod.module.impl.player;

import keystrokesmod.event.player.DamageEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.player.selfdamage.FakeSelfDamage;
import keystrokesmod.module.impl.player.selfdamage.GrimACSelfDamage;
import keystrokesmod.module.impl.player.selfdamage.ISelfDamage;
import keystrokesmod.module.impl.player.selfdamage.VanillaSelfDamage;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SliderSetting;

public class SelfDamage extends Module {
    private final ModeValue mode;
    private final ButtonSetting autoDisable;
    private final ButtonSetting repeat;
    private final SliderSetting repeatDelay;

    private int repeatDelayTicks = -1;

    public SelfDamage() {
        super("SelfDamage", category.player);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new FakeSelfDamage("Fake", this))
                .add(new VanillaSelfDamage("Vanilla", this))
                .add(new GrimACSelfDamage("GrimAC", this))
        );
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", false));
        this.registerSetting(repeat = new ButtonSetting("Repeat", false, () -> !autoDisable.isToggled()));
        this.registerSetting(repeatDelay = new SliderSetting("Repeat delay", 20, 0, 40, 1, "tick",
                () -> !autoDisable.isToggled() && repeat.isToggled()));
    }

    @Override
    public void onEnable() throws Throwable {
        mode.enable();
        ((ISelfDamage) mode.getSelected()).damage();
        if (repeat.isToggled()) {
            repeatDelayTicks = (int) repeatDelay.getInput();
        }
    }

    @Override
    public void onDisable() throws Throwable {
        mode.disable();
        repeatDelayTicks = -1;
    }

    @Override
    public void onUpdate() throws Throwable {
        if (repeatDelayTicks == 0) {
            ((ISelfDamage) mode.getSelected()).damage();
            if (repeat.isToggled()) {
                repeatDelayTicks = (int) repeatDelay.getInput();
            }
        } else if (repeatDelayTicks != -1) {
            repeatDelayTicks--;
        }
    }

    @EventListener
    public void onDamage(DamageEvent event) {
        if (autoDisable.isToggled()) {
            disable();
        }
    }
}
