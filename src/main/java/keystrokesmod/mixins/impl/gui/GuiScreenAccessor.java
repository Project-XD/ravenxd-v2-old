package keystrokesmod.mixins.impl.gui;


import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {

    @Invoker("mouseClicked")
    void mouseClicked(int x, int y, int mouse);

    @Accessor("buttonList")
    List<GuiButton> getButtonList();
}
