package keystrokesmod.module.impl.render.targetvisual.targethud;

import keystrokesmod.module.impl.render.TargetHUD;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.render.blur.GaussianBlur;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static keystrokesmod.module.impl.render.TargetHUD.*;

public class RavenNewTargetHUD extends SubMode<TargetHUD> implements ITargetVisual {
    public static final int BACKGROUND_COLOR = new Color(0, 0, 0, 80).getRGB();
    public static final int RECT_COLOR = new Color(255, 255, 255, 30).getRGB();
    public static final int TEXT_DIST_TO_RECT = 6;
    public static final int RECT_SHADOW_DIST = 6;

    private final ModeSetting theme;
    private final ModeSetting font;
    private final ButtonSetting animation;
    private final ButtonSetting blur;
    private final ButtonSetting background;
    private final ButtonSetting shadow;
    private final ButtonSetting showStatus;
    private final ButtonSetting healthColor;
    private final Animation healthBarAnimation = new Animation(Easing.EASE_OUT_CIRC, 150);
    private final Animation healthBarAnimation2 = new Animation(Easing.EASE_OUT_SINE, 500);

    public RavenNewTargetHUD(String name, @NotNull TargetHUD parent) {
        super(name, parent);
        this.registerSetting(theme = new ModeSetting("Theme", Theme.themes, 0));
        this.registerSetting(font = new ModeSetting("Font", new String[]{"Minecraft", "ProductSans", "Regular"}, 0));
        this.registerSetting(animation = new ButtonSetting("Animation", true));
        this.registerSetting(blur = new ButtonSetting("Blur", true));
        this.registerSetting(background = new ButtonSetting("Background", false));
        this.registerSetting(shadow = new ButtonSetting("Shadow", false));
        this.registerSetting(showStatus = new ButtonSetting("Show win or loss", true));
        this.registerSetting(healthColor = new ButtonSetting("Traditional health color", false));
    }

    private IFont getFont() {
        switch ((int) font.getInput()) {
            default:
            case 0:
                return FontManager.getMinecraft();
            case 1:
                return FontManager.productSansMedium;
            case 2:
                return FontManager.regular22;
        }
    }

    @Override
    public void render(@NotNull EntityLivingBase target) {
        String name = target.getDisplayName().getFormattedText();
        String healthText = " " + (int) target.getHealth();
        float health = Utils.limit(target.getHealth() / target.getMaxHealth(), 0, 1);
        if (Float.isInfinite(health) || Float.isNaN(health)) {
            health = 0;
        }

        if (showStatus.isToggled() && mc.thePlayer != null) {
            healthText = healthText + " " + ((health <= Utils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? "§aW" : "§cL");
        }

        final String renderText = name + " " + healthText;
        final ScaledResolution scaled = new ScaledResolution(mc);
        minX = scaled.getScaledWidth() / 2 + posX;
        minY = scaled.getScaledHeight() / 2 + 15 + posY;
        maxX = minX + (int) Math.round(getFont().width(renderText)) + 12;
        maxY = minY + 16 + 12;

        if (background.isToggled()) {
            RenderUtils.drawBloomShadow(
                    (float) (minX - RECT_SHADOW_DIST), (float) (minY - RECT_SHADOW_DIST),
                    (float) (maxX - minX + RECT_SHADOW_DIST * 2.0), (float) (maxY - minY + RECT_SHADOW_DIST * 2.0),
                    4, 10, BACKGROUND_COLOR, false);
        }

        if (blur.isToggled()) {
            if (GaussianBlur.startBlur()) {
                RenderUtils.drawBloomShadow(
                        minX, minY,
                        maxX - minX, maxY - minY,
                        4, 10, -1, false);
                GaussianBlur.endBlur(4, 1);
            }
        }

        int healthTextColor = Utils.getColorForHealth(health);
        getFont().drawString(name, minX + TEXT_DIST_TO_RECT, minY + TEXT_DIST_TO_RECT, -1, shadow.isToggled());
        getFont().drawString(healthText, minX + TEXT_DIST_TO_RECT + getFont().width(name), minY + TEXT_DIST_TO_RECT, healthTextColor, shadow.isToggled());

        float healthBar = (float) (int) (maxX - 6 + (minX + 6 - maxX - 6) * (1.0 - ((health < 0.05) ? 0.05 : health)));
        if (healthBar - minX + 3 < 0) { // if goes below, the rounded health bar glitches out
            healthBar = minX + 3;
        }

        float lastHealthBar;
        if (animation.isToggled()) {
            lastHealthBar = (float) healthBarAnimation.getValue();
            healthBarAnimation.run(healthBar);
            healthBarAnimation2.run(healthBar);
        } else {
            lastHealthBar = healthBar;
        }

        RenderUtils.drawRoundedGradientRect((float) minX + 6, (float) maxY - 9, (float) healthBarAnimation2.getValue(), (float) (maxY - 4), 4.0f,
                Utils.merge(Theme.getGradients((int) theme.getInput())[0], 100), Utils.merge(Theme.getGradients((int) theme.getInput())[0], 60),
                Utils.merge(Theme.getGradients((int) theme.getInput())[1], 100), Utils.merge(Theme.getGradients((int) theme.getInput())[1], 60));

        RenderUtils.drawRoundedGradientRect((float) minX + 6, (float) maxY - 9, lastHealthBar, (float) (maxY - 4), 4.0f,
                Utils.merge(Theme.getGradients((int) theme.getInput())[0], 210), Utils.merge(Theme.getGradients((int) theme.getInput())[0], 210),
                Utils.merge(Theme.getGradients((int) theme.getInput())[1], 210), Utils.merge(Theme.getGradients((int) theme.getInput())[1], 210));

        if (healthColor.isToggled()) {
            RenderUtils.drawRoundedRectangle((float) minX + 6, (float) maxY - 9, lastHealthBar, (float) (maxY - 4), 4.0f, healthTextColor);
        }
    }
}