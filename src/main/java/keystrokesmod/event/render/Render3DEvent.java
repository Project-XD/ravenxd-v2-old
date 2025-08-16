package keystrokesmod.event.render;

import keystrokesmod.eventbus.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class Render3DEvent extends Event {
    private final float partialTicks;
}
