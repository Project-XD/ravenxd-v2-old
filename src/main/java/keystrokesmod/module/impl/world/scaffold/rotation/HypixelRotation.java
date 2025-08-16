package keystrokesmod.module.impl.world.scaffold.rotation;

import keystrokesmod.event.player.RotationEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldRotation;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.aim.RotationData;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;

public class HypixelRotation extends IScaffoldRotation {
    private double lastOffsetToMid = -1;

    public HypixelRotation(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public @NotNull RotationData onRotation(float placeYaw, float placePitch, boolean forceStrict, @NotNull RotationEvent event) {
        if (!MoveUtil.isMoving()) {
            return new RotationData(placeYaw, placePitch);
        }

        return new RotationData(applyStrafe(parent.getYaw()), placePitch);
    }

    private float applyStrafe(float yaw) {
        if (parent.isDiagonal()) {
            return yaw - 75;
        } else {
            double offsetToMid = EnumFacing.fromAngle(yaw).getAxis() == EnumFacing.Axis.X ? Math.abs(mc.thePlayer.posZ % 1) : Math.abs(mc.thePlayer.posX % 1);
            if (offsetToMid > 0.6 || offsetToMid < 0.4 || lastOffsetToMid == -1) {
                lastOffsetToMid = offsetToMid;
            }
            return yaw + (float) (lastOffsetToMid >= 0.5 ? 75 : -75);
        }
    }
}
