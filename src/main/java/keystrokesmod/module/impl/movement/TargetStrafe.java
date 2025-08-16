package keystrokesmod.module.impl.movement;

import keystrokesmod.anticrack.AntiCrack;
import keystrokesmod.event.player.JumpEvent;
import keystrokesmod.event.player.PrePlayerInputEvent;
import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import keystrokesmod.eventbus.annotations.EventListener;

import static keystrokesmod.module.ModuleManager.killAura;
import static keystrokesmod.module.ModuleManager.scaffold;

public class TargetStrafe extends Module {
    private static float yaw;
    private static EntityLivingBase target = null;
    private static boolean active = false;
    private final ModeValue mode;
    private final SliderSetting range;
    private final ButtonSetting onlyWhileJumpDown;
    private final ButtonSetting strafeAround;
    private boolean left, colliding;

    public TargetStrafe() {
        super("TargetStrafe", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new LiteralSubMode("Everytime", this))
                .add(new LiteralSubMode("While speed", this))
                .add(new LiteralSubMode("While fly", this))
                .add(new LiteralSubMode("While speed or fly", this))
                .setDefaultValue("Everytime")
        );
        this.registerSetting(range = new SliderSetting("Range", 0, 0, 6, 0.1));
        this.registerSetting(onlyWhileJumpDown = new ButtonSetting("Only while jump down", false));
        this.registerSetting(strafeAround = new ButtonSetting("Strafe around", true));
    }

    public static float getMovementYaw() {
        if (active && target != null) return yaw;
        return mc.thePlayer.rotationYaw;
    }

    private boolean canTargetStrafe() {
        if (onlyWhileJumpDown.isToggled() && !Utils.jumpDown())
            return false;

        switch (mode.getSelected().getPrettyName()) {
            case "Everytime":
                return true;
            case "While speed":
                return ModuleManager.speed.isEnabled();
            case "While fly":
                return ModuleManager.fly.isEnabled();
            case "While speed or fly":
                return ModuleManager.speed.isEnabled() || ModuleManager.fly.isEnabled();
            default:
                throw new IllegalStateException("Invalid mode");
        }
    }

    @EventListener(priority = 1)
    public void onJump(JumpEvent event) {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    }

    @EventListener(priority = 1)
    public void onStrafe(PrePlayerInputEvent event) {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    }

    @EventListener(priority = 1)
    public void onPreUpdate(PreUpdateEvent event) {
        //  Disable if scaffold is enabled
        if (scaffold == null || scaffold.isEnabled() || killAura == null || !killAura.isEnabled()) {
            active = false;
            return;
        }

        active = true;

        /*
         * Getting targets and selecting the nearest one
         */
        if (!canTargetStrafe()) {
            target = null;
            return;
        }

        if (KillAura.target == null) {
            target = null;
            return;
        }

        if (mc.thePlayer.isCollidedHorizontally || !BlockUtils.isBlockUnder(5)) {
            if (!colliding) {
                if (strafeAround.isToggled())
                    MoveUtil.strafe();
                left = !left;
            }
            colliding = true;
        } else {
            colliding = false;
        }

        target = KillAura.target;

        if (target == null) {
            return;
        }

        float yaw = PlayerRotation.getYaw(new Vec3(target)) + (90 + 45) * (left ? -1 : 1);

        final double range = this.range.getInput() + Math.random() / 100f;
        final double posX = -MathHelper.sin((float) Math.toRadians(yaw)) * range + target.posX;
        final double posZ = MathHelper.cos((float) Math.toRadians(yaw)) * range + target.posZ;

        yaw = PlayerRotation.getYaw(new Vec3(posX, target.posY, posZ));

        TargetStrafe.yaw = yaw;
    }

    @Override
    public void onDisable() {
        active = false;
    }
}