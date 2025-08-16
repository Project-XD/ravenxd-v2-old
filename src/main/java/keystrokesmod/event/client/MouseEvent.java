package keystrokesmod.event.client;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lwjgl.input.Mouse;

@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("unused")
@Data
public class MouseEvent extends CancellableEvent {
    private int x = Mouse.getEventX();
    private int y = Mouse.getEventY();
    private int dx = Mouse.getEventDX();
    private int dy = Mouse.getEventDY();
    private int dwheel = Mouse.getEventDWheel();
    private int button = Mouse.getEventButton();
    private boolean buttonstate = Mouse.getEventButtonState();
    private long nanoseconds = Mouse.getEventNanoseconds();
}
