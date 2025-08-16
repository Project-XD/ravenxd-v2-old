package keystrokesmod.module.impl.other;

import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.event.network.PreConnectEvent;
import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class FlagDetector extends Module {
    public static short counter = 0;

    public FlagDetector() {
        super("FlagDetector", category.other);
    }

    @EventListener
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && mc.thePlayer.ticksExisted > 40) {
            counter++;
            Utils.sendMessage(ChatFormatting.RED + "Flag Detected: " + ChatFormatting.GRAY + counter);
        }
    }

    @EventListener
    public void onConnect(@NotNull PreConnectEvent event) {
        counter = 0;
    }

    @Override
    public void onUpdate() {
        if (!Utils.nullCheck())
            counter = 0;
    }

    @Override
    public void onEnable() {
        counter = 0;
    }

    @Override
    public void onDisable() {
        counter = 0;
    }
}
