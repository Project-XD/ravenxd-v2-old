package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.player.JumpEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class OldIntaveSprint extends IScaffoldSprint {
    public OldIntaveSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @EventListener
    public void onJump(@NotNull JumpEvent event) {
        event.cancel();
        mc.thePlayer.motionY = MoveUtil.jumpMotion();
    }

    @Override
    public boolean isSprint() {
        return !mc.thePlayer.onGround;
    }
}
