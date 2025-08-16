package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.impl.render.targetvisual.targethud.*;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.interact.moveable.Moveable;
import keystrokesmod.utility.interact.moveable.MoveableManager;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import keystrokesmod.event.network.AttackEntityEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.event.render.Render2DEvent;
import org.jetbrains.annotations.Nullable;

public class TargetHUD extends Module implements Moveable {
    public static int posX = 70;
    public static int posY = 30;
    public static int minX;
    public static int maxX;
    public static int minY;
    public static int maxY;
    private static ModeValue mode;
    private static @Nullable EntityLivingBase target = null;
    private final ButtonSetting onlyKillAura;
    private long lastTargetTime = -1;

    public TargetHUD() {
        super("TargetHUD", category.render);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new RavenTargetHUD("Raven", this))
                .add(new ExhibitionTargetHUD("Exhibition", this))
                .add(new WurstTargetHUD("Wurst", this))
                .add(new TestTargetHUD("Test", this))
                .add(new RavenNewTargetHUD("RavenNew", this))
                .add(new MyauTargetHUD("Myau", this))
        );
        this.registerSetting(onlyKillAura = new ButtonSetting("Only killAura", true));
    }

    private static void render(EntityLivingBase target) {
        if (target != null) {
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            final int n2 = 8;
            final int n3 = mc.fontRendererObj.getStringWidth(target.getDisplayName().getFormattedText()) + n2;
            final int n4 = scaledResolution.getScaledWidth() / 2 - n3 / 2 + posX;
            final int n5 = scaledResolution.getScaledHeight() / 2 + 15 + posY;
            minX = n4 - n2;
            minY = n5 - n2;
            maxX = n4 + n3;
            maxY = n5 + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + n2;

            ((ITargetVisual) mode.getSubModeValues().get((int) mode.getInput())).render(target);
        }
    }

    @Override
    public void onEnable() {
        mode.enable();
        MoveableManager.register(this);
    }

    public void onDisable() {
        MoveableManager.unregister(this);
        mode.disable();

        target = null;
        lastTargetTime = -1;
    }

    @Override
    public void onUpdate() {
        if (!Utils.nullCheck()) {
            target = null;
            return;
        }

        if (KillAura.target != null) {
            target = KillAura.target;
            lastTargetTime = System.currentTimeMillis();
        }

        if (target != null && lastTargetTime != -1 && (target.isDead || System.currentTimeMillis() - lastTargetTime > 5000 || target.getDistanceSqToEntity(mc.thePlayer) > 20)) {
            target = null;
            lastTargetTime = -1;
        }


        if (onlyKillAura.isToggled()) return;

        // manual target
        if (target != null) {
            if (!Utils.inFov(180, target) || target.getDistanceSqToEntity(mc.thePlayer) > 36) {
                target = null;
            }
        } else {
            if (mc.objectMouseOver != null
                    && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                    && mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
                target = (EntityLivingBase) mc.objectMouseOver.entityHit;
                lastTargetTime = System.currentTimeMillis();
            }
        }
    }

    @EventListener
    public void onAttack(AttackEntityEvent event) {
        if (onlyKillAura.isToggled()) return;

        if (event.getTarget() instanceof EntityLivingBase) {
            target = (EntityLivingBase) event.getTarget();
        }
    }

    @EventListener
    public void onRender(Render2DEvent event) {
        if (mc.currentScreen != null) {
            return;
        }
        render(target);
    }

    @Override
    public void render() {
        render(mc.thePlayer);
    }

    @Override
    public int getMinX() {
        return minX;
    }

    @Override
    public int getMaxX() {
        return maxX;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    @Override
    public void moveX(int amount) {
        posX += amount;
    }

    @Override
    public void moveY(int amount) {
        posY += amount;
    }
}
