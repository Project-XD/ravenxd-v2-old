package keystrokesmod.module.impl.combat.autoclicker;

import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.module.impl.other.RecordClick;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

public class RecordAutoClicker extends SubMode<IAutoClicker> {
    private final boolean left;
    private final boolean always;

    public RecordAutoClicker(String name, @NotNull IAutoClicker parent, boolean left, boolean always) {
        super(name, parent);
        this.left = left;
        this.always = always;
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (!always && (left ? !Mouse.isButtonDown(0) : !Mouse.isButtonDown(1)))
            return;
        if (System.currentTimeMillis() < RecordClick.getNextClickTime())
            return;

        if (parent.click())
            RecordClick.click();
    }
}
