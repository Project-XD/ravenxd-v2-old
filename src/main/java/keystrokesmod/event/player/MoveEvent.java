package keystrokesmod.event.player;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class MoveEvent extends CancellableEvent {
    private double x;
    private double y;
    private double z;
}
