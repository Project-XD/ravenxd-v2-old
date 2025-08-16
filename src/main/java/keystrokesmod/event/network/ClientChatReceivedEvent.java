package keystrokesmod.event.network;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.util.IChatComponent;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ClientChatReceivedEvent extends CancellableEvent {
    private IChatComponent message;
    private byte type;
}
