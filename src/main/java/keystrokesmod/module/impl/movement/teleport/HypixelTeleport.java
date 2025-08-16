package keystrokesmod.module.impl.movement.teleport;

import keystrokesmod.event.client.ClickEvent;
import keystrokesmod.event.network.SendPacketEvent;
import keystrokesmod.event.player.MoveEvent;
import keystrokesmod.event.player.MoveInputEvent;
import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.module.impl.movement.Teleport;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.RotationUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MovingObjectPosition;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HypixelTeleport extends SubMode<Teleport> {
    private final Queue<Packet<?>> delayedPackets = new ConcurrentLinkedQueue<>();
    private State state = State.NONE;
    private int hasLag = 0;
    private int timerTicks = -1;
    private float yaw, pitch;

    public HypixelTeleport(String name, @NotNull Teleport parent) {
        super(name, parent);
    }

    @EventListener
    public void onClick(ClickEvent event) {
        if (timerTicks != -1) return;
        MovingObjectPosition hitResult = RotationUtils.rayCast(15, RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
        if (hitResult != null && hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            timerTicks = (int) Math.floor(new Vec3(hitResult.getBlockPos()).distanceTo(mc.thePlayer) / MoveUtil.getAllowedHorizontalDistance());
            event.cancel();
        }
    }

    @EventListener
    public void onMove(MoveInputEvent event) {
        if (state == State.TIMER) {
            event.setForward(1);
            event.setStrafe(0);
        }
    }

    @Override
    public void onUpdate() {
        switch (state) {
            case NONE:
                if (timerTicks != -1)
                    state = State.TIMER;
                break;
            case TIMER:
                for (int i = 0; i < timerTicks; i++) {
                    mc.thePlayer.onUpdate();
                }
                state = State.LAG;
                break;
            case LAG:
                if (hasLag >= timerTicks + 2)
                    done();
                else
                    hasLag++;
                break;
        }
    }

    @EventListener(priority = 1)
    public void onSendPacket(SendPacketEvent event) {
        switch (state) {
            case NONE:
                if (event.getPacket() instanceof C03PacketPlayer) {
                    if (!MoveUtil.isMoving() && event.getPacket().getClass() == C03PacketPlayer.class) {
                        event.cancel();
                        if (hasLag == 0) {
                            yaw = mc.thePlayer.rotationYaw;
                            pitch = mc.thePlayer.rotationPitch;
                        }
                        hasLag++;
                    } else {
                        hasLag = 0;
                    }
                }
                break;
            case TIMER:
                synchronized (delayedPackets) {
                    delayedPackets.add(event.getPacket());
                    event.cancel();
                }
                break;
            case LAG:
                if (event.getPacket() instanceof C03PacketPlayer) {
                    event.cancel();
                } else {
                    synchronized (delayedPackets) {
                        delayedPackets.add(event.getPacket());
                        event.cancel();
                    }
                }
                break;
        }
    }

    @EventListener
    public void onMove(@NotNull MoveEvent event) {
        if (state == State.LAG) {
            event.cancel();
            mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
        }
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (state == State.NONE && hasLag > 0) {
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }

    @Override
    public void onDisable() {
        done();
    }

    private void done() {
        state = State.NONE;
        hasLag = 0;
        timerTicks = -1;

        synchronized (delayedPackets) {
            for (Packet<?> p : delayedPackets) {
                PacketUtils.sendPacket(p);
            }
            delayedPackets.clear();
        }

        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    enum State {
        NONE,
        TIMER,
        LAG
    }
}
