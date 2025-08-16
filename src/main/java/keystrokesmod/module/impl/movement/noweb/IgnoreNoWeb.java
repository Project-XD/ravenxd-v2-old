package keystrokesmod.module.impl.movement.noweb;

import keystrokesmod.event.world.BlockWebEvent;
import keystrokesmod.module.impl.movement.NoWeb;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class IgnoreNoWeb extends SubMode<NoWeb> {
    public IgnoreNoWeb(String name, @NotNull NoWeb parent) {
        super(name, parent);
    }

    @EventListener
    public void onBlockWeb(@NotNull BlockWebEvent event) {
        event.cancel();
    }
}
