package keystrokesmod.event.player;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.*;
import net.minecraft.util.MovingObjectPosition;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ScaffoldPlaceEvent extends CancellableEvent {
    private MovingObjectPosition hitResult;
    private boolean extra;
}
