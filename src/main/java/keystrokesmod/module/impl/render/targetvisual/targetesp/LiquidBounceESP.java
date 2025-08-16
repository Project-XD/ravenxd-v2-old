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

public class LiquidBounceESP extends SubMode<TargetESP> implements ITargetVisual {
    private final ModeSetting colorMode;
    private final SliderSetting red, green, blue;
    private final SliderSetting pulseSpeed, pulseIntensity;
    private final SliderSetting gradientSpeed;
    private final SliderSetting animationSpeed;
    private final SliderSetting heightOffset;

    private Color currentColor = new Color(0, 150, 255, 150);
    private long lastUpdate = System.currentTimeMillis();
    private final Minecraft mc = Minecraft.getMinecraft();

    public LiquidBounceESP(String name, @NotNull TargetESP parent) {
        super(name, parent);

        this.registerSetting(colorMode = new ModeSetting("Color mode", new String[]{"Pulse", "Rainbow", "Static"}, 0));
        this.registerSetting(red = new SliderSetting("Red", 0, 0, 255, 1));
        this.registerSetting(green = new SliderSetting("Green", 150, 0, 255, 1));
        this.registerSetting(blue = new SliderSetting("Blue", 255, 0, 255, 1));
        this.registerSetting(pulseSpeed = new SliderSetting("Pulse speed", 1.5, 0.1, 5, 0.1, new ModeOnly(colorMode, 0)));
        this.registerSetting(pulseIntensity = new SliderSetting("Pulse intensity", 0.6, 0.1, 1, 0.05, new ModeOnly(colorMode, 0)));
        this.registerSetting(gradientSpeed = new SliderSetting("Rainbow speed", 2.0, 0.5, 5, 0.1, new ModeOnly(colorMode, 1)));
        this.registerSetting(animationSpeed = new SliderSetting("Animation speed", 1.8, 0.5, 5, 0.1));
        this.registerSetting(heightOffset = new SliderSetting("Height offset", 0.1, 0, 0.5, 0.01));
    }

    @Override
    public void onUpdate() {
        updateColor();
    }

    private void updateColor() {
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastUpdate) / 1000f;
        lastUpdate = currentTime;

        int baseRed = (int) red.getInput();
        int baseGreen = (int) green.getInput();
        int baseBlue = (int) blue.getInput();

        switch ((int) colorMode.getInput()) {
            case 0:
                float pulse = (float) (Math.sin(currentTime * 0.001 * pulseSpeed.getInput()) * 0.5 + 0.5);
                int alpha = 150 + (int) (105 * pulse * pulseIntensity.getInput());
                currentColor = new Color(baseRed, baseGreen, baseBlue, alpha);
                break;

            case 1:
                float hue = (System.currentTimeMillis() % (10000 / (int) gradientSpeed.getInput())) / (10000f / (float) gradientSpeed.getInput());
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

        double offset = heightOffset.getInput() * Math.sin(System.currentTimeMillis() * 0.002 * animationSpeed.getInput());

        drawCylinder(x, y + offset, z, radius, height - offset * 0.5, 32, currentColor);

        drawRing(x, y + height + offset * 0.5, z, radius * 1.05, 24,
                new Color(255, 255, 255, (int)(200 * (0.5 + 0.5 * Math.sin(System.currentTimeMillis() * 0.005)))));

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void drawCylinder(double x, double y, double z, double radius, double height, int segments, Color color) {
        GL11.glBegin(GL11.GL_QUAD_STRIP);

        for (int i = 0; i <= segments; i++) {
            double angle = (Math.PI * 2 * i) / segments;
            double dx = Math.sin(angle) * radius;
            double dz = Math.cos(angle) * radius;

            GL11.glColor4f(
                    color.getRed()/255f,
                    color.getGreen()/255f,
                    color.getBlue()/255f,
                    color.getAlpha()/510f
            );
            GL11.glVertex3d(x + dx, y, z + dz);

            GL11.glColor4f(
                    color.getRed()/255f,
                    color.getGreen()/255f,
                    color.getBlue()/255f,
                    color.getAlpha()/255f
            );
            GL11.glVertex3d(x + dx, y + height, z + dz);
        }

        GL11.glEnd();

        GL11.glLineWidth(1.5f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glColor4f(0, 0, 0, 0.8f);

        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * 2 * i) / segments;
            double dx = Math.sin(angle) * radius;
            double dz = Math.cos(angle) * radius;
            GL11.glVertex3d(x + dx, y + height, z + dz);
        }

        GL11.glEnd();
    }

    private void drawRing(double x, double y, double z, double radius, int segments, Color color) {
        GL11.glLineWidth(2.5f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glColor4f(
                color.getRed()/255f,
                color.getGreen()/255f,
                color.getBlue()/255f,
                color.getAlpha()/255f
        );

        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * 2 * i) / segments;
            GL11.glVertex3d(
                    x + Math.sin(angle) * radius,
                    y,
                    z + Math.cos(angle) * radius
            );
        }

        GL11.glEnd();
    }
}