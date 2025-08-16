package keystrokesmod.module.impl.player.antivoid;

import keystrokesmod.event.network.SendPacketEvent;
import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.event.player.PreMoveEvent;
import keystrokesmod.event.player.RotationEvent;
import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.player.AntiVoid;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import lombok.Getter;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GrimACAntiVoid extends SubMode<AntiVoid> {
    private final SliderSetting distance;
    private final ButtonSetting allowPearl;

    @Getter
    private static boolean airStuck = false;
    private float yaw, pitch;
    private @Nullable C08PacketPlayerBlockPlacement delayedPacket = null;

    public GrimACAntiVoid(String name, @NotNull AntiVoid parent) {
        super(name, parent);
        this.registerSetting(distance = new SliderSetting("Distance", 5, 0, 10, 1));
        this.registerSetting(allowPearl = new ButtonSetting("Allow pearl", true));
    }

    @Override
    public void onDisable() throws Throwable {
        airStuck = false;
    }

    @Override
    public void onUpdate() throws Throwable {
        if (mc.thePlayer.fallDistance > distance.getInput() && Utils.overVoid() && !mc.thePlayer.onGround) {
            if (!airStuck) {
                yaw = RotationHandler.getRotationYaw();
                pitch = RotationHandler.getRotationPitch();
            }
            airStuck = true;
        } else {
            airStuck = false;
        }
    }

    @EventListener
    public void onPreMove(PreMoveEvent event) {
        if (airStuck) {
            event.cancel();
        }
    }

    @EventListener(priority = -2)
    public void onPreMotion(PreMotionEvent event) {
        if (airStuck) {
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }

    @EventListener(priority = -2)
    public void onRotation(RotationEvent event) {
        if (airStuck) {
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }

    @EventListener
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (!airStuck) return;
        if (event.getPacket() instanceof C03PacketPlayer) {
            if (delayedPacket != null) {
                PacketUtils.sendPacketNoEvent(delayedPacket);
                delayedPacket = null;
            }
            event.cancel();
        } else if (event.getPacket() instanceof C08PacketPlayerBlockPlacement && allowPearl.isToggled()) {
            ItemStack item = SlotHandler.getHeldItem();
            if (item != null && item.getItem() == Items.ender_pearl) {
                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
                delayedPacket = (C08PacketPlayerBlockPlacement) event.getPacket();
                event.cancel();
            }
        }
    }

    @EventListener
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            airStuck = false;
        }
    }
}
