package keystrokesmod.event.player;

import keystrokesmod.eventbus.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SprintEvent extends Event {
    private boolean sprint;
}
