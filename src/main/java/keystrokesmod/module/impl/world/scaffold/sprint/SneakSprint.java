package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.player.MoveInputEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class SneakSprint extends IScaffoldSprint {
    private final SliderSetting slowDown;

    public SneakSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(slowDown = new SliderSetting("SlowDown", 1, 0.2, 1, 0.01));
    }

    @EventListener
    public void onMoveInput(@NotNull MoveInputEvent event) {
        event.setSneak(true);
        event.setSneakSlowDown(slowDown.getInput());
    }

    @Override
    public boolean isSprint() {
        return mc.thePlayer.isSneaking();
    }
}
