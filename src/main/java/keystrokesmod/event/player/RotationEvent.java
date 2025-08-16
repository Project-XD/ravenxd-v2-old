package keystrokesmod.event.player;

import keystrokesmod.eventbus.Event;
import keystrokesmod.module.impl.other.RotationHandler;
import lombok.Getter;
import lombok.Setter;

public class RotationEvent extends Event {
    @Getter
    private float yaw;
    @Getter
    private float pitch;
    @Setter
    @Getter
    private RotationHandler.MoveFix moveFix;
    @Getter
    private boolean smoothBack = true;
    private boolean isSet;

    public RotationEvent(float yaw, float pitch, RotationHandler.MoveFix moveFix) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.moveFix = moveFix;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        isSet = true;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        isSet = true;
    }

    public boolean isSet() {
        return isSet;
    }

    public void noSmoothBack() {
        smoothBack = false;
    }
}
