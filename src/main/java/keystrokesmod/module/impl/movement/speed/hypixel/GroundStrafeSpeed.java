package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.event.player.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class GroundStrafeSpeed extends SubMode<HypixelSpeed> {
    public GroundStrafeSpeed(String name, @NotNull HypixelSpeed parent) {
        super(name, parent);
    }

    @EventListener
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (parent.parent.noAction()) return;

        if (Utils.isMoving() && mc.currentScreen == null && mc.thePlayer.onGround) {
            MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
            MoveUtil.jump();
        }
    }
}
