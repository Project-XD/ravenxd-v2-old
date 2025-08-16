package keystrokesmod.mixins.impl.client;


import keystrokesmod.Client;
import keystrokesmod.event.network.AttackEntityEvent;
import keystrokesmod.module.impl.other.SlotHandler;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    @Shadow private int currentPlayerItem;

    @Shadow @Final private NetHandlerPlayClient netClientHandler;

    /**
     * @author xia__mc
     * @reason for SlotHandler (silent switch)
     */
    @Inject(method = "syncCurrentPlayItem", at = @At("HEAD"), cancellable = true)
    private void syncCurrentPlayItem(CallbackInfo ci) {
        int i = SlotHandler.getCurrentSlot();
        if (i != this.currentPlayerItem) {
            this.currentPlayerItem = i;
            this.netClientHandler.addToSendQueue(new C09PacketHeldItemChange(this.currentPlayerItem));
        }

        ci.cancel();
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(EntityPlayer self, Entity target, CallbackInfo ci) {
        AttackEntityEvent event = new AttackEntityEvent(target);
        Client.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
