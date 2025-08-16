package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.event.player.ScaffoldPlaceEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.exploit.disabler.hypixel.HypixelMotionDisabler;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.aim.RotationData;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class HypixelJumpSprint extends JumpSprint {
    private final ButtonSetting lowHop;
    private final SliderSetting delayTicks;
    private int delay = 0;
    private boolean cycle = false;

    public HypixelJumpSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(lowHop = new ButtonSetting("Low hop", true));
        this.registerSetting(delayTicks = new SliderSetting("Delay", 1, 1, 3, 1, "tick"));
    }

    @EventListener
    public void onPreUpdate(PreUpdateEvent event) {
        if (lowHop.isToggled() && parent.offGroundTicks == 5 && HypixelMotionDisabler.isDisabled()
                && !parent.isDiagonal() && !ModuleManager.tower.canTower()) {
            if (cycle)
                mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
            cycle = !cycle;
        }
    }

    @Override
    public RotationData onFinalRotation(RotationData data) {
        if (mc.thePlayer.onGround && MoveUtil.isMoving() && parent.placeBlock != null && !ModuleManager.tower.canTower() && !Utils.jumpDown()) {
            delay = (int) delayTicks.getInput();
        }

        if (delay > 0) {
            return new RotationData(
                    (float) (data.getYaw() - 180 - parent.getRandom() * 5),
                    (float) Utils.limit(data.getPitch() - parent.getRandom() * 5, -90, 90)
            );
        }
        return data;
    }

    @EventListener
    public void onPlace(ScaffoldPlaceEvent event) {
        if (delay > 0) {
            event.cancel();
        }
    }

    @Override
    public void onUpdate() throws Throwable {
        if (delay > 0) {
            delay--;
        }
    }
}
