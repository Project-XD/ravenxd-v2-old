package keystrokesmod.mixins.impl.gui;

import net.minecraft.client.multiplayer.GuiConnecting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiConnecting.class)
public interface GuiConnectingAccessor {
    @Accessor("cancel")
    boolean isCancel();
}
