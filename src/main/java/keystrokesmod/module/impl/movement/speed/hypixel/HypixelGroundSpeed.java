package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.event.network.SendPacketEvent;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.impl.player.blink.NormalBlink;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class HypixelGroundSpeed extends SubMode<HypixelSpeed> {
    private final NormalBlink blink = new NormalBlink("Blink", this) {
        @Override
        public void onSendPacket(@NotNull SendPacketEvent e) {
            if (e.getPacket() instanceof C0FPacketConfirmTransaction)
                return;
            super.onSendPacket(e);
        }
    };

    public HypixelGroundSpeed(String name, @NotNull HypixelSpeed parent) {
        super(name, parent);
    }

    public void onDisable() {
        blink.disable();

        mc.thePlayer.motionX *= .8;
        mc.thePlayer.motionZ *= .8;
    }

    @EventListener(priority = 1)
    public void onPreUpdate(PreMotionEvent event) {
        if (parent.parent.noAction()) {
            blink.disable();
            return;
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionX *= 1.114 - MoveUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
            mc.thePlayer.motionZ *= 1.114 - MoveUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
            MoveUtil.strafe();
        }

        if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted % 2 == 0) {
            blink.enable();
        } else {
            blink.disable();
        }
    }
}
