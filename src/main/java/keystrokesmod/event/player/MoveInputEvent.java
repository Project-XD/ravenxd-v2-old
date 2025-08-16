package keystrokesmod.event.player;

import keystrokesmod.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MoveInputEvent extends Event {
    private float forward, strafe;
    private boolean jump, sneak;
    private double sneakSlowDown;

    public MoveInputEvent(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDown) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakSlowDown = sneakSlowDown;
    }

    public void cancel() {
        setForward(0);
        setStrafe(0);
        setJump(false);
        setSneak(false);
    }
}
