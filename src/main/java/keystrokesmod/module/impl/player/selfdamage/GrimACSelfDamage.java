package keystrokesmod.module.impl.player.selfdamage;

import keystrokesmod.event.player.MoveInputEvent;
import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import org.jetbrains.annotations.NotNull;

public class GrimACSelfDamage extends SubMode<Module> implements ISelfDamage {
    private int jumps = 0;
    private boolean toDamage = false;
    private boolean lastJump = false;

    public GrimACSelfDamage(String name, @NotNull Module parent) {
        super(name, parent);
    }

    @EventListener
    public void onPreMotion(@NotNull PreMotionEvent event) {
        if (toDamage) {
            if (jumps > 0)
                event.setOnGround(false);
            if (mc.thePlayer.onGround) {
                MoveUtil.jump();
                lastJump = true;
            } else if (lastJump) {
                lastJump = false;
                jumps++;
                if (jumps == 1) {
                    return;
                }
                if (jumps >= 4) {
                    toDamage = false;
                }
            }
        }
    }

    @EventListener
    public void onMoveInput(@NotNull MoveInputEvent event) {
        if (toDamage)
            event.setJump(true);
    }

    @Override
    public void damage() {
        jumps = 0;
        toDamage = true;
    }

    @Override
    public void onDisable() throws Throwable {
        jumps = 0;
        toDamage = false;
    }
}
