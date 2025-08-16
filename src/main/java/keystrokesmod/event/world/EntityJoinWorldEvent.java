package keystrokesmod.event.world;

import keystrokesmod.eventbus.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class EntityJoinWorldEvent extends Event {
    private final Entity entity;
}
