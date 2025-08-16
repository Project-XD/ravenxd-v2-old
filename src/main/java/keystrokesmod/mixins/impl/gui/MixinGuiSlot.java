package keystrokesmod.mixins.impl.gui;


import keystrokesmod.utility.render.BackgroundUtils;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSlot.class)
public class MixinGuiSlot {

    @Inject(method = "drawContainerBackground", at = @At("HEAD"), cancellable = true, remap = false)
    public void onDrawContainerBackground(Tessellator p_drawContainerBackground_1_, @NotNull CallbackInfo ci) {
        BackgroundUtils.renderBackground((GuiSlot) (Object) this);

        ci.cancel();
    }
}
