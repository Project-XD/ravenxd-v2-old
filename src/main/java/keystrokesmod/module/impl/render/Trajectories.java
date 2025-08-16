package keystrokesmod.module.impl.render;

import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.event.render.Render3DEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.movement.TrajectoriesEngine;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.item.*;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Trajectories extends Module {
    private static final int HIGHLIGHT_COLOR = new Color(234, 38, 38).getRGB();

    private final ButtonSetting autoScale;
    private final ButtonSetting disableUncharged;
    private final ButtonSetting highlightOnEntity;

    private final TrajectoriesEngine engine = new TrajectoriesEngine();

    public Trajectories() {
        super("Trajectories", category.render);
        this.registerSetting(autoScale = new ButtonSetting("Auto-scale", true));
        this.registerSetting(disableUncharged = new ButtonSetting("Disable uncharged bow", true));
        this.registerSetting(highlightOnEntity = new ButtonSetting("Highlight on entity", true));
    }

    @EventListener
    public void onPreUpdate(PreUpdateEvent event) {
        if (!Utils.nullCheck() || mc.thePlayer.getHeldItem() == null) {
            return;
        }
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (!(heldItem.getItem() instanceof ItemBow) && !(heldItem.getItem() instanceof ItemSnowball) && !(heldItem.getItem() instanceof ItemEgg) && !(heldItem.getItem() instanceof ItemEnderPearl)) {
            return;
        }
        if (heldItem.getItem() instanceof ItemBow && !mc.thePlayer.isUsingItem() && disableUncharged.isToggled()) {
            return;
        }

        engine.requestUpdate();
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (!Utils.nullCheck() || mc.thePlayer.getHeldItem() == null) {
            return;
        }
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (!(heldItem.getItem() instanceof ItemBow) && !(heldItem.getItem() instanceof ItemSnowball) && !(heldItem.getItem() instanceof ItemEgg) && !(heldItem.getItem() instanceof ItemEnderPearl)) {
            return;
        }
        if (heldItem.getItem() instanceof ItemBow && !mc.thePlayer.isUsingItem() && disableUncharged.isToggled()) {
            return;
        }

        RenderUtils.glColor(-1);
        GL11.glPushMatrix();
        boolean bl3 = GL11.glIsEnabled(2929);
        boolean bl4 = GL11.glIsEnabled(3553);
        boolean bl5 = GL11.glIsEnabled(3042);
        if (bl3) {
            GL11.glDisable(2929);
        }
        if (bl4) {
            GL11.glDisable(3553);
        }
        GL11.glLineWidth(1.5f);
        GL11.glBegin(3);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        if (!bl5) {
            GL11.glEnable(3042);
        }

        MovingObjectPosition target = engine.getTarget();
        Vec3 vec3 = Utils.getEyePos();
        for (Vec3 predictPose : engine.getPredictPoses()) {
            vec3 = predictPose;
            GL11.glVertex3d(vec3.x, vec3.y, vec3.z);
        }

        // 落点
        double posX = vec3.x;
        double posY = vec3.y;
        double posZ = vec3.z;

        if (target != null && highlightOnEntity.isToggled()) {
            RenderUtils.glColor(HIGHLIGHT_COLOR);
        }
        GL11.glVertex3d(posX, posY, posZ);

        GL11.glEnd();
        GL11.glDisable(2929);
        GL11.glDisable(3042);
        GL11.glTranslated(posX - mc.getRenderManager().viewerPosX, posY - mc.getRenderManager().viewerPosY, posZ - mc.getRenderManager().viewerPosZ);
        if (target != null && target.sideHit != null) {
            switch (target.sideHit.getIndex()) {
                case 2:
                case 3: {
                    GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
                    break;
                }
                case 4:
                case 5: {
                    GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
                    break;
                }
            }
        }
        if (autoScale.isToggled()) {
            double distance = Math.max(mc.thePlayer.getDistance(posX, posY, posZ) * 0.042830285, 1);
            GL11.glScaled(distance, distance, distance);
        }
        this.drawX();
        GL11.glDisable(2848);
        if (bl3) {
            GL11.glEnable(2929);
        }
        if (bl4) {
            GL11.glEnable(3553);
        }
        if (!bl5) {
            GL11.glDisable(3042);
        }
        GL11.glPopMatrix();
    }

    public void drawX() {
        GL11.glBegin(1);
        GL11.glVertex3d(-0.25, 0.0, 0.0);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, 0.0, -0.25);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.25, 0.0, 0.0);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, 0.0, 0.25);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glEnd();
    }
}
