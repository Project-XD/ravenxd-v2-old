package keystrokesmod.event.network;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AttackEntityEvent extends CancellableEvent {
    private final Entity target;
}
