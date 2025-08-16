package keystrokesmod.utility;

import keystrokesmod.event.player.PostUpdateEvent;
import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.event.network.SendPacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class BadPacketsHandler { // ensures you don't get banned
    public boolean C08;
    public boolean C07;
    public boolean C09;
    public boolean delayAttack;
    public boolean delay;
    public int playerSlot = -1;
    public int serverSlot = -1;

    @EventListener(priority = 2)
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getPacket() instanceof C02PacketUseEntity) { // sending a C07 on the same tick as C02 can ban, this usually happens when you unblock and attack on the same tick
            if (C07) {
                event.cancel();
            }
        }
        else if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            C08 = true;
        }
        else if (event.getPacket() instanceof C07PacketPlayerDigging) {
            C07 = true;
        }
        else if (event.getPacket() instanceof C09PacketHeldItemChange) {
            if (((C09PacketHeldItemChange) event.getPacket()).getSlotId() == playerSlot && ((C09PacketHeldItemChange) event.getPacket()).getSlotId() == serverSlot) {
                event.cancel();
                return;
            }
            C09 = true;
            serverSlot = playerSlot = ((C09PacketHeldItemChange) event.getPacket()).getSlotId();
        }
    }

    @EventListener
    public void onReceivePacket(ReceivePacketEvent e) {
        if (e.getPacket() instanceof S09PacketHeldItemChange) {
            S09PacketHeldItemChange packet = (S09PacketHeldItemChange) e.getPacket();
            if (packet.getHeldItemHotbarIndex() >= 0 && packet.getHeldItemHotbarIndex() < InventoryPlayer.getHotbarSize()) {
                serverSlot = packet.getHeldItemHotbarIndex();
            }
        }
        else if (e.getPacket() instanceof S0CPacketSpawnPlayer && Minecraft.getMinecraft().thePlayer != null) {
            if (((S0CPacketSpawnPlayer) e.getPacket()).getEntityID() != Minecraft.getMinecraft().thePlayer.getEntityId()) {
                return;
            }
            this.playerSlot = -1;
        }
    }

    @EventListener(priority = 2)
    public void onPostUpdate(PostUpdateEvent e) {
        if (delay) {
            delayAttack = false;
            delay = false;
        }
        if (C08 || C09) {
            delay = true;
            delayAttack = true;
        }
        C08 = C07 = C09 = false;
    }
}
