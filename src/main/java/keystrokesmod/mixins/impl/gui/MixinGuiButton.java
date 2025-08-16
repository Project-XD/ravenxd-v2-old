package keystrokesmod.mixins.impl.gui;

import keystrokesmod.module.ModuleManager;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.render.ColorUtils;
import keystrokesmod.utility.render.RRectUtils;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.render.blur.GaussianBlur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;


@SuppressWarnings("UnresolvedMixinReference")
@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {
    @Shadow
    @Final
    protected static ResourceLocation buttonTextures;
    @Shadow
    public boolean visible;
    @Shadow
    public int xPosition;
    @Shadow
    public int yPosition;
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    public boolean enabled;
    @Shadow
    public String displayString;
    @Shadow
    protected boolean hovered;
    @Unique
    private int ravenXD$hoverValue;

    @Unique
    @Contract("_, _ -> new")
    private static @NotNull Color raven_XD$interpolateColorC(final @NotNull Color color1, final @NotNull Color color2) {
        return new Color(
                ColorUtils.interpolateInt(color1.getRed(), color2.getRed(), 0), 
                ColorUtils.interpolateInt(color1.getGreen(), color2.getGreen(), 0), 
                ColorUtils.interpolateInt(color1.getBlue(), color2.getBlue(), 0), 
                ColorUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), 0)
        );
    }

    @Shadow
    protected abstract int getHoverState(boolean p_getHoverState_1_);

    @Shadow
    protected abstract void mouseDragged(Minecraft p_mouseDragged_1_, int p_mouseDragged_2_, int p_mouseDragged_3_);

    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    public void onDrawButton(Minecraft minecraft, int x, int y, CallbackInfo ci) {
        if (this.visible) {
            final int height = (int) Math.round(this.height * 0.9);
            final boolean blur = ModuleManager.clientTheme.buttonBlur.isToggled();
            
            this.hovered = x >= xPosition && y >= yPosition && x < xPosition + this.width && y < yPosition + height;

            if (hovered) {
                ravenXD$hoverValue = (int) Math.min(ravenXD$hoverValue + 4.0 * 150 / Minecraft.getDebugFPS(), 200);

            } else {
                ravenXD$hoverValue = (int) Math.max(ravenXD$hoverValue - 4.0 * 150 / Minecraft.getDebugFPS(), 102);
            }

            Color rectColor = new Color(35, 37, 43, ravenXD$hoverValue);
            rectColor = raven_XD$interpolateColorC(rectColor, ColorUtils.brighter(rectColor, 0.4f));
            RenderUtils.drawBloomShadow(xPosition - 3, yPosition - 3, width + 6, height + 6, 12, new Color(0, 0, 0, 50), false);
            RRectUtils.drawRoundOutline(xPosition, yPosition, width, height, 4, 0.0015f, rectColor, new Color(255, 255, 255, 20));
            if (blur) {
                if (GaussianBlur.startBlur()) {
                    RRectUtils.drawRoundOutline(xPosition, yPosition, width, height, 4, 0.0015f, new Color(0, 0, 0, 5), new Color(0, 0, 0, 5));
                    RRectUtils.drawRoundOutline(xPosition, yPosition, width, height, 4, 0.0015f, new Color(0, 0, 0, 50), new Color(200, 200, 200, 60));
                    GaussianBlur.endBlur(10, 1);
                }
            }

            this.mouseDragged(minecraft, x, y);

            String text = ModuleManager.clientTheme.buttonLowerCase.isToggled() ?
                    displayString.toLowerCase() : displayString;

            if (FontManager.tenacity20.isTextSupported(text)) {
                FontManager.tenacity20.drawCenteredString(text,
                        xPosition + this.width / 2.0f,
                        yPosition + height / 2f - FontManager.tenacity20.height() / 2f,
                        -1
                );
            } else {
                FontManager.getMinecraft().drawCenteredString(text,
                        xPosition + this.width / 2.0f,
                        yPosition + height / 2f - FontManager.tenacity20.height() / 2f,
                        -1
                );
            }
        }

        ci.cancel();
    }
}
