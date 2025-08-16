package keystrokesmod.event.render;

import keystrokesmod.eventbus.Event;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class SwingAnimationEvent extends Event {
    private int animationEnd;
}