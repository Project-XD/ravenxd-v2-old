package keystrokesmod.event.render;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.EntityLivingBase;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class PreRenderNameTag extends CancellableEvent {
    private final EntityLivingBase entity;
    private final double x;
    private final double y;
    private final double z;
}
