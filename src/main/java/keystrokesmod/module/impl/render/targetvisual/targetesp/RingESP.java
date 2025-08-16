package keystrokesmod.module.impl.render.targetvisual.targetesp;

import keystrokesmod.module.impl.render.TargetESP;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RingESP extends SubMode<TargetESP> implements ITargetVisual {
    private final ModeSetting colorMode;
    private final SliderSetting red, green, blue;
    private final SliderSetting pulseSpeed, pulseIntensity;
    private final SliderSetting rainbowSpeed;
    private final SliderSetting ringCount;
    private final SliderSetting rotationSpeed;
    private final SliderSetting ringThickness;
    private final SliderSetting pulseAnimation;

    private Color currentColor = new Color(0, 150, 255, 180);
    private float rotationAngle = 0;
    private final Minecraft mc = Minecraft.getMinecraft();

    public RingESP(String name, @NotNull TargetESP parent) {
        super(name, parent);

        this.registerSetting(new DescriptionSetting("Dynamic Ring ESP"));
        this.registerSetting(colorMode = new ModeSetting("Color mode", new String[]{"Pulse", "Rainbow", "Static"}, 0));
        this.registerSetting(red = new SliderSetting("Red", 0, 0, 255, 1));
        this.registerSetting(green = new SliderSetting("Green", 150, 0, 255, 1));
        this.registerSetting(blue = new SliderSetting("Blue", 255, 0, 255, 1));
        this.registerSetting(pulseSpeed = new SliderSetting("Pulse speed", 1.5, 0.1, 5, 0.1, new ModeOnly(colorMode, 0)));
        this.registerSetting(pulseIntensity = new SliderSetting("Pulse intensity", 0.6, 0.1, 1, 0.05, new ModeOnly(colorMode, 0)));
        this.registerSetting(rainbowSpeed = new SliderSetting("Rainbow speed", 2.0, 0.5, 5, 0.1, new ModeOnly(colorMode, 1)));
        this.registerSetting(ringCount = new SliderSetting("Ring Count", 3, 1, 6, 1));
        this.registerSetting(rotationSpeed = new SliderSetting("Rotation Speed", 1.0, 0.1, 3, 0.1));
        this.registerSetting(ringThickness = new SliderSetting("Ring Thickness", 1.5, 0.5, 3, 0.1));
        this.registerSetting(pulseAnimation = new SliderSetting("Pulse Animation", 1.8, 0.5, 5, 0.1));
    }

    @Override
    public void onUpdate() {
        updateColor();
        rotationAngle += (float) (rotationSpeed.getInput() * 0.5f);
        if (rotationAngle > 360) rotationAngle -= 360;
    }

    private void updateColor() {
        long currentTime = System.currentTimeMillis();
        int baseRed = (int) red.getInput();
        int baseGreen = (int) green.getInput();
        int baseBlue = (int) blue.getInput();

        switch ((int) colorMode.getInput()) {
            case 0:
                float pulse = (float) (Math.sin(currentTime * 0.001 * pulseSpeed.getInput()) * 0.5 + 0.5);
                int alpha = 180 + (int) (75 * pulse * pulseIntensity.getInput());
                currentColor = new Color(baseRed, baseGreen, baseBlue, alpha);
                break;

            case 1:
                float hue = (System.currentTimeMillis() % (10000 / (int) rainbowSpeed.getInput())) / (10000f / (float) rainbowSpeed.getInput());
                currentColor = Color.getHSBColor(hue, 0.8f, 1.0f);
                currentColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 180);
                break;

            case 2:
                currentColor = new Color(baseRed, baseGreen, baseBlue, 180);
                break;
        }
    }

    @Override
    public void render(@NotNull EntityLivingBase target) {
        updateColor();

        mc.entityRenderer.disableLightmap();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        mc.entityRenderer.disableLightmap();

        double radius = target.width * 0.9;
        double height = target.height;
        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;

        drawRings(x, y, z, radius, height);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void drawRings(double x, double y, double z, double radius, double height) {
        int rings = (int) ringCount.getInput();
        double time = System.currentTimeMillis() * 0.001 * pulseAnimation.getInput();

        for (int i = 0; i < rings; i++) {
            double ringHeight = y + (i + 1) * height / (rings + 1);
            double ringRadius = radius * (0.9 + 0.1 * Math.sin(time + i));
            double pulse = 0.5 + 0.5 * Math.sin(time * 2 + i);

            GL11.glPushMatrix();
            GL11.glTranslated(x, ringHeight, z);
            GL11.glRotated(rotationAngle + i * 30, 0, 1, 0);
            GL11.glTranslated(-x, -ringHeight, -z);

            GL11.glLineWidth((float) (ringThickness.getInput() * (0.8 + 0.2 * pulse)));
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glColor4f(
                    currentColor.getRed()/255f,
                    currentColor.getGreen()/255f,
                    currentColor.getBlue()/255f,
                    currentColor.getAlpha()/255f * (float) (0.7 + 0.3 * pulse)
            );

            int segments = 36;
            for (int j = 0; j < segments; j++) {
                double angle = Math.toRadians(j * 10);
                double dx = Math.sin(angle) * ringRadius;
                double dz = Math.cos(angle) * ringRadius;
                GL11.glVertex3d(x + dx, ringHeight, z + dz);
            }

            GL11.glEnd();
            GL11.glPopMatrix();
        }
    }
}