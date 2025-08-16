package keystrokesmod.event.network;

import keystrokesmod.eventbus.CancellableEvent;
import keystrokesmod.utility.GuiConnectingMsg;
import lombok.Getter;
import net.minecraft.client.multiplayer.GuiConnecting;

@Getter
public class PreConnectEvent extends CancellableEvent {
    private final GuiConnecting screen;
    private final String ip;
    private final int port;
    private final GuiConnectingMsg extraMessage = new GuiConnectingMsg();

    public PreConnectEvent(GuiConnecting screen, String ip, int port) {
        this.screen = screen;
        this.ip = ip;
        this.port = port;
    }

}
