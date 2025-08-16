package keystrokesmod.event.player;

import keystrokesmod.eventbus.Event;
import lombok.Getter;

@Getter
public class StepEvent extends Event {
    private final double height;

    public StepEvent(double height) {
        this.height = height;
    }

}
