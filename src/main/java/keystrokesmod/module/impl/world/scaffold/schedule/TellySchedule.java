package keystrokesmod.module.impl.world.scaffold.schedule;

import keystrokesmod.event.player.JumpEvent;
import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.event.player.ScaffoldPlaceEvent;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSchedule;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class TellySchedule extends IScaffoldSchedule {
    private final SliderSetting straightTicks;
    private final SliderSetting diagonalTicks;
    private final SliderSetting jumpDownTicks;

    private boolean noPlace = false;
    private boolean lastDiagonal = false;

    public TellySchedule(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(straightTicks = new SliderSetting("Straight ticks", 6, 1, 8, 1));
        this.registerSetting(diagonalTicks = new SliderSetting("Diagonal ticks", 4, 1, 8, 1));
        this.registerSetting(jumpDownTicks = new SliderSetting("Jump down ticks", 1, 1, 8, 1));
    }

    @Override
    public boolean noPlace() {
        return noPlace || (mc.thePlayer.onGround && parent.placeBlock == null);
    }

    @Override
    public boolean noRotation() {
        return noPlace();
    }

    @EventListener(priority = -1)
    public void onPreUpdate(PreUpdateEvent event) {
        if (parent.offGroundTicks == 0) {
            RotationHandler.setRotationYaw(mc.thePlayer.rotationYaw);
            RotationHandler.setRotationPitch(mc.thePlayer.rotationPitch);
            noPlace = true;
            lastDiagonal = false;
            if (parent.onGroundTicks >= 1 && MoveUtil.isMoving() && !Utils.jumpDown()) {
                MoveUtil.jump();
            }
        } else {
            if (BlockUtils.insideBlock(mc.thePlayer.getEntityBoundingBox().offset(
                    mc.thePlayer.motionX, mc.thePlayer.motionY + 0.1, mc.thePlayer.motionZ))) {
                noPlace = true;
            } else {
                if (Utils.jumpDown()) {
                    if (parent.offGroundTicks >= (int) jumpDownTicks.getInput()) {
                        noPlace = false;
                        lastDiagonal = false;
                    }
                } else {
                    if (parent.isDiagonal() || lastDiagonal) {
                        if (parent.offGroundTicks == (int) diagonalTicks.getInput()) {
                            noPlace = false;
                            lastDiagonal = true;
                        }
                    } else {
                        if (parent.offGroundTicks == (int) straightTicks.getInput()) {
                            noPlace = false;
                            lastDiagonal = false;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() throws Throwable {
        noPlace = false;
    }

    @EventListener
    public void onScaffoldPlace(ScaffoldPlaceEvent event) {
        if (noPlace())
            event.cancel();
    }

    @EventListener
    public void onJump(JumpEvent event) {
        if (parent.offGroundTicks == 0 && parent.onGroundTicks == 0)
            event.cancel();
    }
}
