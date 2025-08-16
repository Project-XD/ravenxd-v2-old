package keystrokesmod.event.player;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@Getter
@Setter
@AllArgsConstructor
public class PreAttackEvent extends CancellableEvent {
    private final Entity entity;
}
