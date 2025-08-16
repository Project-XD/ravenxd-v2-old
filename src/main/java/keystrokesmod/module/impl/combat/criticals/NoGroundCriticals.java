package keystrokesmod.module.impl.combat.criticals;

import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.module.impl.combat.Criticals;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class NoGroundCriticals extends SubMode<Criticals> {
    public NoGroundCriticals(String name, @NotNull Criticals parent) {
        super(name, parent);
    }

    @EventListener(priority = 1)
    public void onPreMotion(PreMotionEvent event) {
        if (Utils.isTargetNearby()) {
            event.setOnGround(false);
        }
    }
}
