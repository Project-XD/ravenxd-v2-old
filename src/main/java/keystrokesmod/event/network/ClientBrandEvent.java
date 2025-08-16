package keystrokesmod.event.network;

import keystrokesmod.eventbus.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ClientBrandEvent extends Event {
    private String brand;
}
