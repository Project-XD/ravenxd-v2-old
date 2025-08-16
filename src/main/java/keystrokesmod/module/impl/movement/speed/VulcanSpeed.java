package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.player.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class VulcanSpeed extends SubMode<Speed> {
    private final SliderSetting lowHop;

    public VulcanSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(lowHop = new SliderSetting("Low hop", 2, 0, 4, 1));
    }

    @EventListener
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!MoveUtil.isMoving()) return;
        switch (parent.offGroundTicks) {
            case 0:
                MoveUtil.jump();

                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe(0.6);
                } else {
                    MoveUtil.strafe(0.485);
                }
                break;

            case 9:
                if (!(blockRelativeToPlayer(0, mc.thePlayer.motionY,
                        0) instanceof BlockAir)) {
                    MoveUtil.strafe();
                }
                break;

            case 2:
            case 1:
                MoveUtil.strafe();
                break;

            case 5:
                mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, (int) lowHop.getInput());
                break;
        }
    }

    public Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(offsetX, offsetY, offsetZ)).getBlock();
    }
}
