package keystrokesmod.event.world;

import keystrokesmod.eventbus.CancellableEvent;
import keystrokesmod.utility.movement.Direction;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class PushOutOfBlockEvent extends CancellableEvent {
    private @NotNull Direction direction;
    private float pushMotion;

}
