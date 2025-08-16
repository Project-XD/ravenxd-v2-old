package keystrokesmod.module.impl.movement.jesus;

import keystrokesmod.event.world.BlockAABBEvent;
import keystrokesmod.event.player.MoveInputEvent;
import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.impl.movement.Jesus;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

public class KarhuJesus extends SubMode<Jesus> {
    public KarhuJesus(String name, @NotNull Jesus parent) {
        super(name, parent);
    }

    @EventListener
    public void onMoveInput(@NotNull MoveInputEvent event) {
        if (BlockUtils.blockRelativeToPlayer(0, -1, 0) instanceof BlockLiquid)
            event.setJump(false);
    }

    @EventListener
    public void onBlockAABB(@NotNull BlockAABBEvent event) {
        if (event.getBlock() instanceof BlockLiquid && !mc.gameSettings.keyBindSneak.isKeyDown()) {
            final int x = event.getBlockPos().getX();
            final int y = event.getBlockPos().getY();
            final int z = event.getBlockPos().getZ();

            event.setBoundingBox(AxisAlignedBB.fromBounds(x, y, z, x + 1, y + 1, z + 1));
        }
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (Utils.inLiquid()) {
            event.setPosY(event.getPosY() - (mc.thePlayer.ticksExisted % 2 == 0 ? 0.015625 : 0));
            event.setOnGround(false);
        }
    }
}
