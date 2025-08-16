package keystrokesmod.event.render;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class DrawBlockHighlightEvent extends CancellableEvent {
    private final RenderGlobal context;
    private final EntityPlayer player;
    private final MovingObjectPosition target;
    private final int subID;
    private final ItemStack currentItem;
    private final float partialTicks;
}
