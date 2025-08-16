package keystrokesmod.module.impl.combat;

import keystrokesmod.event.player.MoveInputEvent;
import keystrokesmod.event.player.PostMotionEvent;
import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.event.player.RotationEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.utility.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Mouse;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class BowAimbot extends Module {
    private final SliderSetting range;
    private final SliderSetting fov;
    private final SliderSetting prediction;
    private final ModeSetting rotationMode;
    private final ModeSetting moveFixMode;
    private final SliderSetting rotationSpeed;
    private final SliderSetting randomization;
    private final ButtonSetting autoShoot;
    private final ButtonSetting requireMouseDown;
    private final ButtonSetting silentSwing;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting targetInvisible;
    private final ButtonSetting targetPlayer;
    private final ButtonSetting targetEntity;
    private final SliderSetting maxRotationChange;
    private final SliderSetting moveFixRandomization;
    private final SliderSetting humanVar;
    private final SliderSetting patternVar;

    private EntityLivingBase target;
    private float[] rotations = new float[2];
    private float[] serverRotations = new float[2];
    private float[] lastRotations = new float[2];
    private boolean shouldShoot;
    private int rotationTime;
    private float lastServerYaw;
    private long lastUpdateTime;
    private long lastMoveFixTime;
    private int moveFixCooldown;
    private float[] rotationPattern = new float[3];
    private int patternIndex;

    public BowAimbot() {
        super("BowAimbot", category.combat);

        this.registerSetting(range = new SliderSetting("Range", 50.0, 20.0, 100.0, 1.0));
        this.registerSetting(fov = new SliderSetting("FOV", 360.0, 30.0, 360.0, 4.0));
        this.registerSetting(prediction = new SliderSetting("Prediction", 0.5, 0.0, 2.0, 0.1));
        this.registerSetting(rotationMode = new ModeSetting("Rotation", new String[]{"Silent", "Lock view"}, 0));
        this.registerSetting(rotationSpeed = new SliderSetting("Rotation speed", 80, 10, 180, 1, () -> rotationMode.getInput() == 0));
        this.registerSetting(maxRotationChange = new SliderSetting("Max rotation change", 90, 15, 180, 1, () -> rotationMode.getInput() == 0));
        this.registerSetting(randomization = new SliderSetting("Randomization", 3, 0, 10, 1, () -> rotationMode.getInput() == 0));
        this.registerSetting(moveFixMode = new ModeSetting("Move fix", new String[]{"None", "Strict", "Advanced"}, 1, () -> rotationMode.getInput() == 0));
        this.registerSetting(moveFixRandomization = new SliderSetting("Move fix random", 3, 0, 10, 1, () -> rotationMode.getInput() == 0 && moveFixMode.getInput() != 0));
        this.registerSetting(humanVar = new SliderSetting("Human var", 30, 0, 100, 1, () -> rotationMode.getInput() == 0));
        this.registerSetting(patternVar = new SliderSetting("Pattern var", 25, 0, 100, 1, () -> rotationMode.getInput() == 0));
        this.registerSetting(autoShoot = new ButtonSetting("Auto shoot", true));
        this.registerSetting(requireMouseDown = new ButtonSetting("Require mouse down", false));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(targetInvisible = new ButtonSetting("Target invisible", true));
        this.registerSetting(targetPlayer = new ButtonSetting("Target player", true));
        this.registerSetting(targetEntity = new ButtonSetting("Target entity", false));
    }

    @Override
    public void onEnable() {
        target = null;
        rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        serverRotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        lastRotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        shouldShoot = false;
        lastServerYaw = mc.thePlayer.rotationYaw;
        rotationTime = 0;
        lastUpdateTime = System.currentTimeMillis();
        lastMoveFixTime = System.currentTimeMillis();
        moveFixCooldown = 0;
        patternIndex = 0;
        generateRotationPattern();
    }

    private void generateRotationPattern() {
        for (int i = 0; i < rotationPattern.length; i++) {
            rotationPattern[i] = (float) (ThreadLocalRandom.current().nextDouble() * 2 - 1);
        }
    }

    @EventListener
    public void onPreUpdate(PreUpdateEvent e) {
        if (!isBowDrawn() || !shouldAim()) {
            target = null;
            shouldShoot = false;
            rotationTime = 0;
            return;
        }

        findTarget();
        rotationTime++;

        if (target == null) {
            shouldShoot = false;
            return;
        }

        calculateRotations();
        float[] targetRotations = rotations.clone();

        if (rotationMode.getInput() == 0 && randomization.getInput() > 0) {
            float randAmount = (float) randomization.getInput();
            targetRotations[0] += (ThreadLocalRandom.current().nextFloat() - 0.5f) * randAmount;
            targetRotations[1] += (ThreadLocalRandom.current().nextFloat() - 0.5f) * randAmount;
        }

        if (rotationMode.getInput() == 0) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = Math.min((currentTime - lastUpdateTime) / 50f, 2.0f);
            lastUpdateTime = currentTime;

            float humanVarValue = (float) humanVar.getInput() / 100f;
            if (humanVarValue > 0) {
                targetRotations[0] += rotationPattern[patternIndex] * humanVarValue * 10;
                patternIndex = (patternIndex + 1) % rotationPattern.length;
                if (patternIndex == 0 && ThreadLocalRandom.current().nextInt(100) < 30) {
                    generateRotationPattern();
                }
            }

            float patternVarValue = (float) patternVar.getInput() / 100f;
            if (patternVarValue > 0) {
                float patternOffset = (float) Math.sin(rotationTime * 0.1f) * patternVarValue * 5;
                targetRotations[0] += patternOffset;
            }

            float yawDiff = MathHelper.wrapAngleTo180_float(targetRotations[0] - serverRotations[0]);
            float pitchDiff = targetRotations[1] - serverRotations[1];

            float maxChange = (float) maxRotationChange.getInput();
            yawDiff = MathHelper.clamp_float(yawDiff, -maxChange, maxChange);
            pitchDiff = MathHelper.clamp_float(pitchDiff, -maxChange, maxChange);

            float speedFactor = (float) rotationSpeed.getInput() / 180f;
            float interpolation = speedFactor * deltaTime;

            interpolation = (float) Math.sin(interpolation * Math.PI / 2);

            serverRotations[0] += yawDiff * interpolation;
            serverRotations[1] += pitchDiff * interpolation;

            serverRotations[1] = MathHelper.clamp_float(serverRotations[1], -90, 90);

            lastRotations = serverRotations.clone();
        } else {
            serverRotations = targetRotations;
        }

        if (rotationMode.getInput() == 1) {
            mc.thePlayer.rotationYaw = serverRotations[0];
            mc.thePlayer.rotationPitch = serverRotations[1];
        }

        lastServerYaw = serverRotations[0];
        shouldShoot = autoShoot.isToggled() && isFullyCharged();
    }

    @EventListener
    public void onPostMotion(PostMotionEvent e) {
        if (shouldShoot && target != null && isBowDrawn()) {
            releaseBow();
            shouldShoot = false;
        }
    }

    @EventListener(priority = -1)
    public void onRotation(RotationEvent e) {
        if (target == null || rotationMode.getInput() != 0 || !isBowDrawn() || !shouldAim())
            return;

        e.setYaw(serverRotations[0]);
        e.setPitch(serverRotations[1]);
    }

    @EventListener
    public void onMoveInput(MoveInputEvent e) {
        if (rotationMode.getInput() == 0 && target != null && isBowDrawn()) {
            switch ((int) moveFixMode.getInput()) {
                case 1:
                    fixMovementStrict(e);
                    break;
                case 2:
                    fixMovementAdvanced(e);
                    break;
            }
        }
    }

    private void fixMovementStrict(MoveInputEvent e) {
        if (ThreadLocalRandom.current().nextFloat() > 0.8f) return;

        float strafe = e.getStrafe();
        float forward = e.getForward();

        if (strafe == 0 && forward == 0) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveFixTime < moveFixCooldown) return;

        moveFixCooldown = ThreadLocalRandom.current().nextInt(50, 200);
        lastMoveFixTime = currentTime;

        float moveAngle = (float) Math.toDegrees(Math.atan2(forward, strafe)) - 90;
        float serverDiff = MathHelper.wrapAngleTo180_float(lastRotations[0] - moveAngle);

        float minThreshold = 30 + ThreadLocalRandom.current().nextFloat() * 30;
        float maxThreshold = 120 + ThreadLocalRandom.current().nextFloat() * 30;

        if (Math.abs(serverDiff) > minThreshold && Math.abs(serverDiff) < maxThreshold) {
            float randIntensity = (float) moveFixRandomization.getInput() / 10f;
            float randFactor = 1.0f + (ThreadLocalRandom.current().nextFloat() - 0.5f) * randIntensity;
            float correction = serverDiff * randFactor;

            double rads = Math.toRadians(correction * 0.7f);
            float cos = MathHelper.cos((float) rads);
            float sin = MathHelper.sin((float) rads);

            float magnitude = (float) Math.sqrt(strafe * strafe + forward * forward);
            float newStrafe = strafe * cos - forward * sin;
            float newForward = forward * cos + strafe * sin;
            float newMagnitude = (float) Math.sqrt(newStrafe * newStrafe + newForward * newForward);

            if (newMagnitude > 0) {
                float scale = magnitude / newMagnitude;
                e.setStrafe(newStrafe * scale);
                e.setForward(newForward * scale);
            }
        }
    }

    private void fixMovementAdvanced(MoveInputEvent e) {
        if (ThreadLocalRandom.current().nextFloat() > 0.75f) return;

        float strafe = e.getStrafe();
        float forward = e.getForward();

        if (strafe == 0 && forward == 0) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveFixTime < moveFixCooldown) return;
        moveFixCooldown = ThreadLocalRandom.current().nextInt(50, 200);
        lastMoveFixTime = currentTime;

        float movementYaw = (float) Math.toDegrees(Math.atan2(forward, strafe)) - 90f;
        float velocityYaw = getMovementDirection();

        double speed = Math.sqrt(
                mc.thePlayer.motionX * mc.thePlayer.motionX +
                        mc.thePlayer.motionZ * mc.thePlayer.motionZ
        );

        float velocityFactor = (float) Math.min(speed * 0.5f, 0.7f);
        float targetYaw = MathHelper.wrapAngleTo180_float(
                movementYaw * (1 - velocityFactor) + velocityYaw * velocityFactor
        );

        float diff = MathHelper.wrapAngleTo180_float(lastRotations[0] - targetYaw);

        float minThreshold = 30 + ThreadLocalRandom.current().nextFloat() * 30;
        float maxThreshold = 120 + ThreadLocalRandom.current().nextFloat() * 30;

        if (Math.abs(diff) > minThreshold && Math.abs(diff) < maxThreshold) {
            float randIntensity = (float) moveFixRandomization.getInput() / 15f;
            float randomFactor = 1.0f + (ThreadLocalRandom.current().nextFloat() - 0.5f) * randIntensity;
            float adjustedDiff = diff * randomFactor * 0.7f;

            float scaleFactor = MathHelper.clamp_float(1.0f - Math.abs(adjustedDiff) / 180f, 0.7f, 1.0f);
            scaleFactor *= (0.9f + ThreadLocalRandom.current().nextFloat() * 0.2f);

            strafe *= scaleFactor;
            forward *= scaleFactor;

            double angle = Math.toRadians(lastRotations[0] + adjustedDiff);
            float sin = MathHelper.sin((float) angle);
            float cos = MathHelper.cos((float) angle);

            e.setStrafe(strafe * cos - forward * sin);
            e.setForward(forward * cos + strafe * sin);
        }
    }

    private boolean isBowDrawn() {
        if (mc.thePlayer == null || mc.thePlayer.getHeldItem() == null)
            return false;

        ItemStack heldItem = mc.thePlayer.getHeldItem();
        return heldItem.getItem() instanceof ItemBow && mc.thePlayer.isUsingItem();
    }

    private boolean shouldAim() {
        return (!requireMouseDown.isToggled() || Mouse.isButtonDown(1)) &&
                !(ModuleManager.scaffold != null && ModuleManager.scaffold.isEnabled());
    }

    private boolean isFullyCharged() {
        int useDuration = mc.thePlayer.getItemInUseDuration();
        float charge = useDuration / 20.0F;
        charge = (charge * charge + charge * 2.0F) / 3.0F;
        return charge >= 1.0F;
    }

    private void findTarget() {
        List<EntityLivingBase> targets = new ArrayList<>();
        Vec3 eyePos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.thePlayer)
                continue;

            EntityLivingBase living = (EntityLivingBase) entity;

            if (living instanceof EntityPlayer) {
                if (!targetPlayer.isToggled()) continue;
                if (Utils.isFriended((EntityPlayer) living)) continue;
                if (ignoreTeammates.isToggled() && Utils.isTeamMate(living)) continue;
            } else if (!targetEntity.isToggled()) {
                continue;
            }

            if (!targetInvisible.isToggled() && living.isInvisible())
                continue;

            if (mc.thePlayer.getDistanceToEntity(living) > range.getInput())
                continue;

            if (fov.getInput() != 360 && !Utils.inFov((float) fov.getInput(), living))
                continue;

            targets.add(living);
        }

        double predictAmount = prediction.getInput();
        targets.sort(Comparator.comparingDouble(entity -> {
            Vec3 toPlayer = new Vec3(
                    mc.thePlayer.posX - entity.posX,
                    0,
                    mc.thePlayer.posZ - entity.posZ
            ).normalize();

            Vec3 entityVel = new Vec3(entity.motionX, 0, entity.motionZ).normalize();
            double angleFactor = 1.0 + Math.abs(toPlayer.dotProduct(entityVel));

            Vec3 predictedPos = new Vec3(
                    entity.posX + entity.motionX * predictAmount,
                    entity.posY + entity.motionY * predictAmount,
                    entity.posZ + entity.motionZ * predictAmount
            );
            double predictedDist = mc.thePlayer.getDistanceToEntity(entity);

            return predictedDist * angleFactor;
        }));

        target = targets.isEmpty() ? null : targets.get(0);
    }

    private float getMovementDirection() {
        if (mc.thePlayer.moveForward == 0 && mc.thePlayer.moveStrafing == 0) {
            return mc.thePlayer.rotationYaw;
        }

        float rotationYaw = mc.thePlayer.rotationYaw;

        if (mc.thePlayer.moveForward < 0) {
            rotationYaw += 180;
        }

        float forward = 1;
        if (mc.thePlayer.moveForward < 0) {
            forward = -0.5F;
        } else if (mc.thePlayer.moveForward > 0) {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0) {
            rotationYaw -= 70 * forward;
        }

        if (mc.thePlayer.moveStrafing < 0) {
            rotationYaw += 70 * forward;
        }

        return rotationYaw;
    }

    private void calculateRotations() {
        if (target == null) return;

        double predictAmount = prediction.getInput();
        Vec3 predictedPos = new Vec3(
                target.posX + (target.posX - target.prevPosX) * predictAmount,
                target.posY + (target.posY - target.prevPosY) * predictAmount + target.getEyeHeight() - 0.15,
                target.posZ + (target.posZ - target.prevPosZ) * predictAmount
        );

        predictedPos = predictedPos.addVector(
                0,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.25,
                0
        );

        Vec3 eyePos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        double diffX = predictedPos.xCoord - eyePos.xCoord;
        double diffZ = predictedPos.zCoord - eyePos.zCoord;
        double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);

        int useDuration = mc.thePlayer.getItemInUseDuration();
        float charge = useDuration / 20.0F;
        charge = (charge * charge + charge * 2.0F) / 3.0F;
        if (charge > 1.0F) charge = 1.0F;

        double velocity = charge * 3.0;
        double gravity = 0.05;

        double discriminant = Math.pow(velocity, 4) - gravity * (gravity * Math.pow(distance, 2)
                + 2 * (predictedPos.yCoord - eyePos.yCoord) * Math.pow(velocity, 2));

        double pitch;
        if (discriminant < 0) {
            pitch = -Math.toDegrees(Math.atan2(predictedPos.yCoord - eyePos.yCoord, distance));
        } else {
            double numerator = velocity * velocity - Math.sqrt(discriminant);
            double denominator = gravity * distance;
            pitch = -Math.toDegrees(Math.atan(numerator / denominator));
        }

        double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;

        rotations[0] = (float) yaw;
        rotations[1] = (float) MathHelper.clamp_double(pitch, -90, 90);
    }

    private void releaseBow() {
        if (silentSwing.isToggled()) {
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN,
                    EnumFacing.DOWN
            ));
        } else {
            mc.thePlayer.stopUsingItem();
        }
    }

    @Override
    public String getInfo() {
        return autoShoot.isToggled() ? "Auto" : "Manual";
    }
}