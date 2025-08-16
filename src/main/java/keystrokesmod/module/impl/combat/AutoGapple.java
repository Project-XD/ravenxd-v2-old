package keystrokesmod.module.impl.combat;

import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.event.network.SendPacketEvent;
import keystrokesmod.event.player.*;
import keystrokesmod.event.world.WorldChangeEvent;
import keystrokesmod.mixins.impl.entity.EntityPlayerAccessor;
import keystrokesmod.mixins.impl.entity.EntityPlayerSPAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.player.antivoid.GrimACAntiVoid;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.*;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.progress.Progress;
import keystrokesmod.utility.render.progress.ProgressManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.event.render.Render2DEvent;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoGapple extends Module {
    private final SliderSetting minHealth;
    private final SliderSetting sendDelay;
    private final SliderSetting delay;
    private final ButtonSetting visual;
    private final ButtonSetting onlyWhileKillAura;
    private final ButtonSetting stopMovement;

    private final Animation animation = new Animation(Easing.EASE_OUT_CIRC, 500);
    private final Progress progress = new Progress("AutoGapple");
    private final CoolDown stopWatch = new CoolDown(0);
    public static boolean eating = false;
    private int movingPackets = 0;
    private int slot = 0;
    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private boolean needSkip = false;

    public AutoGapple() {
        super("AutoGapple", category.combat);
        this.registerSetting(minHealth = new SliderSetting("Min health", 10, 1, 20, 1));
        this.registerSetting(sendDelay = new SliderSetting("Send delay", 3, 2, 10, 1));
        this.registerSetting(delay = new SliderSetting("Delay", 250, 0, 500, 50));
        this.registerSetting(visual = new ButtonSetting("Visual", true));
        this.registerSetting(onlyWhileKillAura = new ButtonSetting("Only while killAura", true));
        this.registerSetting(stopMovement = new ButtonSetting("Stop movement", false));
    }

    @Override
    public void onEnable() {
        packets.clear();
        slot = -1;
        needSkip = false;
        movingPackets = 0;
        eating = false;
        animation.reset();
    }

    @Override
    public void onDisable() {
        eating = false;
        release();
    }

    @EventListener
    public void onWorldChange(WorldChangeEvent event) {
        eating = false;
        release();
    }

    private void release() {
        while (!packets.isEmpty()) {
            final Packet<?> packet = packets.poll();

            if (packet instanceof C01PacketChatMessage
                    || packet instanceof C08PacketPlayerBlockPlacement
                    || packet instanceof C07PacketPlayerDigging)
                continue;

            PacketUtils.sendPacketNoEvent(packet);
        }

        movingPackets = 0;
    }

    @EventListener
    public void onMoveInput(MoveInputEvent event) {
        if (eating && stopMovement.isToggled()) {
            event.setForward(0);
            event.setStrafe(0);
        }
    }

    @EventListener
    public void onPreMove(PreMoveEvent event) {
        if (eating
                && ((EntityPlayerSPAccessor) mc.thePlayer).getPositionUpdateTicks() < 20
                && !needSkip) {
            event.cancel();
        } else if (needSkip) {
            needSkip = false;
        }
    }

    @EventListener(priority = -2)
    public void onPostMotion(PostMotionEvent event) {
        if (eating) {
            movingPackets++;
            packets.add(new C01PacketChatMessage("release"));
        }
    }
    
    @EventListener(priority = -2)
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) {
            eating = false;
            packets.clear();

            return;
        }

        if (!mc.playerController.getCurrentGameType().isSurvivalOrAdventure()
                || (onlyWhileKillAura.isToggled() && KillAura.target == null)
                || GrimACAntiVoid.isAirStuck()
                || !stopWatch.finished((long) delay.getInput())) {
            eating = false;
            release();

            return;
        }

        slot = getFoodSlot();

        if (slot == -1 || mc.thePlayer.getHealth() >= minHealth.getInput()) {
            if (eating) {
                eating = false;
                release();
            }
        } else {
            eating = true;
            if (movingPackets >= 32) {
                if (slot != mc.thePlayer.inventory.currentItem)
                    PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(slot));
                PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)));
                ((EntityPlayerAccessor) mc.thePlayer).setItemInUseCount(mc.thePlayer.getItemInUseCount() - 32);
                release();
                if (slot != mc.thePlayer.inventory.currentItem)
                    PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                stopWatch.start();
                animation.reset();
            } else if (mc.thePlayer.ticksExisted % (int) sendDelay.getInput() == 0) {
                while (!packets.isEmpty()) {
                    final Packet<?> packet = packets.poll();

                    if (packet instanceof C01PacketChatMessage) {
                        break;
                    }

                    if (packet instanceof C03PacketPlayer) {
                        movingPackets--;
                    }

                    PacketUtils.sendPacketNoEvent(packet);
                }
            }
        }
    }

    @EventListener
    public void onSendPacket(SendPacketEvent event) {
        if (!Utils.nullCheck()
                || !mc.playerController.getCurrentGameType().isSurvivalOrAdventure()
                || GrimACAntiVoid.isAirStuck()) return;

        final Packet<?> packet = event.getPacket();

        if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart ||
                packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing ||
                packet instanceof C01PacketEncryptionResponse || packet instanceof C01PacketChatMessage) {
            return;
        }

        if (!(packet instanceof C09PacketHeldItemChange) &&
                !(packet instanceof C0EPacketClickWindow) &&
                !(packet instanceof C16PacketClientStatus) &&
                !(packet instanceof C0DPacketCloseWindow)) {
            if (eating) {
                event.cancel();

                packets.add(packet);
            }
        }
    }

    @EventListener
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapped = (S12PacketEntityVelocity) packet;

            if (wrapped.getEntityID() == mc.thePlayer.getEntityId())
                needSkip = true;
        }
    }

    public int getFoodSlot() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() == Items.golden_apple) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    @EventListener
    public void onRenderTick(Render2DEvent event) {
        animation.run(Math.min(movingPackets, 32));
        if (eating && visual.isToggled()) {
            progress.setProgress(animation.getValue() / 32);
            ProgressManager.add(progress);
        } else {
            ProgressManager.remove(progress);
        }
    }
}
