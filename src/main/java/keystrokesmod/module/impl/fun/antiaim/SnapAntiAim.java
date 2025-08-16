package keystrokesmod.module.impl.fun.antiaim;

import keystrokesmod.event.player.JumpEvent;
import keystrokesmod.event.player.RotationEvent;
import keystrokesmod.module.impl.fun.AntiAim;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.movement.Move;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class SnapAntiAim extends SubMode<AntiAim> {
    private final SliderSetting pitch;
    private final SliderSetting minDelay;
    private final SliderSetting maxDelay;
    private final ButtonSetting fastJump;

    private int disableTicks = 0;
    private boolean lastAim = false;
    private float yaw;
    private boolean yawCycle = false;
    private boolean scheduleCycle = false;

    public SnapAntiAim(String name, @NotNull AntiAim parent) {
        super(name, parent);
        this.registerSetting(pitch = new SliderSetting("Pitch", 90, -90, 90, 5));
        this.registerSetting(minDelay = new SliderSetting("Min delay", 1, 1, 5, 1));
        this.registerSetting(maxDelay = new SliderSetting("Max delay", 2, 1, 5, 1));
        this.registerSetting(fastJump = new ButtonSetting("Fast jump", true));
    }

    @Override
    public void guiUpdate() throws Throwable {
        Utils.correctValue(minDelay, maxDelay);
    }

    @EventListener
    public void onJump(JumpEvent event) {
        if (fastJump.isToggled()) {
            if (lastAim)
                event.cancel();
            disableTicks = (2 >= disableTicks) ? 2 : disableTicks;
        }
    }

    @EventListener(priority = 1)
    public void onRotation(@NotNull RotationEvent event) {
        if (!parent.canAntiAim()) {
            return;
        }

        if (disableTicks <= 0) {
            if (MoveUtil.isMoving()) {
                float moveYaw = mc.thePlayer.rotationYaw + Move.fromMovement(mc.thePlayer.moveForward, mc.thePlayer.moveStrafing).getDeltaYaw();
                if (yawCycle) {
                    yaw = moveYaw - (scheduleCycle ? 135 : 225);
                } else {
                    yaw = moveYaw;
                }
                yawCycle = !yawCycle;
            } else {
                yaw = mc.thePlayer.rotationYaw - (scheduleCycle ? 180 : 135) + (float) Math.random() * 5;
            }

            disableTicks = Utils.randomizeInt(minDelay.getInput(), maxDelay.getInput());
            scheduleCycle = !scheduleCycle;
            lastAim = true;
        } else {
            disableTicks--;
            lastAim = false;
        }

        event.setYaw(yaw);
        event.setPitch((float) pitch.getInput());
    }

    @Override
    public void onEnable() throws Throwable {
        lastAim = false;
        disableTicks = 0;
        yaw = mc.thePlayer.rotationYaw;
        yawCycle = false;
    }
}
