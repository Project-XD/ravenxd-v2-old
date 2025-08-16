package keystrokesmod.module.impl.player.selfdamage;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.jetbrains.annotations.NotNull;

public class VanillaSelfDamage extends SubMode<Module> implements ISelfDamage {
    public VanillaSelfDamage(String name, @NotNull Module parent) {
        super(name, parent);
    }

    @Override
    public void damage() {
        PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(
                mc.thePlayer.posX, mc.thePlayer.posY + 4, mc.thePlayer.posZ, false
        ));
        PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false
        ));
    }
}
