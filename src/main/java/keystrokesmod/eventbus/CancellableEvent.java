package keystrokesmod.eventbus;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class CancellableEvent extends Event {
    private boolean cancelled = false;

    public void cancel() {
        this.cancelled = true;
    }
}

