package keystrokesmod.module.impl.movement.spider;

import keystrokesmod.event.world.BlockAABBEvent;
import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.event.world.PushOutOfBlockEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.impl.movement.Spider;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;

public class TestSpider extends SubMode<Spider> {
    public TestSpider(String name, @NotNull Spider parent) {
        super(name, parent);
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.isCollidedHorizontally && !BlockUtils.insideBlock()) {
            double yaw = MoveUtil.direction();
            mc.thePlayer.setPosition(
                    mc.thePlayer.posX + -MathHelper.sin((float) yaw) * 0.05,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ + MathHelper.cos((float) yaw) * 0.05
            );
            MoveUtil.stop();
            ((KeyBindingAccessor) mc.gameSettings.keyBindForward).setPressed(false);
        }
    }

    @EventListener
    public void onPushOutOfBlock(@NotNull PushOutOfBlockEvent event) {
        if (BlockUtils.insideBlock())
            event.cancel();
    }

    @EventListener
    public void onAABB(BlockAABBEvent event) {
        if (BlockUtils.insideBlock()) {
            BlockPos playerPos = new BlockPos(mc.thePlayer);
            BlockPos blockPos = event.getBlockPos();
            if (blockPos.getY() > playerPos.getY())
                event.setBoundingBox(null);
        }
    }
}
