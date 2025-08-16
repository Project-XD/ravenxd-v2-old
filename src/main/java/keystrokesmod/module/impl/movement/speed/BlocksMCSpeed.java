package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class BlocksMCSpeed extends SubMode<Speed> {
    public BlocksMCSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
    }

    @EventListener
    public void onPreUpdate(@NotNull PreUpdateEvent event) {
        if (parent.noAction() || !MoveUtil.isMoving()) return;

        if (mc.thePlayer.onGround) {
            MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance());
            MoveUtil.jump();
        } else {
            MoveUtil.strafe();
        }

        if (parent.offGroundTicks == 5) {
            mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
        }
    }
}
