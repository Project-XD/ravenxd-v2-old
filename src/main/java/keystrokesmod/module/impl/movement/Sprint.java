package keystrokesmod.module.impl.movement;

import keystrokesmod.event.client.PreTickEvent;
import keystrokesmod.event.player.*;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.setting.impl.ModeSetting;

public class Sprint extends Module {
    private final ModeSetting mode;

    public Sprint() {
        super("Sprint", category.movement);
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Legit", "LegitMotion"}, 0));
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (mode.getInput() == 1) {
            event.setSprinting(false);
        }
    }

    @EventListener
    public void onPreTick(PreTickEvent e) {
        ((KeyBindingAccessor) mc.gameSettings.keyBindSprint).setPressed(true);
    }
}
