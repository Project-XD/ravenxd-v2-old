package keystrokesmod.mixins.impl.network;


import keystrokesmod.Client;
import keystrokesmod.event.player.PostVelocityEvent;
import keystrokesmod.event.player.PreVelocityEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Inject(method = "handleEntityVelocity", at = @At("HEAD"), cancellable = true)
    public void onPreHandleEntityVelocity(S12PacketEntityVelocity packet, CallbackInfo ci) {
        if (!Utils.nullCheck()) return;

        if (packet.getEntityID() == Client.mc.thePlayer.getEntityId()) {
            if (ModuleManager.longJump.isEnabled()) return;

            PreVelocityEvent event = new PreVelocityEvent(packet.getMotionX(), packet.getMotionY(), packet.getMotionZ());
            Client.EVENT_BUS.post(event);
            if (event.isCancelled()) ci.cancel();

            try {
                Reflection.S12PacketEntityVelocityXMotion.set(packet, event.getMotionX());
                Reflection.S12PacketEntityVelocityYMotion.set(packet, event.getMotionY());
                Reflection.S12PacketEntityVelocityZMotion.set(packet, event.getMotionZ());
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    @Inject(method = "handleEntityVelocity", at = @At("RETURN"))
    public void onPostHandleEntityVelocity(S12PacketEntityVelocity packet, CallbackInfo ci) {
        if (!Utils.nullCheck()) return;

        if (packet.getEntityID() == Client.mc.thePlayer.getEntityId()) {
            Client.EVENT_BUS.post(new PostVelocityEvent());
        }
    }
}
