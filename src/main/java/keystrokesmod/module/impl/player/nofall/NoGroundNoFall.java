package keystrokesmod.module.impl.player.nofall;

import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.module.impl.player.NoFall;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class NoGroundNoFall extends SubMode<NoFall> {
    public NoGroundNoFall(String name, @NotNull NoFall parent) {
        super(name, parent);
    }

    @EventListener(priority = 1)
    public void onPreMotion(@NotNull PreMotionEvent event) {
        if (!parent.noAction())
            event.setOnGround(false);
    }
}
