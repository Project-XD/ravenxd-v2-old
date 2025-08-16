package keystrokesmod.event.player;

import keystrokesmod.eventbus.CancellableEvent;
import keystrokesmod.utility.MoveUtil;
import lombok.Getter;
import lombok.Setter;

import static keystrokesmod.Client.mc;

@Setter
@Getter
public class PrePlayerInputEvent extends CancellableEvent {
    private float strafe;
    private float forward;
    private float friction;
    private float yaw;

    public PrePlayerInputEvent(float strafe, float forward, float friction, float yaw) {
        this.strafe = strafe;
        this.forward = forward;
        this.friction = friction;
        this.yaw = yaw;
    }

    public void setSpeed(final double speed, final double motionMultiplier) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        mc.thePlayer.motionX *= motionMultiplier;
        mc.thePlayer.motionZ *= motionMultiplier;
    }

    public void setSpeed(final double speed) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        MoveUtil.stop();
    }
}
