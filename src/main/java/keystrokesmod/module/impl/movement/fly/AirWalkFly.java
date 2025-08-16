package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.world.BlockAABBEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import net.minecraft.util.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

public class AirWalkFly extends SubMode<Fly> {
    public AirWalkFly(String name, @NotNull Fly parent) {
        super(name, parent);
    }

    @EventListener
    public void onBlockAABB(@NotNull BlockAABBEvent event) {
        if (BlockUtils.replaceable(event.getBlockPos())) {
            final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

            if (y < mc.thePlayer.posY) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
            }
        }
    }
}
