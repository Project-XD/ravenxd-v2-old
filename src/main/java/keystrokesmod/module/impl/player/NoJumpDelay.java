package keystrokesmod.module.impl.player;

import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.mixins.impl.entity.EntityLivingBaseAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.eventbus.annotations.EventListener;

public class NoJumpDelay extends Module {
    private final ButtonSetting notWhileScaffold;

    public NoJumpDelay() {
        super("NoJumpDelay", category.player);
        this.registerSetting(notWhileScaffold = new ButtonSetting("Not while scaffold", false));
    }

    @EventListener
    public void onPreUpdate(PreUpdateEvent event) {
        if (notWhileScaffold.isToggled() && ModuleManager.scaffold.isEnabled())
            return;
        ((EntityLivingBaseAccessor) mc.thePlayer).setJumpTicks(0);
    }
}
