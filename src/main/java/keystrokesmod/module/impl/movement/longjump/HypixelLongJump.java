package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.event.world.BlockAABBEvent;
import keystrokesmod.event.player.PrePlayerInputEvent;
import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.event.player.RotationEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;

public class HypixelLongJump extends SubMode<LongJump> {
    private int startY = -1;

    public HypixelLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
    }

    @EventListener
    public void onRotation(@NotNull RotationEvent event) {
        event.setYaw(mc.thePlayer.rotationYaw - 110);
        event.setPitch(85);
    }

    @EventListener
    public void onBlockAABB(@NotNull BlockAABBEvent event) {
        if (BlockUtils.replaceable(event.getBlockPos())) {
            final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

            if (y < startY) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
            }
        }
    }

    @EventListener
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (startY == -1) {
            if (mc.thePlayer.onGround)
                startY = (int) mc.thePlayer.posY;
            return;
        }

        if (Utils.isMoving() && mc.currentScreen == null && mc.thePlayer.onGround) {
            MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
            MoveUtil.jump();
        }

        BlockPos ground = new BlockPos(mc.thePlayer.posX, startY - 1, mc.thePlayer.posZ);
        if (BlockUtils.replaceable(ground)) {
            BlockPos pos = ground.down();
            PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(
                    pos, EnumFacing.UP.getIndex(), SlotHandler.getHeldItem(),
                    (float) (mc.thePlayer.posX - pos.getX()),
                    (float) (mc.thePlayer.posY - pos.getY()),
                    (float) (mc.thePlayer.posZ - pos.getZ())));
            mc.thePlayer.swingItem();
            if (mc.thePlayer.onGround)
                parent.disable();
        }
    }

    @EventListener
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Anti-cheat flagged.");
            parent.disable();
        }
    }

    @Override
    public void onDisable() throws Throwable {
        startY = -1;
    }
}
