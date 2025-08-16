package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.event.player.MoveEvent;
import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.event.network.SendPacketEvent;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

public class BWPFly extends SubMode<Fly> {
    // Flight settings
    private final SliderSetting speed = new SliderSetting("Speed", 0.8, 0.1, 2.0, 0.05);
    private final SliderSetting verticalSpeed = new SliderSetting("Vertical speed", 0.4, 0.1, 1.0, 0.05);

    // Disabler settings
    private final SliderSetting transactionDelay = new SliderSetting("Transaction delay", 3, 1, 10, 1, " ticks");
    private final SliderSetting positionVariance = new SliderSetting("Position variance", 0.0001, 0.00001, 0.001, 0.00001);

    // Internal state
    private int groundSpoofTicks;
    private double motionY;
    private int gameTicks;
    private boolean delayingTransactions;
    private final Queue<Packet<?>> delayedPackets = new ConcurrentLinkedQueue<>();

    public BWPFly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(speed);
        this.registerSetting(verticalSpeed);
        this.registerSetting(transactionDelay);
        this.registerSetting(positionVariance);
    }

    @Override
    public void onEnable() {
        groundSpoofTicks = 0;
        motionY = 0;
        gameTicks = 0;
        delayingTransactions = false;
        delayedPackets.clear();
        mc.thePlayer.fallDistance = 0;
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        // Spoof ground status in a natural pattern
        groundSpoofTicks++;
        event.setOnGround(groundSpoofTicks % 10 < 7);

        // Reset fall distance to prevent flags
        mc.thePlayer.fallDistance = 0;

        // Apply vertical motion
        if (Utils.jumpDown()) {
            motionY = verticalSpeed.getInput();
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            motionY = -verticalSpeed.getInput();
        } else {
            motionY *= 0.85; // Smooth vertical decay
        }

        // Handle wall collisions
        if (mc.thePlayer.isCollidedHorizontally) {
            motionY = verticalSpeed.getInput() * 0.7;
        }
    }

    @EventListener
    public void onMove(MoveEvent event) {
        // Horizontal movement
        if (MoveUtil.isMoving()) {
            MoveUtil.strafe(speed.getInput());
            event.setX(mc.thePlayer.motionX);
            event.setZ(mc.thePlayer.motionZ);
        } else {
            event.setX(0);
            event.setZ(0);
        }

        // Vertical movement
        event.setY(motionY);
    }

    @EventListener
    public void onSendPacket(SendPacketEvent event) {
        if (!Utils.nullCheck()) return;

        Packet<?> packet = event.getPacket();

        // Transaction handling
        if (packet instanceof C0FPacketConfirmTransaction) {
            if (delayingTransactions) {
                delayedPackets.add(packet);
                event.cancel();
            }
            return;
        }

        // Position variance
        if (packet instanceof C03PacketPlayer) {
            C03PacketPlayer pp = (C03PacketPlayer) packet;
            if (gameTicks > 60) { // Only after initial grace period
                double offset = positionVariance.getInput() * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                boolean modifyX = ThreadLocalRandom.current().nextBoolean();

                event.setPacket(new C03PacketPlayer.C04PacketPlayerPosition(
                        modifyX ? pp.getPositionX() + offset : pp.getPositionX(),
                        pp.getPositionY(),
                        modifyX ? pp.getPositionZ() : pp.getPositionZ() + offset,
                        pp.isOnGround()
                ));
            }
        }
    }

    @EventListener
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!Utils.nullCheck()) return;

        gameTicks++;

        // Start delaying transactions after initial grace period
        if (gameTicks == 60) { // 3 seconds = 60 ticks
            delayingTransactions = true;
            Utils.sendMessage("§aBypass activated - flight enabled");
        }

        // Handle teleport packets
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            event.cancel();
            Utils.sendMessage("§cPosition correction blocked - maintaining flight");
        }

        // Release delayed packets periodically
        if (event.getPacket() instanceof S32PacketConfirmTransaction) {
            if (gameTicks % (int)transactionDelay.getInput() == 0) {
                releaseDelayedPackets();
            }
        }
    }

    private void releaseDelayedPackets() {
        delayingTransactions = false;
        while (!delayedPackets.isEmpty()) {
            Packet<?> packet = delayedPackets.poll();
            if (packet != null) {
                mc.getNetHandler().addToSendQueue(packet);
            }
        }
        // Reset grace period timer
        gameTicks = 0;
    }

    @Override
    public void onDisable() {
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
        releaseDelayedPackets();
    }
}