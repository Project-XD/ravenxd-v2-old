package keystrokesmod.event.render;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class PreRenderPlayerEvent extends CancellableEvent {
    private final EntityPlayer entity;
    private final RenderPlayer renderer;
    private final float partialRenderTick;
    private final double x;
    private final double y;
    private final double z;
}
