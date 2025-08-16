package keystrokesmod.module.impl.player;

import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.settings.KeyBinding;
import keystrokesmod.eventbus.annotations.EventListener;

public class AutoJump extends Module {
    public static ButtonSetting b;
    private boolean c = false;

    public AutoJump() {
        super("AutoJump", Module.category.player, 0);
        this.registerSetting(b = new ButtonSetting("Cancel when shifting", true));
    }

    public void onDisable() {
        this.ju(this.c = false);
    }

    @EventListener
    public void p(PreUpdateEvent e) {
        if (Utils.nullCheck()) {
            if (mc.thePlayer.onGround && (!b.isToggled() || !mc.thePlayer.isSneaking())) {
                if (Utils.onEdge()) {
                    this.ju(this.c = true);
                } else if (this.c) {
                    this.ju(this.c = false);
                }
            } else if (this.c) {
                this.ju(this.c = false);
            }
        }
    }

    private void ju(boolean ju) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), ju);
    }
}
