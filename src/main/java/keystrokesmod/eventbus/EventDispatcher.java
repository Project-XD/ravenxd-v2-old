package keystrokesmod.eventbus;

import keystrokesmod.Client;
import keystrokesmod.event.client.PreTickEvent;
import keystrokesmod.event.network.ClientChatReceivedEvent;
import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.event.render.*;
import keystrokesmod.event.world.BlockPlaceEvent;
import keystrokesmod.event.world.EntityJoinWorldEvent;
import keystrokesmod.event.world.WorldChangeEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.utility.Utils;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.Client.mc;

public final class EventDispatcher {
    private static final EventDispatcher INSTANCE = new EventDispatcher();

    private static WorldClient lastWorld = null;

    public static void init() {
        Client.EVENT_BUS.register(INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    @EventListener
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) event.getPacket();
            Client.EVENT_BUS.post(new ClientChatReceivedEvent(packet.getChatComponent(), packet.getType()));
        }
    }

    @EventListener
    public void onPreTick(PreTickEvent event) {
        if (!Utils.nullCheck()) return;

        if (mc.theWorld != lastWorld) {
            lastWorld = mc.theWorld;
            Client.EVENT_BUS.post(new WorldChangeEvent());
        }
    }

    /**
     * TODO Recode this with mixin for better performance and obf compatibility.
     */
    @SubscribeEvent
    public void onPreRenderNameTag(RenderLivingEvent.Specials.@NotNull Pre<EntityLivingBase> baseEvent) {
        PreRenderNameTag event = new PreRenderNameTag(baseEvent.entity, baseEvent.x, baseEvent.y, baseEvent.z);
        Client.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            baseEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorldEvent(net.minecraftforge.event.entity.@NotNull EntityJoinWorldEvent event) {
        Client.EVENT_BUS.post(new EntityJoinWorldEvent(event.entity));
    }

    @SubscribeEvent
    public void onBlockPlaceEvent(BlockEvent.@NotNull PlaceEvent event) {
        Client.EVENT_BUS.post(new BlockPlaceEvent(event.player, event.pos, event.state));
    }

    @SubscribeEvent
    public void onDrawBlockHighlightEvent(net.minecraftforge.client.event.@NotNull DrawBlockHighlightEvent event) {
        Client.EVENT_BUS.post(new DrawBlockHighlightEvent(
                event.context, event.player, event.target, event.subID, event.currentItem, event.partialTicks
        ));
    }

    @SubscribeEvent
    public void onFOVUpdate(net.minecraftforge.client.event.@NotNull FOVUpdateEvent baseEvent) {
        if (baseEvent.entity != mc.thePlayer) return;
        FOVUpdateEvent event = new FOVUpdateEvent(baseEvent.fov, baseEvent.newfov);
        Client.EVENT_BUS.post(event);
        baseEvent.newfov = event.getNewFov();
    }

    @SubscribeEvent
    public void onPreRenderPlayer(RenderPlayerEvent.@NotNull Pre baseEvent) {
        if (!(baseEvent.entity instanceof EntityPlayer)) return;
        PreRenderPlayerEvent event = new PreRenderPlayerEvent(
                (EntityPlayer) baseEvent.entity, baseEvent.renderer, baseEvent.partialRenderTick,
                baseEvent.x, baseEvent.y, baseEvent.z
        );
        Client.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            baseEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPostRenderPlayer(RenderPlayerEvent.@NotNull Post baseEvent) {
        if (!(baseEvent.entity instanceof EntityPlayer)) return;
        Client.EVENT_BUS.post(new PostRenderPlayerEvent(
                (EntityPlayer) baseEvent.entity, baseEvent.renderer, baseEvent.partialRenderTick,
                baseEvent.x, baseEvent.y, baseEvent.z
        ));
    }
}
