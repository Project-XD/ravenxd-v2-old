package keystrokesmod.event.player;


import keystrokesmod.eventbus.Event;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class SafeWalkEvent extends Event {
    private boolean safeWalk;
}
