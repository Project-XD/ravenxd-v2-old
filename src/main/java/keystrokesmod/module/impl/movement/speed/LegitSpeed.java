package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.player.MoveInputEvent;
import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class LegitSpeed extends SubMode<Speed> {
    private final ButtonSetting rotation;
    private final ButtonSetting cpuSpeedUpExploit;

    public LegitSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(rotation = new ButtonSetting("Rotation", false));
        this.registerSetting(cpuSpeedUpExploit = new ButtonSetting("CPU SpeedUp Exploit", false));
    }

    @EventListener
    public void onPreUpdate(@NotNull PreUpdateEvent event) {
        if (parent.noAction()) return;

        if (!mc.thePlayer.onGround && rotation.isToggled()) {
            RotationHandler.setRotationYaw(mc.thePlayer.moveStrafing > 0 ? mc.thePlayer.rotationYaw + 45 : mc.thePlayer.rotationYaw - 45);
            RotationHandler.setMoveFix(RotationHandler.MoveFix.Silent);
        }

        if (cpuSpeedUpExploit.isToggled())
            Utils.getTimer().timerSpeed = 1.004f;
    }

    @EventListener
    public void onMove(MoveInputEvent event) {
        if (MoveUtil.isMoving())
            event.setJump(true);
    }

    @Override
    public void onDisable() {
        Utils.resetTimer();
    }
}
