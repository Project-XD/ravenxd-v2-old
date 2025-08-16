package keystrokesmod.module.impl.world;

import keystrokesmod.Client;
import keystrokesmod.event.player.*;
import keystrokesmod.event.render.Render3DEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.impl.world.scaffold.IScaffoldRotation;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSchedule;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.module.impl.world.scaffold.rotation.*;
import keystrokesmod.module.impl.world.scaffold.schedule.NormalSchedule;
import keystrokesmod.module.impl.world.scaffold.schedule.SimpleTellySchedule;
import keystrokesmod.module.impl.world.scaffold.schedule.TellySchedule;
import keystrokesmod.module.impl.world.scaffold.sprint.*;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Timer;
import keystrokesmod.utility.*;
import keystrokesmod.utility.aim.AimSimulator;
import keystrokesmod.utility.aim.RotationData;
import keystrokesmod.utility.movement.Move;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import keystrokesmod.event.client.MouseEvent;
import keystrokesmod.event.render.Render2DEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Scaffold extends IAutoClicker {
    private static final String[] precisionModes = new String[]{"Very low", "Low", "Moderate", "High", "Very high", "Unlimited"};

    private final SliderSetting motion;
    private final ButtonSetting safeWalk;
    private final ButtonSetting safeWalkOnNoBlocks;
    public final ButtonSetting tower;
    private final ButtonSetting sameY;
    private final ButtonSetting autoJump;
    private final ModeValue clickMode;
    private final ButtonSetting alwaysPlaceIfPossible;
    private final SliderSetting minRotationSpeed;
    private final SliderSetting maxRotationSpeed;
    private final SliderSetting minRotationAccuracy;
    private final SliderSetting maxRotationAccuracy;
    public final ModeValue schedule;
    private final ModeValue rotation;
    private final ButtonSetting moveFix;
    private final SliderSetting strafe;
    private final ButtonSetting notWhileDiagonal;
    private final ButtonSetting notWhileTower;
    private final ModeValue sprint;
    private final ButtonSetting cancelSprint;
    private final ButtonSetting rayCast;
    private final ButtonSetting recycleRotation;
    private final ButtonSetting hover;
    private final ButtonSetting sneak;
    private final SliderSetting sneakEveryBlocks;
    private final ButtonSetting jump;
    private final SliderSetting jumpEveryBlocks;
    private final SliderSetting sneakTime;
    private final ButtonSetting rotateWithMovement;
    private final ButtonSetting staticYaw;
    private final ButtonSetting reserveYaw;
    private final ButtonSetting staticPitch;
    private final ButtonSetting staticPitchOnJump;
    private final SliderSetting straightPitch;
    private final SliderSetting diagonalPitch;
    private final ModeSetting precision;
    private final ButtonSetting autoSwap;
    private final ButtonSetting useBiggestStack;
    private final ButtonSetting showBlockCount;
    private final ButtonSetting delayOnJump;
    private final SliderSetting delayOnJumpAmount;
    private final ButtonSetting stopAtStart;
    private final ButtonSetting silentSwing;
    private final ButtonSetting noSwing;
    private final ButtonSetting expand;
    private final SliderSetting expandDistance;
    private final ButtonSetting lookView;
    private final ButtonSetting esp;
    private final ModeSetting theme;
    private final ButtonSetting raytrace;
    private final SliderSetting alpha;
    private final ButtonSetting outline;

    private final Map<BlockPos, Timer> highlight = new HashMap<>();
    public @Nullable MovingObjectPosition rayCasted = null;
    public MovingObjectPosition placeBlock;
    public float placeYaw;
    public float placePitch = 85;
    public int at;
    public int index;
    public boolean rmbDown;
    public int delayTicks = 0;
    public boolean place;
    public int offGroundTicks = 0;
    public int onGroundTicks = 0;
    public boolean telly$noBlockPlace = false;
    private int lastSlot;
    public double startPos = -1;
    private boolean forceStrict;
    private boolean down;
    private int add = 0;
    private int sneak$bridged = 0;
    private int jump$bridged = 0;
    private boolean placedUp;
    public float lastYaw = 0;
    public float lastPitch = 85;
    private HoverState hoverState = HoverState.DONE;
    private boolean stopMoving = false;
    private double lastOffsetToMid = -1;
    private MovingObjectPosition lastESPRaytrace = null;
    private final ButtonSetting gradientESP;
    private final SliderSetting fadeDistance;
    private final SliderSetting outlineThickness;

    public Scaffold() {
        super("Scaffold", category.world);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new LiteralSubMode("Basic", this))
                .add(new NormalAutoClicker("Normal", this, false, true))
                .setDefaultValue("Basic")
        );
        this.registerSetting(alwaysPlaceIfPossible = new ButtonSetting("Always place if possible", false));
        this.registerSetting(schedule = new ModeValue("Schedule", this)
                .add(new NormalSchedule("Normal", this))
                .add(new TellySchedule("Telly", this))
                .add(new SimpleTellySchedule("Simple telly", this))
                .setDefaultValue("Normal")
        );
        this.registerSetting(rotation = new ModeValue("Rotation", this)
                .add(new NoneRotation("None", this))
                .add(new BackwardsRotation("Backwards", this))
                .add(new StrictRotation("Strict", this))
                .add(new PreciseRotation("Precise", this))
                .add(new ConstantRotation("Constant", this))
                .add(new HypixelRotation("Hypixel", this))
                .setDefaultValue("Backwards")
        );
        ModeOnly doRotation = new ModeOnly(rotation, 0).reserve();
        this.registerSetting(minRotationSpeed = new SliderSetting("Min rotation speed", 180, 0, 180, 1, doRotation));
        this.registerSetting(maxRotationSpeed = new SliderSetting("Max rotation speed", 180, 0, 180, 1, doRotation));
        this.registerSetting(minRotationAccuracy = new SliderSetting("Min rotation accuracy", 180, 0, 180, 1, doRotation));
        this.registerSetting(maxRotationAccuracy = new SliderSetting("Max rotation accuracy", 180, 0, 180, 1, doRotation));
        this.registerSetting(moveFix = new ButtonSetting("MoveFix", false, doRotation));
        this.registerSetting(motion = new SliderSetting("Motion", 1.0, 0.5, 1.2, 0.01, () -> !moveFix.isToggled()));
        this.registerSetting(strafe = new SliderSetting("Strafe", 0, 0, 90, 5));
        this.registerSetting(notWhileDiagonal = new ButtonSetting("Not while diagonal", true, () -> strafe.getInput() != 0));
        this.registerSetting(notWhileTower = new ButtonSetting("Not while tower", false, () -> strafe.getInput() != 0));
        this.registerSetting(sprint = new ModeValue("Sprint", this)
                .add(new DisabledSprint("Disabled", this))
                .add(new VanillaSprint("Vanilla", this))
                .add(new EdgeSprint("Edge", this))
                .add(new JumpSprint("JumpA", this))
                .add(new JumpSprint("JumpB", this))
                .add(new JumpSprint("JumpC", this))
                .add(new HypixelJumpSprint("HypixelJump", this))
                .add(new HypixelJump2Sprint("HypixelJump2", this))
                .add(new HypixelSprint("Hypixel", this))
                .add(new LegitSprint("Legit", this))
                .add(new SneakSprint("Sneak", this))
                .add(new OldIntaveSprint("OldIntave", this))
        );
        this.registerSetting(precision = new ModeSetting("Precision", precisionModes, 4));
        this.registerSetting(cancelSprint = new ButtonSetting("Cancel sprint", false, new ModeOnly(sprint, 0).reserve()));
        this.registerSetting(rayCast = new ButtonSetting("Ray cast", false));
        this.registerSetting(hover = new ButtonSetting("Hover", false));
        this.registerSetting(recycleRotation = new ButtonSetting("Recycle rotation", false));
        this.registerSetting(sneak = new ButtonSetting("Sneak", false));
        this.registerSetting(sneakEveryBlocks = new SliderSetting("Sneak every blocks", 0, 1, 10, 1, sneak::isToggled));
        this.registerSetting(sneakTime = new SliderSetting("Sneak time", 50, 0, 500, 10, "ms", sneak::isToggled));
        this.registerSetting(jump = new ButtonSetting("Jump", false));
        this.registerSetting(jumpEveryBlocks = new SliderSetting("Jump every blocks", 0, 1, 10, 1, jump::isToggled));
        this.registerSetting(rotateWithMovement = new ButtonSetting("Rotate with movement", true));
        this.registerSetting(staticYaw = new ButtonSetting("Static yaw", false));
        this.registerSetting(reserveYaw = new ButtonSetting("Reserve yaw", false));
        this.registerSetting(staticPitch = new ButtonSetting("Static pitch", false));
        this.registerSetting(staticPitchOnJump = new ButtonSetting("Static pitch on jump", false, staticPitch::isToggled));
        this.registerSetting(straightPitch = new SliderSetting("Straight pitch", 75.7, 45, 90, 0.1, staticPitch::isToggled));
        this.registerSetting(diagonalPitch = new SliderSetting("Diagonal pitch", 75.6, 45, 90, 0.1, staticPitch::isToggled));
        this.registerSetting(autoSwap = new ButtonSetting("AutoSwap", true));
        this.registerSetting(useBiggestStack = new ButtonSetting("Use biggest stack", true, autoSwap::isToggled));
        this.registerSetting(delayOnJump = new ButtonSetting("Delay on jump", false));
        this.registerSetting(delayOnJumpAmount = new SliderSetting("Delay on jump amount", 0, 0, 4, 1, delayOnJump::isToggled));
        this.registerSetting(safeWalk = new ButtonSetting("Safewalk", true));
        this.registerSetting(safeWalkOnNoBlocks = new ButtonSetting("Safewalk on no blocks", true));
        this.registerSetting(showBlockCount = new ButtonSetting("Show block count", true));
        this.registerSetting(stopAtStart = new ButtonSetting("Stop at start", false));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
        this.registerSetting(noSwing = new ButtonSetting("No swing", false, silentSwing::isToggled));
        this.registerSetting(tower = new ButtonSetting("Tower", false));
        this.registerSetting(sameY = new ButtonSetting("SameY", false));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", false));
        this.registerSetting(expand = new ButtonSetting("Expand", false));
        this.registerSetting(expandDistance = new SliderSetting("Expand distance", 4.5, 0, 10, 0.1, expand::isToggled));
        this.registerSetting(lookView = new ButtonSetting("Look view", false));
        this.registerSetting(new DescriptionSetting("Rendering"));
        this.registerSetting(new DescriptionSetting("Visual Enhancements"));
        this.registerSetting(esp = new ButtonSetting("ESP", false));
        this.registerSetting(theme = new ModeSetting("Theme", Theme.themes, 0));
        this.registerSetting(raytrace = new ButtonSetting("Raytrace", false, esp::isToggled));
        this.registerSetting(alpha = new SliderSetting("Alpha", 200, 0, 255, 1, () -> esp.isToggled() && raytrace.isToggled()));
        this.registerSetting(outline = new ButtonSetting("Outline", true, esp::isToggled));
        this.registerSetting(gradientESP = new ButtonSetting("Gradient ESP", true, esp::isToggled));
        this.registerSetting(fadeDistance = new SliderSetting("Fade distance", 5, 1, 10, 0.5, "blocks", esp::isToggled));
        this.registerSetting(outlineThickness = new SliderSetting("Outline thickness", 1.5, 0.5, 3, 0.1, esp::isToggled));
        this.registerSetting(new ButtonSetting("Show background", true));
    }

    @Override
    public void guiUpdate() throws Throwable {
        Utils.correctValue(minRotationSpeed, maxRotationSpeed);
        Utils.correctValue(minRotationAccuracy, maxRotationAccuracy);
    }

    public static boolean sprint() {
        if (ModuleManager.scaffold.isEnabled()
                && ModuleManager.scaffold.sprint.getInput() != 0) {
            return ((IScaffoldSprint) ModuleManager.scaffold.sprint.getSelected()).isSprint();
        }
        return false;
    }

    @EventListener
    public void onSprint(SprintEvent event) {
        if (!sprint()) {
            event.setSprint(false);
        }
    }

    public static int getSlot() {
        int slot = -1;
        int highestStack = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && ContainerUtils.canBePlaced((ItemBlock) itemStack.getItem()) && itemStack.stackSize > 0) {
                if (mc.thePlayer.inventory.mainInventory[i].stackSize > highestStack) {
                    highestStack = mc.thePlayer.inventory.mainInventory[i].stackSize;
                    slot = i;
                }
            }
        }
        return slot;
    }

    public void onDisable() {
        clickMode.disable();
        schedule.disable();
        rotation.disable();
        sprint.disable();

        placeBlock = null;
        if (lastSlot != -1) {
            SlotHandler.setCurrentSlot(lastSlot);
            lastSlot = -1;
        }
        delayTicks = 0;
        highlight.clear();
        at = index = 0;
        add = 0;
        startPos = -1;
        forceStrict = false;
        down = false;
        place = false;
        placedUp = false;
        offGroundTicks = 0;
        telly$noBlockPlace = false;
        lastOffsetToMid = -1;
        lastESPRaytrace = null;
        Utils.resetTimer();
    }

    public void onEnable() {
        clickMode.enable();
        schedule.enable();
        rotation.enable();
        sprint.enable();

        lastSlot = -1;
        startPos = mc.thePlayer.posY;
        sneak$bridged = 0;
        jump$bridged = 0;
        lastYaw = RotationHandler.getRotationYaw();
        lastPitch = RotationHandler.getRotationPitch();

        if (hover.isToggled() && mc.thePlayer.onGround) {
            hoverState = HoverState.JUMP;
        } else {
            hoverState = HoverState.DONE;
        }

        if (stopAtStart.isToggled()) {
            stopMoving = true;
        }

    }

    @EventListener
    public void onRotation(RotationEvent event) {
        if (!Utils.nullCheck()) {
            return;
        }

        final RotationData data = ((IScaffoldRotation) rotation.getSelected()).onRotation(placeYaw, placePitch, forceStrict, event);
        float yaw;
        float pitch;
        if (((IScaffoldSchedule) schedule.getSelected()).noRotation()) {
            yaw = event.getYaw();
            pitch = event.getPitch();
        } else {
            yaw = data.getYaw();
            pitch = data.getPitch();

            if (strafe.getInput() != 0)
                yaw = applyStrafe(yaw, (float) strafe.getInput());

            if (staticYaw.isToggled()) {
                float delta = yaw % 45;
                if (delta > 22.5 && delta <= 45)
                    yaw += 45 - delta;
                else if (delta < -22.5 && delta >= -45)
                    yaw -= 45 + delta;
                else if (delta <= 22.5 && delta > 0)
                    yaw -= delta;
                else if (delta >= -22.5 && delta < 0)
                    yaw -= delta;
            }

            if (reserveYaw.isToggled())
                yaw += 180;

            if (staticPitch.isToggled() && (staticPitchOnJump.isToggled() || mc.thePlayer.onGround)) {
                double direction = MoveUtil.direction();
                double movingYaw = Math.round(direction / 45) * 45;
                boolean isMovingStraight = movingYaw % 90 == 0f;

                if (isMovingStraight) {
                    pitch = (float) straightPitch.getInput();
                } else {
                    pitch = (float) diagonalPitch.getInput();
                }
            }
        }

        final RotationData result = ((IScaffoldSprint) sprint.getSelected()).onFinalRotation(new RotationData(yaw, pitch));

        float rotationSpeed = (float) Utils.randomizeDouble(minRotationSpeed.getInput(), maxRotationSpeed.getInput());
        double rotationAccuracy = Utils.randomizeDouble(minRotationAccuracy.getInput(), maxRotationAccuracy.getInput());

        lastYaw = AimSimulator.rotMove(result.getYaw(), lastYaw,
                rotationSpeed, AimSimulator.getGCD(), rotationAccuracy);
        lastPitch = AimSimulator.rotMove(result.getPitch(), lastPitch,
                rotationSpeed, AimSimulator.getGCD(), rotationAccuracy);

        event.setYaw(lastYaw);
        event.setPitch(lastPitch);
        event.setMoveFix(moveFix.isToggled() ? RotationHandler.MoveFix.Silent : RotationHandler.MoveFix.None);

        if (lookView.isToggled()) {
            mc.thePlayer.rotationYaw = event.getYaw();
            mc.thePlayer.rotationPitch = event.getPitch();
        }
    }

    public float applyStrafe(float yaw, float strafeVal) {
        if ((!isDiagonal() || !notWhileDiagonal.isToggled()) && (!ModuleManager.tower.canTower() || !notWhileTower.isToggled())) {
            if (isDiagonal()) {
                yaw += strafeVal;
            } else {
                double offsetToMid = EnumFacing.fromAngle(yaw).getAxis() == EnumFacing.Axis.X ? Math.abs(mc.thePlayer.posZ % 1) : Math.abs(mc.thePlayer.posX % 1);
                if (offsetToMid > 0.6 || offsetToMid < 0.4 || lastOffsetToMid == -1) {
                    lastOffsetToMid = offsetToMid;
                }
                yaw += lastOffsetToMid >= 0.5 ? strafeVal : -strafeVal;
            }
        }
        return yaw;
    }

    @Override
    public boolean click() {
        place = true;
        return true;
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (cancelSprint.isToggled()) {
            event.setSprinting(false);
        }
    }

    @EventListener
    public void onJump(JumpEvent e) {
        if (delayOnJump.isToggled())
            delayTicks = (int) delayOnJumpAmount.getInput();
    }

    @EventListener
    public void onMoveInput(@NotNull MoveInputEvent event) {
        if (stopMoving) {
            event.cancel();
            stopMoving = false;
        }
    }

    @EventListener
    public void onPreUpdate(PreUpdateEvent event) {
        // place here
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
            onGroundTicks++;
        } else {
            offGroundTicks++;
            onGroundTicks = 0;
        }

        switch (hoverState) {
            case JUMP:
                if (mc.thePlayer.onGround) {
                    MoveUtil.jump();
                }
                hoverState = HoverState.FALL;
                break;
            case FALL:
                if (mc.thePlayer.onGround)
                    hoverState = HoverState.DONE;
                break;
        }

        if ((rotation.getInput() != 5 && autoJump.isToggled()) && mc.thePlayer.onGround && MoveUtil.isMoving()) {
            MoveUtil.jump();
        }

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        if (lastSlot == -1) {
            lastSlot = SlotHandler.getCurrentSlot();
        }
        int slot = SlotHandler.getCurrentSlot();
        if (autoSwap.isToggled()) {
            if (useBiggestStack.isToggled()) {
                slot = getSlot();
            } else if (SlotHandler.getHeldItem() == null
                    || !(SlotHandler.getHeldItem().getItem() instanceof ItemBlock)
                    || !ContainerUtils.canBePlaced((ItemBlock) SlotHandler.getHeldItem().getItem())) {
                slot = getSlot();
            }
        }
        SlotHandler.setCurrentSlot(slot);

        final ItemStack heldItem = SlotHandler.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock) || !ContainerUtils.canBePlaced((ItemBlock) heldItem.getItem()))
            return;

        if (keepYPosition() && !down) {
            startPos = Math.floor(mc.thePlayer.posY);
            down = true;
        } else if (!keepYPosition()) {
            down = false;
            placedUp = false;
        }
        if (keepYPosition() && (sprint.getInput() == 3 || sprint.getInput() == 4 || sprint.getInput() == 5 || sprint.getInput() == 6 || sprint.getInput() == 12)) {
            if (mc.thePlayer.onGround) {
                add = 0;
                if (Math.floor(mc.thePlayer.posY) == Math.floor(startPos) && sprint.getInput() == 5) {
                    placedUp = false;
                }
            }
        }

        double original = startPos;
        if (sprint.getInput() == 3) {
            if (groundDistance() >= 2 && add == 0) {
                original++;
                add++;
            }
        } else if (sprint.getInput() == 4 || sprint.getInput() == 5 || sprint.getInput() == 6) {
            if (groundDistance() > 0 && mc.thePlayer.posY >= Math.floor(mc.thePlayer.posY) && mc.thePlayer.fallDistance > 0 && ((!placedUp || isDiagonal()) || sprint.getInput() == 4 || sprint.getInput() == 6)) {
                original++;
            }
        }

        Vec3 targetVec3 = getPlacePossibility(0, original);
        if (targetVec3 == null) {
            return;
        }
        BlockPos targetPos = new BlockPos(targetVec3.xCoord, targetVec3.yCoord, targetVec3.zCoord);

        if (mc.thePlayer.onGround && Utils.isMoving() && motion.getInput() != 1.0 && !moveFix.isToggled()) {
            MoveUtil.strafe(MoveUtil.speed() * motion.getInput());
        }

        rayCasted = null;
        float searchYaw = 25;
        switch ((int) precision.getInput()) {
            case 0:
                searchYaw = 35;
                break;
            case 1:
                searchYaw = 30;
                break;
            case 2:
                break;
            case 3:
                searchYaw = 15;
                break;
            case 4:
                searchYaw = 5;
                break;
            case 5:
                searchYaw = 360;
                break;
        }

        EnumFacingOffset enumFacing = getEnumFacing(targetVec3);
        if (enumFacing == null) {
            return;
        }
        targetPos = targetPos.add(enumFacing.getOffset().xCoord, enumFacing.getOffset().yCoord, enumFacing.getOffset().zCoord);
        float[] targetRotation = RotationUtils.getRotations(targetPos);
        float[] searchPitch = new float[]{78, 12};

        for (int i = 0; i < 2; i++) {
            if (i == 1 && Utils.overPlaceable(-1)) {
                searchYaw = 180;
                searchPitch = new float[]{65, 25};
            } else if (i == 1) {
                if (expand.isToggled() && !(tower.isToggled() && Utils.jumpDown())) {
                    final keystrokesmod.script.classes.Vec3 eyePos = Utils.getEyePos();
                    final BlockPos groundPos = new BlockPos(mc.thePlayer).down();
                    long expDist = Math.round(expandDistance.getInput());
                    for (double j = 0; j < expDist; j += 0.05) {
                        targetPos = RotationUtils.getExtendedPos(groundPos, mc.thePlayer.rotationYaw, j);

                        if (sameY.isToggled() || hoverState != HoverState.DONE) {
                            targetPos = new BlockPos(targetPos.getX(), startPos, targetPos.getZ());
                        }

                        if (!BlockUtils.replaceable(targetPos))
                            continue;

                        Optional<Triple<BlockPos, EnumFacing, keystrokesmod.script.classes.Vec3>> optional = RotationUtils.getPlaceSide(targetPos);
                        if (!optional.isPresent()) continue;

                        Triple<BlockPos, EnumFacing, keystrokesmod.script.classes.Vec3> placeSide = optional.get();

                        if (placeSide.getRight().distanceTo(eyePos) > expandDistance.getInput()) break;

                        rayCasted = new MovingObjectPosition(placeSide.getRight().toVec3(), placeSide.getMiddle(), placeSide.getLeft());
                        placeYaw = PlayerRotation.getYaw(placeSide.getRight());
                        placePitch = PlayerRotation.getPitch(placeSide.getRight());
                        break;
                    }
                }
                break;
            }
            for (float checkYaw : generateSearchSequence(searchYaw)) {
                float playerYaw = isDiagonal() ? getYaw() : targetRotation[0];
                float fixedYaw = (float) (playerYaw - checkYaw + getRandom());
                double deltaYaw = Math.abs(playerYaw - fixedYaw);
                if (i == 1 && (inBetween(75, 95, (float) deltaYaw)) || deltaYaw > 500) {
                    continue;
                }
                for (float checkPitch : generateSearchSequence(searchPitch[1])) {
                    float fixedPitch = RotationUtils.clampTo90((float) (targetRotation[1] + checkPitch + getRandom()));
                    MovingObjectPosition raycast = RotationUtils.rayTraceCustom(mc.playerController.getBlockReachDistance(), fixedYaw, fixedPitch);
                    if (raycast != null) {
                        if (raycast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                            if (raycast.getBlockPos().equals(targetPos) && raycast.sideHit == enumFacing.getEnumFacing()) {
                                if (rayCasted == null || !BlockUtils.isSamePos(raycast.getBlockPos(), rayCasted.getBlockPos())) {
                                    if (heldItem.getItem() instanceof ItemBlock && ((ItemBlock) heldItem.getItem()).canPlaceBlockOnSide(mc.theWorld, raycast.getBlockPos(), raycast.sideHit, mc.thePlayer, heldItem)) {
                                        if (rayCasted == null
                                                && raycast.getBlockPos().getY() <= mc.thePlayer.posY) {  // to fix the insane block search bug
                                            forceStrict = (forceStrict(checkYaw)) && i == 1;
                                            if (recycleRotation.isToggled()) {
                                                Optional<Triple<BlockPos, EnumFacing, keystrokesmod.script.classes.Vec3>> placeSide = RotationUtils.getPlaceSide(raycast.getBlockPos().offset(raycast.sideHit));
                                                if (placeSide.isPresent()) {
                                                    rayCasted = new MovingObjectPosition(placeSide.get().getRight().toVec3(), placeSide.get().getMiddle(), placeSide.get().getLeft());
                                                    placeYaw = PlayerRotation.getYaw(placeSide.get().getRight());
                                                    placePitch = PlayerRotation.getPitch(placeSide.get().getRight());
                                                    break;
                                                }
                                            }
                                            rayCasted = raycast;
                                            placeYaw = fixedYaw;
                                            placePitch = fixedPitch;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (rayCasted != null) {
                break;
            }
        }

        if (((IScaffoldSchedule) schedule.getSelected()).noPlace())
            return;

        if (clickMode.getInput() == 0 || place) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);

            MovingObjectPosition hitResult = RotationUtils.rayCast(4.5, lastYaw, lastPitch);
            if (rayCasted == null && alwaysPlaceIfPossible.isToggled()) {
                if (hitResult == null) return;
                placeBlock = hitResult;
            } else {
                if (rayCasted == null) return;
                placeBlock = rayCasted;
            }
            if (rayCast.isToggled()) {
                if (hitResult == null
                        || !hitResult.getBlockPos().equals(placeBlock.getBlockPos())
                        || hitResult.sideHit != placeBlock.sideHit
                ) {
                    return;
                }
            }

            place(placeBlock, false);
            place = false;
            if (placeBlock.sideHit == EnumFacing.UP && keepYPosition()) {
                placedUp = true;
            }
        }
    }

    public Vec3 getPlacePossibility(double offsetY, double original) {
        List<Vec3> possibilities = new ArrayList<>();
        int range = 5;
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = BlockUtils.blockRelativeToPlayer(x, y, z);
                    if (!block.getMaterial().isReplaceable()) {
                        for (int x2 = -1; x2 <= 1; x2 += 2) {
                            possibilities.add(new Vec3(
                                    mc.thePlayer.posX + x + x2,
                                    mc.thePlayer.posY + y,
                                    mc.thePlayer.posZ + z
                            ));
                        }
                        for (int y2 = -1; y2 <= 1; y2 += 2) {
                            possibilities.add(new Vec3(
                                    mc.thePlayer.posX + x,
                                    mc.thePlayer.posY + y + y2,
                                    mc.thePlayer.posZ + z
                            ));
                        }
                        for (int z2 = -1; z2 <= 1; z2 += 2) {
                            possibilities.add(new Vec3(
                                    mc.thePlayer.posX + x,
                                    mc.thePlayer.posY + y,
                                    mc.thePlayer.posZ + z + z2
                            ));
                        }
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> mc.thePlayer.getDistance(
                vec3.xCoord, vec3.yCoord, vec3.zCoord) > 5
        );

        if (possibilities.isEmpty()) {
            return null;
        }

        possibilities.sort(Comparator.comparingDouble(vec3 -> {
            final double dx = (mc.thePlayer.posX) - vec3.xCoord;
            final double dy = ((keepYPosition() ? original : mc.thePlayer.posY) - 1 + offsetY) - vec3.yCoord;
            final double dz = (mc.thePlayer.posZ) - vec3.zCoord;
            return MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);
        }));

        return possibilities.get(0);
    }

    @EventListener
    public void onRenderTick(Render2DEvent ev) {
        if (!Utils.nullCheck() || !showBlockCount.isToggled() || mc.currentScreen != null) {
            return;
        }

        int blocks = totalBlocks();
        String text = blocks + " block" + (blocks == 1 ? "" : "s");

        String color;
        if (blocks <= 5) {
            color = "§c";
        } else if (blocks <= 15) {
            color = "§6";
        } else if (blocks <= 25) {
            color = "§e";
        } else {
            color = "§a";
        }

        ScaledResolution sr = new ScaledResolution(mc);
        int textWidth = mc.fontRendererObj.getStringWidth(text);

        int padding = 10;
        int iconSize = 16;
        int elementHeight = 22;
        int cornerRadius = 5;
        int totalWidth = padding * 2 + iconSize + textWidth + 6;

        int x = (sr.getScaledWidth() - totalWidth) / 2;
        int y = 20;

        int bgColor = new Color(40, 40, 50, 180).getRGB();
        RenderUtils.drawRoundedRectangle(x, y, x + totalWidth, y + elementHeight, cornerRadius, bgColor);

        if (blocks > 0) {
            int iconX = x + padding;
            int iconY = y + (elementHeight - iconSize) / 2;

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableBlend();

            mc.getTextureManager().bindTexture(new ResourceLocation("textures/blocks/wool_colored_white.png"));
            RenderUtils.drawModalRectWithCustomSizedTexture(iconX, iconY, 0, 0, iconSize, iconSize, 16, 16);
        }

        int textX = x + padding + iconSize + 6;
        int textY = y + (elementHeight - mc.fontRendererObj.FONT_HEIGHT) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(color + text, textX, textY, 0xFFFFFF);
    }

    public float[] generateSearchSequence(float value) {
        int length = (int) value * 2;
        float[] sequence = new float[length + 1];

        int index = 0;
        sequence[index++] = 0;

        for (int i = 1; i <= value; i++) {
            sequence[index++] = i;
            sequence[index++] = -i;
        }

        return sequence;
    }

    @EventListener
    public void onMouse(@NotNull MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == 1) {
            rmbDown = mouseEvent.isButtonstate();
            if (placeBlock != null && rmbDown) {
                mouseEvent.cancel();
            }
        }
    }

    public boolean stopFastPlace() {
        return this.isEnabled() && placeBlock != null;
    }

    public boolean isDiagonal() {
        float yaw = mc.thePlayer.rotationYaw;
        if (rotateWithMovement.isToggled()) {
            yaw += Move.fromMovement(mc.thePlayer.moveForward, mc.thePlayer.moveStrafing).getDeltaYaw();
        }
        yaw = RotationUtils.normalize(yaw, 0, 360);
        float delta = yaw % 90;
        return delta > 20 && delta < 70;
    }

    public double groundDistance() {
        for (int i = 1; i <= 20; i++) {
            if (!mc.thePlayer.onGround && !(BlockUtils.getBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - ((double) i / 10), mc.thePlayer.posZ)) instanceof BlockAir)) {
                return ((double) i / 10);
            }
        }
        return -1;
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (!Utils.nullCheck() || !esp.isToggled()) {
            return;
        }

        if (!highlight.isEmpty()) {
            Iterator<Map.Entry<BlockPos, Timer>> iterator = highlight.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, Timer> entry = iterator.next();
                if (entry.getValue() == null) {
                    entry.setValue(new Timer(750));
                    entry.getValue().start();
                }

                BlockPos pos = entry.getKey();
                double distance = mc.thePlayer.getDistanceSq(pos);
                double maxDistance = fadeDistance.getInput() * fadeDistance.getInput();

                if (distance > maxDistance) {
                    iterator.remove();
                    continue;
                }

                int baseAlpha = entry.getValue() == null ? 210 : 210 - entry.getValue().getValueInt(0, 210, 1);
                if (baseAlpha == 0) {
                    iterator.remove();
                    continue;
                }

                double fadeFactor = 1.0 - (distance / maxDistance);
                int alpha = (int) (baseAlpha * fadeFactor);

                if (alpha < 5) {
                    iterator.remove();
                    continue;
                }

                int color = Theme.getGradient((int) theme.getInput(), 0);
                if (gradientESP.isToggled()) {
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;

                    double centerX = pos.getX() + 0.5;
                    double centerY = pos.getY() + 0.5;
                    double centerZ = pos.getZ() + 0.5;
                    double distToCenter = Math.sqrt(
                            Math.pow(centerX - mc.thePlayer.posX, 2) +
                                    Math.pow(centerY - mc.thePlayer.posY, 2) +
                                    Math.pow(centerZ - mc.thePlayer.posZ, 2)
                    );

                    double gradientFactor = Math.min(1.0, distToCenter / 3.0);
                    r = (int) (r * (1.0 - gradientFactor * 0.5));
                    g = (int) (g * (1.0 - gradientFactor * 0.3));
                    b = (int) (b * (1.0 + gradientFactor * 0.2));

                    color = (alpha << 24) | (r << 16) | (g << 8) | b;
                } else {
                    color = Utils.merge(color, alpha);
                }

                if (!raytrace.isToggled()) {
                    renderEnhancedBlock(pos, color, outlineThickness.getInput());
                }
            }
        }

        if (raytrace.isToggled()) {
            MovingObjectPosition hitResult = mc.objectMouseOver;
            if (hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                hitResult = lastESPRaytrace;
            } else {
                lastESPRaytrace = hitResult;
            }

            if (hitResult == null) {
                hitResult = placeBlock;
            }

            if (hitResult != null && hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                int color = Theme.getGradient((int) theme.getInput(), 0);
                color = Utils.merge(color, (int) alpha.getInput());
                renderEnhancedBlock(hitResult.getBlockPos(), color, outlineThickness.getInput());
            }
        }
    }

    private void renderEnhancedBlock(BlockPos pos, int color, double thickness) {
        double x = pos.getX() - mc.getRenderManager().viewerPosX;
        double y = pos.getY() - mc.getRenderManager().viewerPosY;
        double z = pos.getZ() - mc.getRenderManager().viewerPosZ;

        AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);

        if (outline.isToggled()) {
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GL11.glLineWidth((float) thickness);

            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (color >> 24) & 0xFF;
            GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f);

            WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
            worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            Tessellator.getInstance().draw();

            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }
    }

    private boolean forceStrict(float value) {
        return (inBetween(-170, -105, value)
                || inBetween(-80, 80, value)
                || inBetween(98, 170, value))
                && !inBetween(-10, 10, value);
    }

    public boolean keepYPosition() {
        boolean sameYSca = ((IScaffoldSprint) sprint.getSelected()).isKeepY();
        return this.isEnabled() && Utils.keysDown() && (sameYSca || sameY.isToggled()) && !Utils.jumpDown()
                || hoverState != HoverState.DONE;
    }

    public boolean safewalk() {
        return this.isEnabled() && (safeWalk.isToggled() || (safeWalkOnNoBlocks.isToggled() && totalBlocks() == 0));
    }

    public boolean stopRotation() {
        return this.isEnabled() && (rotation.getInput() <= 1 || (rotation.getInput() == 2 && placeBlock != null));
    }

    private boolean inBetween(float min, float max, float value) {
        return value >= min && value <= max;
    }

    public double getRandom() {
        return Utils.randomizeInt(-90, 90) / 100.0;
    }

    public float getYaw() {
        float yaw = 180.0f;
        double moveForward = MoveUtil.getMoveForward();
        double moveStrafe = MoveUtil.getMoveStrafe();

        if (rotateWithMovement.isToggled()) {
            if (moveForward > 0.0) {
                if (moveStrafe > 0.0) {
                    yaw = 135.0f;
                } else if (moveStrafe < 0.0) {
                    yaw = -135.0f;
                }
            } else if (moveForward < 0.0) {
                if (moveStrafe > 0.0) {
                    yaw = 45.0f;
                } else if (moveStrafe < 0.0) {
                    yaw = -45.0f;
                } else {
                    yaw = 0.0f;
                }
            } else {
                if (moveStrafe > 0.0) {
                    yaw = 90.0f;
                } else if (moveStrafe < 0.0) {
                    yaw = -90.0f;
                }
            }
        }

        return mc.thePlayer.rotationYaw + yaw;
    }

    private @Nullable EnumFacingOffset getEnumFacing(final Vec3 position) {
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (!BlockUtils.getBlock(position.xCoord + x2, position.yCoord, position.zCoord).getMaterial().isReplaceable()) {
                if (x2 > 0) {
                    return new EnumFacingOffset(EnumFacing.WEST, new Vec3(x2, 0, 0));
                } else {
                    return new EnumFacingOffset(EnumFacing.EAST, new Vec3(x2, 0, 0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!BlockUtils.getBlock(position.xCoord, position.yCoord + y2, position.zCoord).getMaterial().isReplaceable()) {
                if (y2 < 0) {
                    return new EnumFacingOffset(EnumFacing.UP, new Vec3(0, y2, 0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!BlockUtils.getBlock(position.xCoord, position.yCoord, position.zCoord + z2).getMaterial().isReplaceable()) {
                if (z2 < 0) {
                    return new EnumFacingOffset(EnumFacing.SOUTH, new Vec3(0, 0, z2));
                } else {
                    return new EnumFacingOffset(EnumFacing.NORTH, new Vec3(0, 0, z2));
                }
            }
        }

        return null;
    }

    public boolean place(MovingObjectPosition block, boolean extra) {
        if (rotation.getInput() == 4 && telly$noBlockPlace) return false;

        if (sneak.isToggled()) {
            if (sneak$bridged >= sneakEveryBlocks.getInput()) {
                sneak$bridged = 0;
                ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(true);
                Client.getExecutor().schedule(() -> ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())), (long) sneakTime.getInput(), TimeUnit.MILLISECONDS);
            }
        }

        if (jump.isToggled()) {
            if (jump$bridged >= jumpEveryBlocks.getInput()) {
                jump$bridged = 0;
                if (mc.thePlayer.onGround)
                    MoveUtil.jump();
            }
        }

        ItemStack heldItem = SlotHandler.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
            return false;
        }

        ScaffoldPlaceEvent event = new ScaffoldPlaceEvent(block, extra);
        Client.EVENT_BUS.post(event);
        if (event.isCancelled()) return false;

        block = event.getHitResult();
        extra = event.isExtra();

        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, heldItem, block.getBlockPos(), block.sideHit, block.hitVec)) {
            sneak$bridged++;
            jump$bridged++;
            if (silentSwing.isToggled()) {
                if (!noSwing.isToggled())
                    PacketUtils.sendPacket(new C0APacketAnimation());
            } else {
                mc.thePlayer.swingItem();
            }
            if (!extra) {
                highlight.put(block.getBlockPos().offset(block.sideHit), null);
            }
            return true;
        }
        return false;
    }

    public int totalBlocks() {
        if (!Utils.nullCheck()) return 0;

        try {
            int totalBlocks = 0;
            for (int i = 0; i < 9; ++i) {
                final ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
                if (stack != null && stack.getItem() instanceof ItemBlock && ContainerUtils.canBePlaced((ItemBlock) stack.getItem()) && stack.stackSize > 0) {
                    totalBlocks += stack.stackSize;
                }
            }
            return totalBlocks;
        } catch (Throwable e) {
            return 0;
        }
    }

    @Override
    public String getInfo() {
        return schedule.getSelected().getPrettyName();
    }

    @EventListener
    public void onSafeWalk(@NotNull SafeWalkEvent event) {
        if (safewalk())
            event.setSafeWalk(true);
    }

    enum HoverState {
        JUMP,
        FALL,
        DONE
    }

    static class EnumFacingOffset {
        EnumFacing enumFacing;
        Vec3 offset;

        EnumFacingOffset(EnumFacing enumFacing, Vec3 offset) {
            this.enumFacing = enumFacing;
            this.offset = offset;
        }

        EnumFacing getEnumFacing() {
            return enumFacing;
        }

        Vec3 getOffset() {
            return offset;
        }
    }
}