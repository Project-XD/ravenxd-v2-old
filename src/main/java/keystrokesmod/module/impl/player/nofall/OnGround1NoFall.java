package keystrokesmod.module.impl.player.nofall;

import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.module.impl.player.NoFall;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class OnGround1NoFall extends SubMode<NoFall> {
    private final SliderSetting minFallDist;

    public OnGround1NoFall(String name, @NotNull NoFall parent) {
        super(name, parent);
        this.registerSetting(minFallDist = new SliderSetting("Min fall distance", 4, 1, 24, 1));
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.fallDistance > minFallDist.getInput() && !parent.noAction()) {
            PacketUtils.sendPacket(new C03PacketPlayer(true));
        }
    }
}
