package keystrokesmod.event.network;

import keystrokesmod.eventbus.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ReceivePacketEvent extends CancellableEvent {
    private final Packet<INetHandlerPlayClient> packet;
}
