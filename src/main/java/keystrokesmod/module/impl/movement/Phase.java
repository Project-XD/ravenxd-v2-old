package keystrokesmod.module.impl.movement;

import keystrokesmod.event.world.PushOutOfBlockEvent;
import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.phase.*;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class Phase extends Module {
    private final ModeValue mode;
    private final ButtonSetting cancelS08;
    private final ButtonSetting cancelPush;

    public Phase() {
        super("Phase", category.movement);
        this.registerSetting(new DescriptionSetting("Lets you go through solid blocks."));
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new VanillaPhase("Vanilla", this))
                .add(new WatchdogPhase("Watchdog", this))
                .add(new WatchdogAutoPhase("Watchdog Auto", this))
                .add(new VulcanPhase("Vulcan", this))
                .add(new GrimACPhase("GrimAC", this))
        );
        this.registerSetting(cancelS08 = new ButtonSetting("Cancel S08", false));
        this.registerSetting(cancelPush = new ButtonSetting("Cancel push", true));
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    @EventListener
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && cancelS08.isToggled())
            event.cancel();
    }

    @EventListener
    public void onPushOutOfBlock(PushOutOfBlockEvent event) {
        if (cancelPush.isToggled())
            event.cancel();
    }

    @Override
    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getPrettyName();
    }
}
