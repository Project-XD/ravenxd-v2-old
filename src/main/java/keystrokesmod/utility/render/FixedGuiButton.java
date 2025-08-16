package keystrokesmod.utility.render;

import keystrokesmod.module.ModuleManager;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.render.blur.GaussianBlur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static keystrokesmod.Client.mc;

public class FixedGuiButton extends GuiButton {
    private int hoverValue;

    private final Animation zoomAnimation = new Animation(Easing.EASE_OUT_SINE, 150);

    public FixedGuiButton(int id, int x, int y, int width, int height, String name) {
        super(id, x, y, width, height, name);
        zoomAnimation.setValue(0);
    }

    @Contract("_, _ -> new")
    private static @NotNull Color interpolateColorC(final @NotNull Color color1, final @NotNull Color color2) {
        return new Color(
                ColorUtils.interpolateInt(color1.getRed(), color2.getRed(), 0),
                ColorUtils.interpolateInt(color1.getGreen(), color2.getGreen(), 0),
                ColorUtils.interpolateInt(color1.getBlue(), color2.getBlue(), 0),
                ColorUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), 0)
        );
    }

    @Override
    public void drawButton(Minecraft p_drawButton_1_, int x, int y) {
        if (visible) {
            final boolean blur = ModuleManager.clientTheme.buttonBlur.isToggled();
            double xPosition = this.xPosition;
            double yPosition = this.yPosition;
            double width = this.width;
            double height = this.height;

            hovered = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;

            if (hovered) {
                hoverValue = (int) Math.min(hoverValue + 4.0 * 150 / Minecraft.getDebugFPS(), 200);
            } else {
                hoverValue = (int) Math.max(hoverValue - 4.0 * 150 / Minecraft.getDebugFPS(), 102);
            }
            zoomAnimation.run(Utils.limit(((double) hoverValue - 102) / 30, 0, 4));
            double zoomAmount = zoomAnimation.getValue();
            xPosition -= zoomAmount;
            width += zoomAmount * 2;

            Color rectColor = new Color(35, 37, 43, hoverValue);
            rectColor = interpolateColorC(rectColor, ColorUtils.brighter(rectColor, 0.4f));
            RenderUtils.drawBloomShadow((float) (xPosition - 3), (float) (yPosition - 3), (float) (width + 6), (float) (height + 6), 12, new Color(0, 0, 0, 50), false);
            RRectUtils.drawRoundOutline(xPosition, yPosition, width, height, 4, 0.0015f, rectColor, new Color(255, 255, 255, 20));
            if (blur) {
                if (GaussianBlur.startBlur()) {
                    RRectUtils.drawRoundOutline(xPosition, yPosition, width, height, 4, 0.0015f, new Color(0, 0, 0, 5), new Color(0, 0, 0, 5));
                    RRectUtils.drawRoundOutline(xPosition, yPosition, width, height, 4, 0.0015f, new Color(0, 0, 0, 50), new Color(200, 200, 200, 60));
                    GaussianBlur.endBlur(10, 1);
                }
            }

            this.mouseDragged(mc, x, y);

            String text = ModuleManager.clientTheme.buttonLowerCase.isToggled() ?
                    displayString.toLowerCase() : displayString;

            if (FontManager.tenacity20.isTextSupported(text)) {
                FontManager.tenacity20.drawCenteredString(text,
                        xPosition + width / 2.0f,
                        yPosition + height / 2f - FontManager.tenacity20.height() / 2f,
                        -1
                );
            } else {
                FontManager.getMinecraft().drawCenteredString(text,
                        xPosition + width / 2.0f,
                        yPosition + height / 2f - FontManager.tenacity20.height() / 2f,
                        -1
                );
            }
        } else {
            zoomAnimation.run(0);
        }
    }
}
