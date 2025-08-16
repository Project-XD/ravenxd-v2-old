package keystrokesmod.module.impl.world;

import keystrokesmod.event.client.RightClickEvent;
import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import keystrokesmod.eventbus.annotations.EventListener;

public class FastPlace extends Module {
    public SliderSetting tickDelay;
    public ButtonSetting blocksOnly, pitchCheck;

    public FastPlace() {
        super("FastPlace", Module.category.world, 0);
        this.registerSetting(tickDelay = new SliderSetting("Tick delay", 1, 0, 3, 1));
        this.registerSetting(blocksOnly = new ButtonSetting("Blocks only", true));
        this.registerSetting(pitchCheck = new ButtonSetting("Pitch check", false));
    }

    @EventListener
    public void a(PreUpdateEvent e) {
        if (ModuleManager.scaffold.stopFastPlace()) {
            return;
        }
        if (Utils.nullCheck() && mc.inGameHasFocus && Reflection.rightClickDelayTimerField != null) {
            if (blocksOnly.isToggled()) {
                ItemStack item = SlotHandler.getHeldItem();
                if (item == null || !(item.getItem() instanceof ItemBlock)) {
                    return;
                }
            }

            try {
                int c = (int) tickDelay.getInput();
                if (c == 0) {
                    Reflection.rightClickDelayTimerField.set(mc, 0);
                } else {
                    if (c == 4) {
                        return;
                    }

                    int d = Reflection.rightClickDelayTimerField.getInt(mc);
                    if (d == 4) {
                        Reflection.rightClickDelayTimerField.set(mc, c);
                    }
                }
            } catch (IllegalAccessException | IndexOutOfBoundsException ignored) {
            }
        }
    }

    @EventListener
    public void onRightClick(RightClickEvent event) {
        try {
            int c = (int) tickDelay.getInput();
            if (c == 0) {
                Reflection.rightClickDelayTimerField.set(mc, 0);
            }
        } catch (IllegalAccessException | IndexOutOfBoundsException ignored) {
        }
    }
}
