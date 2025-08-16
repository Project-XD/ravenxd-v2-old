package keystrokesmod.event.player;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class PreVelocityEvent extends CancellableEvent {
    private int motionX;
    private int motionY;
    private int motionZ;
}
