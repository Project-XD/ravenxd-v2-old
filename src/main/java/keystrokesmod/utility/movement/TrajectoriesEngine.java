package keystrokesmod.utility.movement;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.Utils;
import lombok.Getter;
import lombok.var;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static keystrokesmod.Client.mc;

public class TrajectoriesEngine {
    @Getter
    private final List<Vec3> predictPoses = new ObjectArrayList<>(64);
    @Getter
    private MovingObjectPosition target = null;
    private double posX, posY, posZ, motionX, motionY, motionZ;
    private boolean isArrow;

    public void requestUpdate() {
        ItemStack heldItem = SlotHandler.getHeldItem();
        if (heldItem == null) return;
        isArrow = heldItem.getItem() instanceof ItemBow;

        // to support airstuck antivoid (grimac)
        float playerYaw = mc.thePlayer.rotationYaw;
        float playerPitch = mc.thePlayer.rotationPitch;

        posX = mc.getRenderManager().viewerPosX - (double) (MathHelper.cos(playerYaw / 180.0f * (float) Math.PI) * 0.16f);
        posY = mc.getRenderManager().viewerPosY + (double) Utils.getEyeHeight() - (double) 0.1f;
        posZ = mc.getRenderManager().viewerPosZ - (double) (MathHelper.sin(playerYaw / 180.0f * (float) Math.PI) * 0.16f);

        motionX = (double) (-MathHelper.sin(playerYaw / 180.0f * (float) Math.PI) * MathHelper.cos(playerPitch / 180.0f * (float) Math.PI)) * (isArrow ? 1.0 : 0.4);
        motionY = (double) (-MathHelper.sin(playerPitch / 180.0f * (float) Math.PI)) * (isArrow ? 1.0 : 0.4);
        motionZ = (double) (MathHelper.cos(playerYaw / 180.0f * (float) Math.PI) * MathHelper.cos(playerPitch / 180.0f * (float) Math.PI)) * (isArrow ? 1.0 : 0.4);
        int itemInUse = 40;
        if (mc.thePlayer.getItemInUseCount() > 0 && isArrow) {
            itemInUse = mc.thePlayer.getItemInUseCount();
        }
        int n10 = 72000 - itemInUse;
        float f10 = (float) n10 / 20.0f;
        if ((double) (f10 = (f10 * f10 + f10 * 2.0f) / 3.0f) < 0.1) {
            return;
        }
        if (f10 > 1.0f) {
            f10 = 1.0f;
        }
        float f11 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= f11;
        motionY /= f11;
        motionZ /= f11;
        motionX *= (double) (isArrow ? f10 * 2.0f : 1.0f) * 1.5;
        motionY *= (double) (isArrow ? f10 * 2.0f : 1.0f) * 1.5;
        motionZ *= (double) (isArrow ? f10 * 2.0f : 1.0f) * 1.5;

        predict();
    }

    private void predict() {
        boolean ground = false;

        predictPoses.clear();
        for (int k = 0; k <= 100 && !ground; ++k) {
            var start = new net.minecraft.util.Vec3(posX, posY, posZ);
            var predicted = new net.minecraft.util.Vec3(
                    posX + motionX, posY + motionY, posZ + motionZ
            );
            MovingObjectPosition rayTraced = mc.theWorld.rayTraceBlocks(start, predicted,
                    false, true, false);
            if (rayTraced != null) {
                ground = true;
                target = rayTraced;
            } else {
                MovingObjectPosition entityHit = getEntityHit(start, predicted);
                if (entityHit != null) {
                    target = entityHit;
                    ground = true;
                }
            }
            float f14 = 0.99f;
            motionY *= f14;
            double x = (posX += (motionX *= f14)) - mc.getRenderManager().viewerPosX;
            double y = (posY += (motionY -= isArrow ? 0.05 : 0.03)) - mc.getRenderManager().viewerPosY;
            double z = (posZ += (motionZ *= f14)) - mc.getRenderManager().viewerPosZ;
            predictPoses.add(new Vec3(x, y, z));
        }
    }

    private static @Nullable MovingObjectPosition getEntityHit(
            net.minecraft.util.Vec3 origin, net.minecraft.util.Vec3 destination) {
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityLivingBase)) {
                continue;
            }
            if (e instanceof EntityPlayer && AntiBot.isBot(e)) {
                continue;
            }
            if (e != mc.thePlayer) {
                float expand = 0.3f;
                AxisAlignedBB boundingBox = e.getEntityBoundingBox().expand(expand, expand, expand);
                MovingObjectPosition possibleHit = boundingBox.calculateIntercept(origin, destination);
                if (possibleHit != null) {
                    return possibleHit;
                }
            }
        }
        return null;
    }

}
