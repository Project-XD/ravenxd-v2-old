package keystrokesmod.event.render;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.Getter;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;

@Getter
public final class RenderItemEvent extends CancellableEvent {
    private final EnumAction enumAction;
    private final boolean useItem;
    private final float animationProgression;
    private final float partialTicks;
    private final float swingProgress;
    private final ItemStack itemToRender;

    public RenderItemEvent(EnumAction enumAction, boolean useItem, float animationProgression, float partialTicks, float swingProgress, ItemStack itemToRender) {
        this.enumAction = enumAction;
        this.useItem = useItem;
        this.animationProgression = animationProgression;
        this.partialTicks = partialTicks;
        this.swingProgress = swingProgress;
        this.itemToRender = itemToRender;
    }

}