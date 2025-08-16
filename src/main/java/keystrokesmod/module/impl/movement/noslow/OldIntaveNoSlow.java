package keystrokesmod.module.impl.movement.noslow;

import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class OldIntaveNoSlow extends INoSlow {
    public OldIntaveNoSlow(String name, @NotNull NoSlow parent) {
        super(name, parent);
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.isUsingItem()) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot() % 8 + 1));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
        }
    }

    @Override
    public float getSlowdown() {
        return 1;
    }
}
