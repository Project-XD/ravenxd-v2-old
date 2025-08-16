package keystrokesmod.module.impl.movement.spider;

import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.module.impl.movement.Spider;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.util.MathHelper;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class VulcanSpider extends SubMode<Spider> {
    public VulcanSpider(String name, @NotNull Spider parent) {
        super(name, parent);
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.isCollidedHorizontally) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                event.setOnGround(true);
                mc.thePlayer.motionY = MoveUtil.jumpMotion();
            }

            final double yaw = MoveUtil.direction();
            event.setPosX(event.getPosX() - -MathHelper.sin((float) yaw) * 0.1f);
            event.setPosZ(event.getPosZ() - MathHelper.cos((float) yaw) * 0.1f);
        }
    }
}
