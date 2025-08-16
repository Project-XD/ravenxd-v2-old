package keystrokesmod.event.player;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class JumpEvent extends CancellableEvent {
    private float motionY, yaw;
}
