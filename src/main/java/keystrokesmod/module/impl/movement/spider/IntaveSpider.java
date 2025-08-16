package keystrokesmod.module.impl.movement.spider;

import keystrokesmod.event.player.MoveEvent;
import keystrokesmod.module.impl.movement.Spider;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class IntaveSpider extends SubMode<Spider> {
    public IntaveSpider(String name, @NotNull Spider parent) {
        super(name, parent);
    }

    @EventListener
    public void onMove(MoveEvent event) {
        if (!mc.thePlayer.isCollidedHorizontally || mc.thePlayer.isOnLadder() || mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.isUsingItem())
            return;

        event.setY(0.2);
        mc.thePlayer.motionY = 0;
    }
}
