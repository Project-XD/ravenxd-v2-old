package keystrokesmod.script;

import keystrokesmod.Client;
import keystrokesmod.event.network.SendPacketEvent;
import keystrokesmod.event.player.*;
import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.event.render.Render3DEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.Module;
import keystrokesmod.script.classes.Entity;
import keystrokesmod.script.classes.PlayerState;
import keystrokesmod.script.packets.clientbound.SPacket;
import keystrokesmod.script.packets.serverbound.CPacket;
import keystrokesmod.script.packets.serverbound.PacketHandler;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import keystrokesmod.event.network.ClientChatReceivedEvent;
import keystrokesmod.event.client.MouseEvent;
import keystrokesmod.event.world.EntityJoinWorldEvent;
import keystrokesmod.event.render.Render2DEvent;
import org.jetbrains.annotations.NotNull;

public class ScriptEvents {
    public Module module;

    public ScriptEvents(Module module) {
        this.module = module;
    }

    @EventListener
    public void onChat(@NotNull ClientChatReceivedEvent e) {
        if (e.getType() == 2 || !Utils.nullCheck()) {
            return;
        }
        final String r = Utils.stripColor(e.getMessage().getUnformattedText());
        if (r.isEmpty()) {
            return;
        }
        if (Client.scriptManager.invokeBoolean("onChat", module, e.getMessage().getUnformattedText()) == 0) {
            e.cancel();
        }
    }

    @EventListener
    public void onSendPacket(@NotNull SendPacketEvent e) {
        if (e.isCancelled() || e.getPacket() == null) {
            return;
        }
        if (e.getPacket().getClass().getSimpleName().startsWith("S")) {
            return;
        }
        CPacket a = PacketHandler.convertServerBound(e.getPacket());
        if (a != null && Client.scriptManager.invokeBoolean("onPacketSent", module, a) == 0) {
            e.cancel();
        }
    }

    @EventListener
    public void onReceivePacket(@NotNull ReceivePacketEvent e) {
        if (e.isCancelled() || e.getPacket() == null) {
            return;
        }
        SPacket a = PacketHandler.convertClientBound(e.getPacket());
        if (a != null && Client.scriptManager.invokeBoolean("onPacketReceived", module, a) == 0) {
            e.cancel();
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (!Utils.nullCheck()) {
            return;
        }
        Client.scriptManager.invoke("onRenderWorld", module, event.getPartialTicks());
    }

    @EventListener
    public void onPreUpdate(PreUpdateEvent e) {
        Client.scriptManager.invoke("onPreUpdate", module);
    }

    @EventListener
    public void onPostUpdate(PostUpdateEvent e) {
        Client.scriptManager.invoke("onPostUpdate", module);
    }

    @EventListener
    public void onRenderTick(Render2DEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }
        Client.scriptManager.invoke("onRenderTick", module, e.getPartialTicks());
    }

    @EventListener
    public void onPreMotion(PreMotionEvent e) {
        PlayerState playerState = new PlayerState(e);
        Client.scriptManager.invoke("onPreMotion", module, playerState);
        if (e.isEquals(playerState)) {
            return;
        }
        if (e.getYaw() != playerState.yaw) {
            e.setYaw(playerState.yaw);
        }
        e.setPitch(playerState.pitch);
        e.setPosX(playerState.x);
        e.setPosY(playerState.y);
        e.setPosZ(playerState.z);
        e.setOnGround(playerState.onGround);
        e.setSprinting(playerState.isSprinting);
        e.setSneaking(playerState.isSneaking);
    }

    @EventListener
    public void onWorldJoin(@NotNull EntityJoinWorldEvent e) {
        if (e.getEntity() == null) {
            return;
        }
        if (e.getEntity() == Minecraft.getMinecraft().thePlayer) {
            Client.scriptManager.invoke("onWorldJoin", module, ScriptDefaults.client.getPlayer());
            ScriptManager.localPlayer = new Entity(Minecraft.getMinecraft().thePlayer);
            return;
        }
        Client.scriptManager.invoke("onWorldJoin", module, new Entity(e.getEntity()));
    }

    @EventListener
    public void onPostInput(PostPlayerInputEvent e) {
        Client.scriptManager.invoke("onPostPlayerInput", module);
    }

    @EventListener
    public void onPostMotion(PostMotionEvent e) {
        Client.scriptManager.invoke("onPostMotion", module);
    }

    @EventListener
    public void onMouse(@NotNull MouseEvent e) {
        if (Client.scriptManager.invokeBoolean("onMouse", module, e.getButton(), e.isButtonstate()) == 0) {
            e.cancel();
        }
    }
}
